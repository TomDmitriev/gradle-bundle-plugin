package org.dm.gradle.plugins.bundle

import org.gradle.api.Action
import org.gradle.api.tasks.bundling.Jar

import static org.dm.gradle.plugins.bundle.BundleUtils.newJarBuilder
import static org.dm.gradle.plugins.bundle.BundleUtils.getBundleSymbolicName
import static org.dm.gradle.plugins.bundle.BundleUtils.getVersion
import static org.dm.gradle.plugins.bundle.BundleUtils.getProperties
import static org.dm.gradle.plugins.bundle.BundleUtils.getClasspath
import static org.dm.gradle.plugins.bundle.BundleUtils.getSources
import static org.dm.gradle.plugins.bundle.BundleUtils.getResources
import static org.dm.gradle.plugins.bundle.BundleUtils.getTrace
import static org.dm.gradle.plugins.bundle.BundleUtils.getOutput

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundleGenerator implements Action<Jar> {
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
