package org.dm.gradle.plugins.bundle

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
enum DefaultJarBuilderFactory implements org.gradle.internal.Factory<JarBuilder> {
    INSTANCE

    @Override
    JarBuilder create() {
        return new JarBuilder()
    }
}
