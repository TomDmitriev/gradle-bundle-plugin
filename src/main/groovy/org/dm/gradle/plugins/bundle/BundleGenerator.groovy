package org.dm.gradle.plugins.bundle

import org.gradle.api.Action
import org.gradle.api.tasks.bundling.Jar

import static org.dm.gradle.plugins.bundle.BundleUtils.*

/**
 * An action to be used for generating bundles.
 */
class BundleGenerator implements Action<Jar> {
    /**
     * Creates and initializes a new {@link JarBuilder} using
     * {@link BundleExtension} parameters and uses it to produce
     * a new bundle.
     * @param jarTask the task within which this action is
     * performed
     */
    @Override
    void execute(Jar jarTask) {
        def project = jarTask.project
        newJarBuilder(jarTask).
                withProperties(getProperties(jarTask)).
                withClasspath(getClasspath(project)).
                withSourcepath(getSources(project)).
                withResources(getResources(project)).
                withVersion(getVersion(project)).
                withName(getBundleSymbolicName(project)).
                withTrace(getTrace(jarTask)).
                writeTo(getOutput(jarTask))
    }
}
