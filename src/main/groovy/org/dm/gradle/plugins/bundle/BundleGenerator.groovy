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
                withVersion(version(project)).
                withTrace(trace(jarTask)).
                writeTo(output(jarTask))
    }

    private static String version(Project project) {
        def projectVersion = project.version
        projectVersion == Project.DEFAULT_VERSION ? '0' : projectVersion
    }

    private static def props(Jar jarTask) {
        attributes(jarTask.manifest) + jarTask.project.bundle.instructions.collectEntries { key, value ->
            [key, value as String]
        }
    }

    private static def attributes(Manifest manifest) {
        def effManifest = manifest.effectiveManifest
        (effManifest.sections.values() + effManifest.attributes).inject([:]) { allAttrs, attrs ->
            allAttrs << attrs
        }.findAll {
            it.key != 'Manifest-Version'
        }
    }

    private static JarBuilder newJarBuilder(Jar jarTask) {
        jarTask.project.bundle.jarBuilderFactory.create()
    }

    private static File[] cp(Project project) {
        project.configurations.runtime.files
    }

    private static File[] src(Project project) {
        project.sourceSets.main.allSource.srcDirs.findAll {
            it.exists()
        }
    }

    private static File[] rsrc(Project project) {
        def output = project.sourceSets.main.output
        [output.classesDir, output.resourcesDir].findAll {
            it.exists()
        }
    }

    private static def trace(Jar jarTask) {
        jarTask.project.bundle.trace
    }

    private static def output(Jar jarTask) {
        jarTask.archivePath
    }
}
