package org.dm.gradle.plugins.bundle

import spock.lang.Specification

class DefaultJarBuilderFactorySpec extends Specification {
    def "Produces non-null JarBuilder"() {
        expect:
        DefaultJarBuilderFactory.INSTANCE.create() != null
    }
}