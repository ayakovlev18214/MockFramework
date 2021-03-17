package ru.nsu.mockframework;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javassist.*;
import javassist.util.HotSwapper;

/**
 * Main Framework class
 */
public class JMock {
    private final static String callback =
            "{MockHandler.callback(this, \"";
    private static int counter = 0;
    private static IntermediateRunningMock lastRanMock;

    public static void setLastRanMock(IntermediateRunningMock lastRanMock) {
        JMock.lastRanMock = lastRanMock;
    }

    private static void incCounter() {
        counter++;
    }

    public static void removeFinals(String cName) {
        try {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc = cp.get(cName);
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

    public static String primitiveToClass(String type) {
        return switch (type) {
            case "byte" -> "Byte";
            case "short" -> "Short";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "char" -> "Character";
            case "boolean" -> "Boolean";
            default -> type;
        };
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

            stub.setSuperclass(objClass);
            for (CtMethod method : objClass.getDeclaredMethods()) {
                CtMethod m = CtNewMethod.copy(method, method.getName(), stub, null);
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

    public static IntermediateRunningMock when(Object funcCall) {
        return lastRanMock;
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


