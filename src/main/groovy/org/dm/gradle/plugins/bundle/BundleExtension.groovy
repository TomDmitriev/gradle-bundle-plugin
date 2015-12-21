package org.dm.gradle.plugins.bundle

/**
 * A Bundle plugin extension.
 */
class BundleExtension {
    private final def instructions = [:]

    private final def excludeDependencies = []

    boolean trace = false

    boolean passProjectProperties = true

    org.gradle.internal.Factory<JarBuilder> jarBuilderFactory = DefaultJarBuilderFactory.INSTANCE

    def instruction(String name, String... values) {
        if (name == null || values == []) {
            return
        }
        String value = values.join(',')
        if (instructions.containsKey(name)) {
            instructions[name] += ",$value"
        } else {
            instructions[name] = value
        }
    }

    def exclude(Map excludeDeps) {
        excludeDependencies << excludeDeps
    }

    def getInstructions() {
        return instructions
    }

    def getExcludeDependencies() {
        return excludeDependencies
    }
}