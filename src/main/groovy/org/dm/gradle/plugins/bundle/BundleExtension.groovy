package org.dm.gradle.plugins.bundle

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundleExtension {
    private def instructions = [:]

    boolean trace = false

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

    def getInstructions() {
        return instructions
    }
}