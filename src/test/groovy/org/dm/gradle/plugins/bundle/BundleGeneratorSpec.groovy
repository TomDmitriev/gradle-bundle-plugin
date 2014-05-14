package org.dm.gradle.plugins.bundle

import org.gradle.api.internal.file.FileResolver
import org.gradle.api.java.archives.Manifest
import org.gradle.api.java.archives.internal.DefaultAttributes
import org.gradle.api.java.archives.internal.DefaultManifest
import spock.lang.Specification

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundleGeneratorSpec extends Specification {
    def "calculates manifest attributes"() {
        given:
        Manifest manifest = new DefaultManifest(Mock(FileResolver))
        manifest.attributes << ['attr-1': 'foo', 'attr-2': 'bar']
        manifest.sections << ['sec-1': new DefaultAttributes() << ['attr-3': 'baz'],
                              'sec-2': new DefaultAttributes() << ['attr-4': 'qux']]

        expect:
        BundleGenerator.attributes(manifest) == ['attr-1': 'foo', 'attr-2': 'bar',
                                                 'attr-3': 'baz', 'attr-4': 'qux']
    }
}