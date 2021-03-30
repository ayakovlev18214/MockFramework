package ru.nsu.mockframework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;

/**
 * Main Framework class
 */
public class JMock {
  private final static String callback =
    "{MockHandler.callback(this, \"";
  private static int counter = 0;
  private static IntermediateMock lastRanMock;
  private static List<JMockArgs> argsList = new ArrayList<>();

  public static <T> StaticMock makeStaticMock(Class<T> tClass) {
    return new StaticMock(tClass);
  }

  public static void setLastRanMock(IntermediateMock lastRanMock) {
    JMock.lastRanMock = lastRanMock;
  }


  /**
   * Returns the mock object for class objectClass which has all the methods definitions,
   * but returns null/0 on any method call.
   * @param objectClass Class, for making mock
   * @param <T> Type of returned value
   * @return Mocked object of given class.
   */
  public static <T> T mock(Class<T> objectClass) {
    try {
      ClassPool cp = ClassPool.getDefault();
      cp.importPackage("ru.nsu.mockframework");
      CtClass cc = cp.makeClass(objectClass.getName() + "MockDelegator" + counter);
      CtClass objClass = cp.get(objectClass.getName());

      CtClass innerInterface = cp.get("ru.nsu.mockframework.IJMock");

      cc.setInterfaces(new CtClass[]{innerInterface});
      String className = objectClass.getName() + "Stub" + counter;
      CtClass stub = cp.makeClass(className);
      counter++;

      stub.setSuperclass(objClass);
      for (CtMethod method : objClass.getDeclaredMethods()) {
        if (Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        CtMethod m = CtNewMethod.copy(method, method.getName(), stub, null);
        if (Modifier.isFinal(m.getModifiers())) {
          m.setModifiers(m.getModifiers() & ~Modifier.FINAL);
        }
        m.setBody(null);
        stub.addMethod(m);
      }

      cc.addField(CtField.make("private " + objClass.getName() + " link;", cc));

      cc.addMethod(CtMethod.make("public void setLink(Object object) {this.link = ("
        + objectClass.getName()
        + ")object;}", cc));

      cc.addMethod(CtMethod.make("public Object getLink() {return link;}", cc));
      cc.setSuperclass(objClass);

      for (CtMethod method : objClass.getDeclaredMethods()) {
        if (Modifier.isStatic(method.getModifiers())) {
          continue;
        }
        CtMethod m = CtNewMethod.copy(method, method.getName(), cc, null);
        if (Modifier.isFinal(m.getModifiers())) {
          m.setModifiers(m.getModifiers() & ~Modifier.FINAL);
        }
        String l = "link.";
        if (!m.getReturnType().getName().equals("void")) {
          l = "return " + l;
        }
        String out = "\"); " + l
          + m.getName() + "($$);}";
        m.setBody(callback + m.getName() + "\", \"" + m.getSignature() + "\", \"" + m.getReturnType().getName() + out);
        cc.addMethod(m);
      }
      T obj = objectClass.cast(cc.toClass().getDeclaredConstructor().newInstance());
      ((IJMock) obj).setLink(stub.toClass().getDeclaredConstructor().newInstance());
      return obj;
    } catch (InstantiationException
      | InvocationTargetException
      | NoSuchMethodException
      | IllegalAccessException
      | CannotCompileException
      | NotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException("Error while mocking class " + objectClass.getName() + "\n" + e);
    }
  }

  public static IntermediateMock when(Object funcCall) {
    lastRanMock.setArgs(argsList);
    argsList = new ArrayList<>();
    return lastRanMock;
  }

  public static int anyNumerical() {
    argsList.add(new JMockArgs(EJMock.ANY, 0));
    return 0;
  }

  public static char anyChar() {
    argsList.add(new JMockArgs(EJMock.ANY, 'a'));
    return 'a';
  }

  public static boolean anyBool() {
    argsList.add(new JMockArgs(EJMock.ANY, true));
    return true;
  }


  public static <T> T any() {
    argsList.add(new JMockArgs(EJMock.ANY, null));
    return null;
  }


  public static <T> T eq(T obj) {
    argsList.add(new JMockArgs(EJMock.EQ, obj));
    return obj;
  }

  public static <T> void initMocks(Object obj) {
    for (Field fd : obj.getClass().getDeclaredFields()) {
      if (fd.isAnnotationPresent(Mock.class)) {
        fd.setAccessible(true);
        try {
          fd.set(obj, JMock.mock(fd.getType()));
        } catch (IllegalAccessException e) {

        }
        break;
      }
    }
  }
}


