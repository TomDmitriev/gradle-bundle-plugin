package org.dm.gradle.plugins.bundle

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.java.archives.Manifest
import org.gradle.api.java.archives.internal.DefaultAttributes
import org.gradle.api.java.archives.internal.DefaultManifest
import spock.lang.Specification

class BundleUtilsSpec extends Specification {
    def "calculates manifest attributes"() {
        given:
        Manifest manifest = new DefaultManifest(Mock(FileResolver))
        manifest.attributes << ['attr-1': 'foo', 'attr-2': 'bar']
        manifest.sections << ['sec-1': new DefaultAttributes() << ['attr-3': 'baz'],
                              'sec-2': new DefaultAttributes() << ['attr-4': 'qux']]

        expect:
        BundleUtils.attributes(manifest) == ['attr-1': 'foo', 'attr-2': 'bar',
                                             'attr-3': 'baz', 'attr-4': 'qux']
    }

    def "Converts to OSGi-compliant version"() {
        expect:
        BundleUtils.version(projectVersion) == bundleVersion

        where:
        projectVersion   | bundleVersion
        '1'              | '1'
        '1.2'            | '1.2'
        '1.2.3'          | '1.2.3'
        '1.2.3.4'        | '1.2.3.4'
        '1.2.3.4.5'      | '1.2.3.4_5'
        '1.2.3.4.5.6'    | '1.2.3.4_5_6'
        '1.2.3.4.5.6.7'  | '1.2.3.4_5_6_7'
        '1.2.3.4.5-6.7'  | '1.2.3.4_5-6_7'
        '1.2.3.4-5.6.7'  | '1.2.3.4-5_6_7'
        '1.2.3.ABC'      | '1.2.3.ABC'
        '1.2.ABC'        | '1.2.0.ABC'
        '1.ABC'          | '1.0.0.ABC'
        '1-ABC'          | '1.0.0.ABC'
        '1-20110303'     | '1.0.0.20110303'
        '1.2-20110303'   | '1.2.0.20110303'
        '1.2.3-20110303' | '1.2.3.20110303'
        '1_20110303'     | '1.0.0.20110303'
        '1.2_20110303'   | '1.2.0.20110303'
        '1.2.3_20110303' | '1.2.3.20110303'
        '1.2.3_20110303' | '1.2.3.20110303'
        '1*20110303'     | '1.0.0.20110303'
        '1*20110303'     | '1.0.0.20110303'
        '1.2*20110303'   | '1.2.0.20110303'
        '1.2.3*20110303' | '1.2.3.20110303'
        '1@20110303'     | '1.0.0.20110303'
        '1.2@20110303'   | '1.2.0.20110303'
        '1.2.3@20110303' | '1.2.3.20110303'
        '1!20110303'     | '1.0.0.20110303'
        '1.2!20110303'   | '1.2.0.20110303'
        '1.2.3!20110303' | '1.2.3.20110303'
        '1%20110303'     | '1.0.0.20110303'
        '1.2%20110303'   | '1.2.0.20110303'
        '1.2.3%20110303' | '1.2.3.20110303'
        '1^20110303'     | '1.0.0.20110303'
        '1.2^20110303'   | '1.2.0.20110303'
        '1.2.3^20110303' | '1.2.3.20110303'
        '1/20110303'     | '1.0.0.20110303'
        '1.2/20110303'   | '1.2.0.20110303'
        '1.2.3/20110303' | '1.2.3.20110303'
        '1-20.11$03@0#3' | '1.0.0.20_11_03_0_3'
        '100000'         | '100000'
    }
}
