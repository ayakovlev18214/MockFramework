package ru.nsu.mockframework;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class IntermediateMock {

  private static List<Object> returnVal = new ArrayList<>();
  private final IJMock mock;
  private final String methodName;
  private final String desc;
  private final String rType;
  private List<JMockArgs> args;

  IntermediateMock(IJMock mock, String methodName,
                   String desc, String rType) {
    this.mock = mock;
    this.methodName = methodName;
    this.desc = desc;
    this.rType = rType;
  }

  public static Object getLastRef(int id) {
    return returnVal.get(id);
  }

  void setArgs(List<JMockArgs> args) {
    this.args = args;
  }

  public void thenReturn(Object returnVal) {
    int id = IntermediateMock.returnVal.size();
    IntermediateMock.returnVal.add(returnVal);
    ClassPool cp = ClassPool.getDefault();
    try {
      CtClass lastStub = cp.get(mock.getLink().getClass().getName());
      CtClass newStub = cp.makeClass("Mock" + UUID.randomUUID().toString().replace("-", ""), lastStub); // subclass
      CtMethod oldMethod = lastStub.getMethod(methodName, desc);
      CtMethod newMethod = CtNewMethod.copy(oldMethod, methodName, newStub, null);

      StringBuilder methodBody = new StringBuilder("{");

      if (!args.isEmpty()) {
        int i = 1;
        for (JMockArgs arg : args) {
          switch (arg.getType()) {
            case ANY:
              break;
            case EQ:
              int newId = IntermediateMock.returnVal.size();
              IntermediateMock.returnVal.add(arg.getVal());
              methodBody
                .append("if((($w)$")
                .append(i)
                .append(").equals(($w)IntermediateMock.getLastRef(")
                .append(newId)
                .append(")))");
              break;
          }
          i++;
        }

      }

      newMethod.insertBefore(methodBody + " return ($r)IntermediateMock.getLastRef(" + id + ");}");
      newStub.addMethod(newMethod);
      mock.setLink(newStub.toClass().getDeclaredConstructor().newInstance());

    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Entered returnValue " +
        "can't be cast to returned type of given func\n" + e);
    } catch (CannotCompileException
      | NotFoundException
      | InstantiationException
      | InvocationTargetException
      | NoSuchMethodException
      | IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
