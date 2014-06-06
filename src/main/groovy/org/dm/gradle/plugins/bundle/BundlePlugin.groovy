package org.dm.gradle.plugins.bundle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin

/**
 * A bundle plugin which internally uses
 * <a href="http://www.aqute.biz/Bnd/Bnd">the bnd tool</a>
 * for generating bundles.
 */
class BundlePlugin implements Plugin<Project> {
    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(Project project) {
        project.extensions.create("bundle", BundleExtension)

        project.plugins.apply(JavaBasePlugin)
        project.plugins.withType(JavaPlugin) {
            project.jar { jarTask ->
                def jarBuilderFactory = new JarBuilderFactoryDecorator(
                        jarTask, project.bundle.jarBuilderFactory)

                actions = [new BundleGenerator(jarBuilderFactory)]
                manifest = new ManifestSubstitute(jarBuilderFactory, manifest)
            }
        }
    }
}
