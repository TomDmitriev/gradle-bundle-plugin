package org.dm.gradle.plugins.bundle

/**
 * A factory producing {@link JarBuilder}s.
 */
enum DefaultJarBuilderFactory implements org.gradle.internal.Factory<JarBuilder> {
    INSTANCE

    @Override
    JarBuilder create() {
        return new JarBuilder()
    }
}
