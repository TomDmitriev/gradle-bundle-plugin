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
 * A set of integration tests. The routine of each test is
 * to execute gradle tasks against a test project and look
 * at the result (e.g. contents of the produced jar).
 * <p/>
 * Currently the tests may only be run in a Unix OS with
 * the plugin deployed in the local maven repo.
 */
class BundlePluginIntegrationSpec extends Specification {
    @Shared
    Path projectDir = Files.createTempDirectory("test")
    @Shared
    Path buildScript = projectDir.resolve('build.gradle')
    String stdout, stderr, jarName

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
        executeGradleCommand 'clean build'

        then:
        stdout =~ /(?m)^:jar$/
    }

    def "Uses project version as 'Bundle-Version' by default"() {
        when:
        buildScript.toFile().append '\nversion = "1.0.2"'
        executeGradleCommand 'clean jar'
        jarName = "build/libs/${projectDir.fileName}-1.0.2.jar"

        then:
        manifestContains 'Bundle-Version: 1.0.2'
    }

    def "Overwrites project version using 'Bundle-Version' instruction"() {
        when:
        buildScript.toFile().append '\nversion = "1.0.2"\nbundle { instructions << ["Bundle-Version": "5.0"] }'
        executeGradleCommand 'clean jar'
        jarName = "build/libs/${projectDir.fileName}-1.0.2.jar"

        then:
        manifestContains 'Bundle-Version: 5.0'
    }

    def "Uses bundle instructions"() {
        when:
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Bundle-Activator: org.foo.bar.TestActivator'
    }

    def "Uses jar manifest values"() {
        when:
        buildScript.toFile().append '\njar { manifest { attributes("Built-By": "abc") } }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Built-By: abc'
    }

    def "Overwrites jar manifest values"() {
        when:
        buildScript.toFile().append '\njar { manifest { attributes("Built-By": "abc") } }\nbundle { instructions << ["Built-By": "xyz"] }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Built-By: xyz'
    }

    def "Uses baseName and extension defined in jar task"() {
        when:
        buildScript.toFile().append '\njar { baseName = "xyz"\nextension = "baz" }'
        executeGradleCommand 'clean jar'

        then:
        Files.exists projectDir.resolve('build/libs/xyz.baz')
    }

    def "Ignores unknown attributes"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["junk": "xyz"] }'
        executeGradleCommand 'clean jar'

        then:
        stdout =~ /(?m)^BUILD SUCCESSFUL$/
    }

    def "Includes project output class files by default"() {
        when:
        executeGradleCommand 'clean jar'

        then:
        jarContains 'org/foo/bar/TestActivator.class'
        jarContains 'org/foo/bar/More.class'
    }

    def "Includes project resources by default"() {
        setup:
        def resources = createDirs projectDir.resolve('src/main/resources/org/foo/bar')
        resources.resolve('dummy.txt').toFile().write 'abc'

        when:
        executeGradleCommand 'clean jar'

        then:
        jarContains 'org/foo/bar/dummy.txt'

        cleanup:
        resources.toFile().deleteDir()
    }

    def "Includes project sources if instructed"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["-sources": true] }'
        executeGradleCommand 'clean jar'

        then:
        jarContains 'OSGI-OPT/src/org/foo/bar/TestActivator.java'
        jarContains 'OSGI-OPT/src/org/foo/bar/More.java'
    }

    def "Supports old OSGI plugin instruction format"() {
        when:
        buildScript.toFile().append '\nbundle { instruction "Built-By", "ab", "c"\ninstruction "Built-By", "x", "y", "z" }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Built-By: ab,c,x,y,z'
    }

    def "Displays builder classpath"() {
        when:
        executeGradleCommand 'clean jar -d'

        then:
        stdout =~ /The Builder is about to generate a jar using classpath: \[.+\]/
    }

    def "Displays errors"() {
        when:
        buildScript.toFile().append '\nbundle { instructions << ["Bundle-Activator": "org.foo.bar.NotExistingActivator"] }'
        executeGradleCommand 'clean jar'

        then:
        stdout =~ /(?m)^BUILD SUCCESSFUL$/
        stderr =~ /Bundle-Activator not found/
    }

    def "Can trace bnd build process"() {
        when:
        buildScript.toFile().append '\nbundle { trace = true }'
        executeGradleCommand 'clean jar'

        then:
        stderr =~ /(?m)^# build$/
    }

    @Issue(1)
    def "Saves manifest under build/tmp"() {
        when:
        executeGradleCommand 'clean jar'

        then:
        projectDir.resolve('build/tmp/jar/MANIFEST.MF').toFile().text == manifest.replaceAll('(?m)^Bnd-LastModified: \\d+$\r\n', '')
    }

    @Issue(1)
    def "Does not re-execute 'jar' when manifest has not been changed"() {
        when:
        executeGradleCommand 'clean jar'
        executeGradleCommand 'jar'

        then:
        stdout =~ /(?m)^:jar UP-TO-DATE$/
    }

    @Issue(1)
    def "Re-executes 'jar' when manifest has been changed"() {
        when:
        executeGradleCommand 'clean jar'
        buildScript.toFile().append '\nbundle { instructions << ["Built-By": "xyz"] }'
        executeGradleCommand 'jar'

        then:
        stdout =~ /(?m)^:jar$/
    }

    @Issue(8)
    def "Uses instructions (Private-Package) from an included file"() {
        setup:
        projectDir.resolve('bnd.bnd').toFile().write 'Private-Package: org.springframework.*'

        when:
        buildScript.toFile().append """
            dependencies { compile "org.springframework:spring-instrument:4.0.6.RELEASE" }
            bundle { instruction "-include", "${projectDir}/bnd.bnd" }"""
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Private-Package: org.foo.bar,org.springframework.instrument'
    }

    @Issue(9)
    def "Supports Include-Resource header"() {
        def resource = 'test-resource.txt'

        setup:
        projectDir.resolve(resource).toFile().write 'this resource should be included'

        when:
        buildScript.toFile().append "\nbundle { instruction 'Include-Resource', '${projectDir}/${resource}' }"
        executeGradleCommand 'clean jar'

        then:
        jarContains resource
    }

    @Issue(9)
    def "Supports -includeresource directive"() {
        def resource = 'test-resource.txt'

        setup:
        projectDir.resolve(resource).toFile().write 'this resource should be included'

        when:
        buildScript.toFile().append "\nbundle { instruction '-includeresource', '${projectDir}/${resource}' }"
        executeGradleCommand 'clean jar'

        then:
        jarContains resource
    }

    private def executeGradleCommand(cmd) {
		def command = isWindows() ? "cmd /c " : ""
		command = command + "gradle $cmd -b $projectDir/build.gradle"
		
		def out = new StringBuilder();
		def err = new StringBuilder();
		
        def process = command.execute()
		process.waitForProcessOutput( out, err )

        stdout = out.toString()
        stderr = err.toString()

        assert process.exitValue() == 0: stderr
    }
	
	private def isWindows(){
		System.getProperty("os.name").toLowerCase() ==~ /win.*/ 
	}

    private def manifestContains(String line) {
        manifest =~ "(?m)^$line\$"
    }

    private def getManifest() {
        jarFile.getInputStream(new ZipEntry('META-INF/MANIFEST.MF')).text
    }

    private ZipFile getJarFile() {
        new ZipFile(projectDir.resolve(jarName ?: "build/libs/${projectDir.fileName}.jar").toFile())
    }

    private def jarContains(String entry) {
        jarFile.getEntry(entry) != null
    }
}