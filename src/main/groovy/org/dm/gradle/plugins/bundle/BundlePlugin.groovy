package org.dm.gradle.plugins.bundle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.extensions.create("bundle", BundleExtension)

        project.plugins.apply(JavaBasePlugin)
        project.plugins.withType(JavaPlugin) {
            project.jar {
                actions = [new BundleGenerator()]
            }
        }
    }
}
