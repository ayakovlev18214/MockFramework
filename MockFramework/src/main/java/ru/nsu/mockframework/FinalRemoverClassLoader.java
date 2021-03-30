package ru.nsu.mockframework;

import javassist.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class FinalRemoverClassLoader extends ClassLoader {

    private static Set<String> classes = Collections.emptySet();

    private final ClassPool pool;

    public static void setClasses(Set<String> set) {
        classes = new HashSet<String>(set);
    }

    public FinalRemoverClassLoader() {
        super(Thread.currentThread().getContextClassLoader());
        pool = ClassPool.getDefault();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //System.out.println(name);
        if (name.startsWith("java.")) {
            return super.loadClass(name);
        } else if (name.startsWith("javax.")) {
            return super.loadClass(name);
        } else if (name.startsWith("sun.")) {
            return super.loadClass(name);
        } else if (name.startsWith("org.junit.")) {
            return super.loadClass(name);
        } else {
            if (classes.contains(name)) {
                //System.out.println(name);
                return findClass(name);
            } else {
                return findClassNonRemove(name);
            }
        }
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {

        try {
            CtClass cc = pool.get(name);

            if (Modifier.isFinal(cc.getModifiers())) {
                cc.setModifiers(cc.getModifiers() & ~Modifier.FINAL);
            }

            CtMethod[] methods = cc.getDeclaredMethods();
            for (CtMethod method : methods) {
                if (Modifier.isFinal(method.getModifiers())) {
                    method.setModifiers(method.getModifiers() & ~Modifier.FINAL);
                }
            }

            byte[] b = cc.toBytecode();

            return defineClass(name, b, 0, b.length);
        } catch (NotFoundException | IOException | CannotCompileException e) {
            throw new ClassNotFoundException();
        }
    }

    public Class<?> findClassNonRemove(String name) throws ClassNotFoundException {

        try {
            CtClass cc = pool.get(name);

            byte[] b = cc.toBytecode();

            return defineClass(name, b, 0, b.length);
        } catch (NotFoundException | IOException | CannotCompileException e) {
            throw new ClassNotFoundException();
        }
    }

}
