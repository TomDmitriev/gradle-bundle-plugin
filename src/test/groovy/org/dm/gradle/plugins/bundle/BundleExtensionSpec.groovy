package org.dm.gradle.plugins.bundle

import spock.lang.Specification

class BundleExtensionSpec extends Specification {
    BundleExtension bundleExtension = new BundleExtension()

    def "When individual instruction is set converts arguments to one value"() {
        when:
        bundleExtension.instruction('Build-By', * instructionArgs[0])
        bundleExtension.instruction('Build-By', * instructionArgs[1])

        then:
        bundleExtension.instructions['Build-By'] == expectedInstruction

        where:
        instructionArgs          | expectedInstruction
        [['a'], []]              | 'a'
        [['a', 'b'], []]         | 'a,b'
        [['ab'], []]             | 'ab'
        [[], []]                 | null
        [['a'], ['x']]           | 'a,x'
        [['a'], ['x', 'y']]      | 'a,x,y'
        [[], ['x', 'y']]         | 'x,y'
        [['a', 'b'], ['x', 'y']] | 'a,b,x,y'
    }
}
