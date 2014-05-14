package org.dm.gradle.plugins.bundle

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundleExtension {
    def instructions = [:]

    boolean trace = false

    org.gradle.internal.Factory<JarBuilder> jarBuilderFactory = DefaultJarBuilderFactory.INSTANCE
}