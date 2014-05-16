package org.dm.gradle.plugins.bundle

import spock.lang.Shared
import spock.lang.IgnoreRest
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import static java.nio.file.Files.createDirectories as createDirs

/**
 * @author <a href="mailto:dm.artyom@gmail.com">Artyom Dmitriev</a>
 */
class BundlePluginIntegrationSpec extends Specification {
    @Shared
    Path projectDir = Files.createTempDirectory("test")
    @Shared
    Path buildScript = projectDir.resolve('build.gradle')
    String stdout, stderr

    void setupSpec() {
        createSources()
    }

    private void createSources() {
        def javaSrc = createDirs projectDir.resolve('src/main/java/org/foo/bar')
        javaSrc.resolve('TestActivator.java').toFile().write getClass().classLoader.getResource('TestActivator.java').text
        javaSrc.resolve('More.java').toFile().write 'package org.foo.bar;\n class More {}'
    }

    void setup() {
        buildScript.toFile().write getClass().classLoader.getResource('build.test').text
    }

    void cleanupSpec() {
        projectDir.toFile().deleteDir()
    }

    def "Jar task is executed while build"() {
        when:
        executeGradleCommand 'build'

        then:
        stdout =~ /(?m)^:jar$/
    }

    def "Uses bundle instructions"() {
        when:
        executeGradleCommand 'jar'

        then:
        manifestContains 'Bundle-Activator: org.foo.bar.TestActivator'
    }

    def "Uses jar manifest values"() {
        when:
        buildScript.toFile().append '\njar { manifest { attributes("Built-By": "abc") } }'
        executeGradleCommand 'jar'

        then:
        manifestContains 'Built-By: abc'
    }

    def "Overwrites jar manifest values"() {
        when:
        buildScript.toFile().append '\njar { manifest { attributes("Built-By": "abc") } }\nbundle { instructions << ["Built-By": "xyz"] }'
        executeGradleCommand 'jar'

        then:
        manifestContains 'Built-By: xyz'
    }

    def "Uses baseName and extension defined in jar task"() {
        when:
        buildScript.toFile().append '\njar { baseName = "xyz"\nextension = "baz" }'
        executeGradleCommand 'jar'

        then:
        Files.exists projectDir.resolve('build/libs/xyz.baz')
    }

    def "Ignores unknown attributes"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["junk": "xyz"] }'
        executeGradleCommand 'jar'

        then:
        stdout =~ /(?m)^BUILD SUCCESSFUL$/
    }

    def "Includes project output class files by default"() {
        when:
        executeGradleCommand 'jar'

        then:
        jarContains 'org/foo/bar/TestActivator.class'
        jarContains 'org/foo/bar/More.class'
    }

    def "Includes project resources by default"() {
        setup:
        def resources = createDirs projectDir.resolve('src/main/resources/org/foo/bar')
        resources.resolve('dummy.txt').toFile().write 'abc'

        when:
        executeGradleCommand 'jar'

        then:
        jarContains 'org/foo/bar/dummy.txt'

        cleanup:
        resources.toFile().deleteDir()
    }

    def "Includes project sources if instructed"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["-sources": true] }'
        executeGradleCommand 'jar'

        then:
        jarContains 'OSGI-OPT/src/org/foo/bar/TestActivator.java'
        jarContains 'OSGI-OPT/src/org/foo/bar/More.java'
    }

    def "Displays builder classpath"() {
        when:
        executeGradleCommand 'jar -d'

        then:
        stdout =~ /The Builder is about to produce a jar using classpath: \[.+\]/
    }

    def "Displays errors"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["Bundle-Activator": "org.foo.bar.NotExistingActivator"] }'
        executeGradleCommand 'jar'

        then:
        stdout =~ /(?m)^BUILD SUCCESSFUL$/
        stderr =~ /Bundle-Activator not found/
    }

    def "Can trace bnd build process"() {
        when:
        buildScript.toFile().append '\nbundle { trace = true }'
        executeGradleCommand 'jar'

        then:
        stderr =~ /(?m)^# build$/
    }

    private def executeGradleCommand(cmd) {
        def process = "gradle clean $cmd -b $projectDir/build.gradle".execute()
        process.waitFor()

        assert process.exitValue() == 0

        stdout = process.in.text
        stderr = process.err.text
    }

    private def manifestContains(String line) {
        jarFile.getInputStream(new ZipEntry('META-INF/MANIFEST.MF')).text =~ "(?m)^$line\$"
    }

    private ZipFile getJarFile() {
        new ZipFile(projectDir.resolve("build/libs/${projectDir.fileName}.jar").toFile())
    }

    private def jarContains(String entry) {
        jarFile.getEntry(entry) != null
    }
}