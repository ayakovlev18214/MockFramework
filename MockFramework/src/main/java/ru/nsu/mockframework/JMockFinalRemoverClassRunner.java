package ru.nsu.mockframework;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.HashSet;
import java.util.Set;

public class JMockFinalRemoverClassRunner extends BlockJUnit4ClassRunner {
    static ClassLoader customClassLoader = null;

    public JMockFinalRemoverClassRunner(Class<?> clazz) throws InitializationError {
        super(loadFromCustomClassloader(clazz));
    }

    // Loads a class in the custom classloader
    private static Class<?> loadFromCustomClassloader(Class<?> clazz) throws InitializationError {
        try {
            FinalRemoverClassLoader.setClasses(getClassesToAlter(clazz));
            // Only load once to support parallel tests
            if (customClassLoader == null) {
                customClassLoader = new FinalRemoverClassLoader();
            }
            return Class.forName(clazz.getName(), true, customClassLoader);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
    }

    private static Set<String> getClassesToAlter(Class<?> unitTestClass) {
        Set<String> set = new HashSet<>();

        RemoveFinals annotation = unitTestClass.getAnnotation(RemoveFinals.class);

        if (annotation != null) {
            for (Class<?> c : annotation.value()) {
                set.add(c.getName());
            }
        }

        return set;
    }

    // Runs junit tests in a separate thread using the custom class loader
    @Override
    public void run(final RunNotifier notifier) {
        Thread thread = new Thread(() -> super.run(notifier));
        thread.setContextClassLoader(customClassLoader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
