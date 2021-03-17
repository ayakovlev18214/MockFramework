package ru.nsu.mockframework;

import javassist.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IntermediateRunningMock {

    private static List<Object> returnVal = new ArrayList<>();
    private final IJMock mock;
    private final String methodName;
    private final String desc;
    private final String rType;

    IntermediateRunningMock(IJMock mock, String methodName,
                            String desc, String rType) {
        this.mock = mock;
        this.methodName = methodName;
        this.desc = desc;
        this.rType = rType;
    }

    public static Object getLastRef(int id) {
        return returnVal.get(id);
    }

    public void thenReturn(Object returnVal) {
        int id = IntermediateRunningMock.returnVal.size();
        IntermediateRunningMock.returnVal.add(returnVal);
        ClassPool cp = ClassPool.getDefault();

        try {
            //CtClass mockDelegatorClass = cp.get(intermediateRunningMock.getMock().getClass().getName());
            //IJMock mock = (IJMock) intermediateRunningMock.getMock();

            CtClass lastStub = cp.get(mock.getLink().getClass().getName());
            CtClass newStub = cp.makeClass("Mock" + UUID.randomUUID().toString().substring(0, 7), lastStub); // subclass
            CtMethod oldMethod = lastStub.getMethod(methodName, desc);
            CtMethod newMethod = CtNewMethod.copy(oldMethod, methodName, newStub, null);
            //System.out.println(rType);
            //System.out.println(newMethod.getName());

            newStub.addField(
                    CtField.make(
                            "private " + rType +
                                    " ret = (" + rType  +
                                    ")IntermediateRunningMock.getLastRef("+id+");", newStub));

            newMethod.setBody("{return ret;}");
            newStub.addMethod(newMethod);
            mock.setLink(newStub.toClass().getDeclaredConstructor().newInstance());
//      for (CtMethod method : mockDelegatorClass.getDeclaredMethods()) {
//        if (method.getName().equals(intermediateRunningMock.getMethodName())) {
//          CtMethod stubMethod = new CtMethod(method, newStub, null);
//          String fieldName = UUID.randomUUID().toString().replace("-", "");
//          fieldName = "a" + fieldName;
//          CtField field = new CtField(method.getReturnType(), fieldName, newStub);
//          newStub.addField(field);
//          newStub.addInterface(cp.get("ru.nsu.mockframework.IJMock"));
//          try {
//            CtMethod m = newStub.getDeclaredMethod("setVar");
//            newStub.removeMethod(m);
//          } catch (NotFoundException ignored) {
//          }
//          newStub.addMethod(CtNewMethod.make("public void setVar(Object object) {this."
//                  + fieldName
//                  + " = ("
//                  + method.getReturnType().getName()
//                  + ")object;}", newStub));
//
//          stubMethod.setBody("{return " + fieldName + ";}");
//          newStub.addMethod(stubMethod);
//          Object stub = newStub.toClass().getDeclaredConstructor().newInstance();
//          mock.setLink(stub);
//          mock = (IJMock) stub; // это че за?
//          mock.setVar(returnedVal);
//          break;
//        }
//      }
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
