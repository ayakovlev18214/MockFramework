package ru.nsu.mockframework;

import java.lang.reflect.Field;

public class JMockAnnotations {
  public static <T> void initMocks(Class<T> cl) {
    for (Field fd : cl.getDeclaredFields()) {
      if (fd.isAnnotationPresent(Mock.class)) {
        fd.setAccessible(true);
        try {
          fd.set(fd, JMock.mock(fd.getType()));
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        break;
      }
    }
  }


}
