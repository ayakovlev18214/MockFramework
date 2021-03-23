package ru.nsu.mockframework;

import javassist.*;
import javassist.util.HotSwapAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StaticMock implements AutoCloseable {
    private final List<CtMethod> save = new ArrayList<>();
    private CtClass cc;
    private final Class<?> staticClass;
    public StaticMock(Class<?> staticClass) {
        this.staticClass = staticClass;
        ClassPool cp = ClassPool.getDefault();
        try {
            cc = cp.get(staticClass.getName());
            for (CtMethod method : cc.getDeclaredMethods()) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    continue;
                }
                save.add(CtNewMethod.copy(method, method.getName(), cc, null));
                method.setBody(null);
            }
            HotSwapAgent.redefine(staticClass, cc);
        } catch (NotFoundException | CannotCompileException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void close()  {
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
    }
}
