package org.dm.gradle.plugins.bundle

import org.gradle.api.tasks.bundling.Jar

import static org.dm.gradle.plugins.bundle.Objects.requireNonNull
import static org.dm.gradle.plugins.bundle.BundleUtils.*

final class JarBuilderFactoryDecorator implements org.gradle.internal.Factory<JarBuilder> {
    private final Jar jarTask
    private final org.gradle.internal.Factory<JarBuilder> factory
    private JarBuilder jarBuilder

    JarBuilderFactoryDecorator(Jar jarTask, org.gradle.internal.Factory<JarBuilder> factory) {
        this.jarTask = requireNonNull(jarTask)
        this.factory = requireNonNull(factory)
    }

    @Override
    JarBuilder create() {
        if (jarBuilder != null) {
            return jarBuilder
        }
        def project = jarTask.project
        return jarBuilder = factory.create().
                withBase(getBase(project)).
                withProperties(getProperties(jarTask)).
                withClasspath(getClasspath(jarTask)).
                withSourcepath(getSources(project)).
                withResources(getResources(project)).
                withVersion(getVersion(project)).
                withName(getBundleSymbolicName(project)).
                withTrace(getTrace(jarTask)).
                withFailBuild(getFailBuild(jarTask))
    }
}
