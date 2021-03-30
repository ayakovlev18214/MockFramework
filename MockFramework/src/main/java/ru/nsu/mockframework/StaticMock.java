package ru.nsu.mockframework;

import javassist.*;
import javassist.util.HotSwapAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StaticMock implements AutoCloseable{
    private final List<CtMethod> save = new ArrayList<>();
    private CtClass cc;
    private static int lastId = 0;
    private Object currMock;
    private static final List<Object> mocksRefs = new ArrayList<>();
    private final Class<?> staticClass;

    StaticMock(Class<?> staticClass) {
        this.staticClass = staticClass;
        ClassPool cp = ClassPool.getDefault();
        try {
            cc = cp.get(staticClass.getName());
            if (cc.isFrozen()) cc.defrost();
            CtClass staticMockHelper = cp.makeClass(
                    "StaticMock" + UUID.randomUUID().toString().replace("-", ""));
            String helperName = staticMockHelper.getName();

            CtMethod[] methods = cc.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                CtMethod copy = CtNewMethod.copy(method, method.getName(), staticMockHelper, null);
                copy.setModifiers(Modifier.clear(copy.getModifiers(), Modifier.STATIC));
                copy.setBody(null);
                staticMockHelper.addMethod(copy);
            }
            mocksRefs.add(JMock.mock(staticMockHelper.toClass()));
            currMock = mocksRefs.get(lastId);
            for (CtMethod method : methods) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                save.add(CtNewMethod.copy(method, method.getName(), cc, null));
                String body = "((" + helperName + ")ru.nsu.mockframework.StaticMock.getMockRef(" + lastId + "))."
                        + method.getName() + "($$);";
                if (!method.getReturnType().getName().equals("void")) {
                    body = "return ($r)" + body;
                }
                method.setBody("{" + body + "}");
            }
            lastId++;
            HotSwapAgent.redefine(staticClass, cc);
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Object getMockRef(int id) {
        return mocksRefs.get(id);
    }

    @Override
    public void close() {
        cc.defrost();
        for (CtMethod method : cc.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            try {
                cc.removeMethod(method);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        save.forEach(ctMethod -> {
            try {
                cc.addMethod(ctMethod);
            } catch (CannotCompileException e) {
                e.printStackTrace();
            }
        });
        try {
            HotSwapAgent.redefine(staticClass, cc);
        } catch (NotFoundException | IOException | CannotCompileException e) {
            e.printStackTrace();
        }
        mocksRefs.remove(currMock);
    }
}
