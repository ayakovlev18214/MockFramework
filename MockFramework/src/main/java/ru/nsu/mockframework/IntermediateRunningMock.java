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
            CtClass lastStub = cp.get(mock.getLink().getClass().getName());
            CtClass newStub = cp.makeClass("Mock" + UUID.randomUUID().toString().substring(0, 7), lastStub); // subclass
            CtMethod oldMethod = lastStub.getMethod(methodName, desc);
            CtMethod newMethod = CtNewMethod.copy(oldMethod, methodName, newStub, null);


            newMethod.setBody("{return ($r)IntermediateRunningMock.getLastRef("+id+");}");
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
