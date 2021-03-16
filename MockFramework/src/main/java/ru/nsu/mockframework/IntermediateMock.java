package ru.nsu.mockframework;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class IntermediateMock {
  private final IntermediateRunningMock intermediateRunningMock;

  IntermediateMock(IntermediateRunningMock intermediateRunningMock) {
    this.intermediateRunningMock = intermediateRunningMock;
  }

  public void thenReturn(Object returnedVal) {
    ClassPool cp = ClassPool.getDefault();

    try {
      CtClass mockDelegatorClass = cp.get(intermediateRunningMock.getMock().getClass().getName());

      IJMock mock = (IJMock) intermediateRunningMock.getMock();
      CtClass lastStub = cp.get(mock.getLink().getClass().getName());
      CtClass newStub = cp.makeClass(UUID.randomUUID().toString(), lastStub); // subclass

      for (CtMethod method : mockDelegatorClass.getDeclaredMethods()) {
        if (method.getName().equals(intermediateRunningMock.getMethodName())) {
          CtMethod stubMethod = new CtMethod(method, newStub, null);
          String fieldName = UUID.randomUUID().toString().replace("-", "");
          fieldName = "a" + fieldName;
          CtField field = new CtField(method.getReturnType(), fieldName, newStub);
          newStub.addField(field);
          newStub.addInterface(cp.get("ru.nsu.mockframework.IJMock"));
          try {
            CtMethod m = newStub.getDeclaredMethod("setVar");
            newStub.removeMethod(m);
          } catch (NotFoundException ignored) {
          }
          newStub.addMethod(CtNewMethod.make("public void setVar(Object object) {this."
            + fieldName
            + " = ("
            + method.getReturnType().getName()
            + ")object;}", newStub));

          stubMethod.setBody("{return " + fieldName + ";}");
          newStub.addMethod(stubMethod);
          Object stub = newStub.toClass().getDeclaredConstructor().newInstance();
          mock.setLink(stub);
          mock = (IJMock) stub;
          mock.setVar(returnedVal);
          break;
        }
      }
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Entered returnValue " +
        "can't be cast to returned type of given func\n" + e);
    } catch (InstantiationException
      | InvocationTargetException
      | NoSuchMethodException
      | IllegalAccessException
      | CannotCompileException
      | NotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
