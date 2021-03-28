package ru.nsu.mockframework;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RemoveFinals {
    Class<?>[] value();
}
