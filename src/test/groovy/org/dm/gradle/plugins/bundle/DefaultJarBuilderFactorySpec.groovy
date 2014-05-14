package org.dm.gradle.plugins.bundle

import spock.lang.Specification

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class DefaultJarBuilderFactorySpec extends Specification {
    def "Produces non-null JarBuilder"() {
        expect:
        DefaultJarBuilderFactory.INSTANCE.create() != null
    }
}