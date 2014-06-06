package org.dm.gradle.plugins.bundle

import spock.lang.Specification

import java.nio.charset.Charset

class WriterToOutputStreamAdapterSpec extends Specification {
    StringWriter writer = new StringWriter()
    WriterToOutputStreamAdapter adapter = new WriterToOutputStreamAdapter(writer, Charset.forName('UTF-8'))

    def "Writes bytes as characters in correct encoding"() {
        when:
        adapter.write(bytes as byte[])
        adapter.close()

        then:
        writer.toString() == expectedString

        where:
        bytes                                           | expectedString
        [70, 105, 114]                                  | 'Fir'
        [-48, -127, -48, -69, -48, -70, -48, -80]       | 'Ёлка'
        [-29, -125, -94, -29, -125, -97, -27, -79, -98] | 'モミ属'
        []                                              | ''
    }
}