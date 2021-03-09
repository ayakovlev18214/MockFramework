package ru.nsu.mockframework;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;

/**
 * Main Framework class
 */
public class JMock {
  private static int counter = 0;

  private static synchronized void incCounter() {
    counter++;
  }

  public static <T> T mock(Class<T> objectClass) {
    try {
      ClassPool cp = ClassPool.getDefault();
      CtClass s = cp.get(objectClass.getName());
      CtClass cc = cp.makeClass(objectClass.getName() + "Mock" + counter);
      incCounter();
      if (!cc.isFrozen()) {
        cc.setSuperclass(s);
      }
      return objectClass.cast(cc.toClass().getDeclaredConstructor().newInstance());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}


