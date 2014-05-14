package org.dm.gradle.plugins.bundle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.java.archives.Manifest
import org.gradle.api.tasks.bundling.Jar

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundleGenerator implements Action<Jar> {
    @Override
    void execute(Jar jarTask) {
        def project = jarTask.project
        newJarBuilder(jarTask).
                withProperties(props(jarTask)).
                withClasspath(cp(project)).
                withSourcepath(src(project)).
                withResources(rsrc(project)).
                withTrace(trace(jarTask)).
                writeTo(output(jarTask))
    }

    static def props(Jar jarTask) {
        attributes(jarTask.manifest) + jarTask.project.bundle.instructions.collectEntries { key, value ->
            [key, value as String]
        }
    }

    static def attributes(Manifest manifest) {
        def effManifest = manifest.effectiveManifest
        (effManifest.sections.values() + effManifest.attributes).inject([:]) { allAttrs, attrs ->
            allAttrs << attrs
        }.findAll {
            it.key != 'Manifest-Version'
        }
    }

    static JarBuilder newJarBuilder(Jar jarTask) {
        jarTask.project.bundle.jarBuilderFactory.create()
    }

    static File[] cp(Project project) {
        project.configurations.runtime.files
    }

    static File[] src(Project project) {
        project.sourceSets.main.allSource.srcDirs
    }

    static File[] rsrc(Project project) {
        def output = project.sourceSets.main.output
        [output.classesDir, output.resourcesDir]
    }

    static def trace(Jar jarTask) {
        jarTask.project.bundle.trace
    }

    static def output(Jar jarTask) {
        jarTask.archivePath
    }
}
