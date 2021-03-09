package ru.nsu.mockframework;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MockClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {

        byte[] byteCode = classfileBuffer;
        System.out.println("premain");
        return byteCode;
    }
}
