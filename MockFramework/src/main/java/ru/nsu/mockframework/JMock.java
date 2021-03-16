package ru.nsu.mockframework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    "{MockHandler.callback(this, new Throwable().getStackTrace()[0].getMethodName());";
  private static int counter = 0;
  private static IntermediateRunningMock lastRunnedMock;

  public static void setLastRunnedMock(IntermediateRunningMock lastRunnedMock) {
    JMock.lastRunnedMock = lastRunnedMock;
  }
  private static void incCounter() {
    counter++;
  }

  public static <T> void removeFinals(Class<T> tClass) {
    try {
      ClassPool cp = ClassPool.getDefault();
      CtClass cc = cp.get(tClass.getName());
      int modifiers = cc.getModifiers();
      if (Modifier.isFinal(modifiers)) {
        int notFinalModifier = Modifier.clear(modifiers, Modifier.FINAL);
        cc.setModifiers(notFinalModifier);
      }
      cc.toClass();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

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
      incCounter();
      for (CtMethod method : objClass.getDeclaredMethods()) {
        CtMethod m = CtNewMethod.copy(method, method.getName(), stub, null);
        m.setBody(null);
        stub.addMethod(m);
      }

      stub.setSuperclass(objClass);
      cc.addField(CtField.make("private " + objClass.getName() + " link;", cc));

      cc.addMethod(CtMethod.make("public void setLink(Object object) {this.link = ("
        + objectClass.getName()
        + ")object;}", cc));

      cc.addMethod(CtMethod.make("public Object getLink() {return link;}", cc));
      cc.setSuperclass(objClass);

      for (CtMethod method : objClass.getDeclaredMethods()) {
        CtMethod m = CtNewMethod.copy(method, method.getName(), cc, null);
        String out = "return link"
          + method.getLongName().substring(method.getLongName().lastIndexOf('.'))
          + ";}";
        m.setBody(callback + out);
        cc.addMethod(m);
      }

      objClass.freeze();
      T obj = objectClass.cast(cc.toClass().getDeclaredConstructor().newInstance());
      ((IJMock) obj).setLink(stub.toClass().getDeclaredConstructor().newInstance());
      return obj;
    } catch (InstantiationException
      | InvocationTargetException
      | NoSuchMethodException
      | IllegalAccessException
      | CannotCompileException
      | NotFoundException e) {
      throw new RuntimeException("Error while mocking class " + objectClass.getName() + "\n" + e);
    }
  }

  public static <T> IntermediateMock when(T funcCall) {
    IntermediateRunningMock mock = lastRunnedMock;
    lastRunnedMock = null;
    return new IntermediateMock(mock);
  }

  public static <T> void initMocks(Class<T> cl) {
    for (Field fd : cl.getDeclaredFields()) {
      if (fd.isAnnotationPresent(Mock.class)) {
        fd.setAccessible(true);
        try {
          fd.set(fd, JMock.mock(fd.getType()));
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Can't set mock to the field" + e);
        }
        break;
      }
    }
  }
}


