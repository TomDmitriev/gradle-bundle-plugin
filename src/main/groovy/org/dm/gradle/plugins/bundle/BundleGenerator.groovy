package org.dm.gradle.plugins.bundle

import org.gradle.api.Action
import org.gradle.api.tasks.bundling.Jar

import static org.dm.gradle.plugins.bundle.Objects.requireNonNull
import static org.dm.gradle.plugins.bundle.BundleUtils.*

/**
 * An action to be used for generating bundles.
 */
class BundleGenerator implements Action<Jar> {
    private final org.gradle.internal.Factory<JarBuilder> jarBuilderFactory

    BundleGenerator(org.gradle.internal.Factory<JarBuilder> jarBuilderFactory) {
        this.jarBuilderFactory = requireNonNull(jarBuilderFactory)
    }

    /**
     * Creates and initializes a new {@link JarBuilder} using
     * {@link BundleExtension} parameters and uses it to produce
     * a new bundle.
     * @param jarTask the task within which this action is
     * performed
     */
    @Override
    void execute(Jar jarTask) {
        jarBuilderFactory.create().writeJarTo(getOutput(jarTask))
    }
}
