package org.dm.gradle.plugins.bundle

import spock.lang.Shared
import spock.lang.IgnoreRest
import spock.lang.Specification

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * A set of integration tests. The routine of each test is
 * to execute gradle tasks against a test project and look
 * at the result (e.g. contents of the produced jar).
 */
class BundlePluginIntegrationSpec extends Specification {
    @Shared
    File projectDir = createTempDir()
    @Shared
    File buildScript = resolve(projectDir, 'build.gradle')
    @Shared
    String gradleHome = System.getProperty("gradle.home")
    String stdout, stderr, jarName
    Logger LOG = Logging.getLogger(BundlePluginIntegrationSpec.class)

    void setupSpec() {
        if (!gradleHome) {
            throw new IllegalStateException('System property "gradle.home" is not set')
        }
        createSources()
    }

    private void createSources() {
        javaSrc.mkdirs()
        copyFromResources('TestActivator.java', 'org/foo/bar/TestActivator.java')
        resolve(javaSrc, 'More.java').write 'package org.foo.bar;\n class More {}'
    }

    private File getJavaSrc() {
        resolve(projectDir, 'src/main/java/org/foo/bar')
    }

    private copyFromResources(String name) {
        resolve(javaSrc, name).write getClass().classLoader.getResource(name).text
    }

    private copyFromResources(String name, String path) {
        resolve(javaSrc, name).write getClass().classLoader.getResource(path).text
    }

    private copyToProject(String name) {
        resolve(projectDir, name).write getClass().classLoader.getResource(name).text
    }

    void setup() {
        buildScript.write getClass().classLoader.getResource('build.test').text
    }

    void cleanupSpec() {
        projectDir.deleteDir()
    }

    def "Jar task is executed while build"() {
        when:
        executeGradleCommand 'clean build'

        then:
        stdout =~ /(?m)^:jar$/
    }

    def "Uses project version as 'Bundle-Version' by default"() {
        when:
        buildScript.append '\nversion = "1.0.2"'
        executeGradleCommand 'clean jar'
        jarName = "build/libs/${projectDir.name}-1.0.2.jar"

        then:
        manifestContains 'Bundle-Version: 1.0.2'
    }

    def "Overwrites project version using 'Bundle-Version' instruction"() {
        when:
        buildScript.append '\nversion = "1.0.2"\nbundle { instructions << ["Bundle-Version": "5.0"] }'
        executeGradleCommand 'clean jar'
        jarName = "build/libs/${projectDir.name}-1.0.2.jar"

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
        buildScript.append '\njar { manifest { attributes("Built-By": "abc") } }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Built-By: abc'
    }

    def "Overwrites jar manifest values"() {
        when:
        buildScript.append '\njar { manifest { attributes("Built-By": "abc") } }\nbundle { instructions << ["Built-By": "xyz"] }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Built-By: xyz'
    }

    def "Uses baseName and extension defined in jar task"() {
        when:
        buildScript.append '\njar { baseName = "xyz"\nextension = "baz" }'
        executeGradleCommand 'clean jar'

        then:
        resolve(projectDir, 'build/libs/xyz.baz').exists()
    }

    def "Ignores unknown attributes"() {
        when:
        buildScript.append '\nbundle { instructions << ["junk": "xyz"] }'
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
        def resources = resolve(projectDir, 'src/main/resources/org/foo/bar')
        resources.mkdirs()
        resolve(resources, 'dummy.txt').write 'abc'

        when:
        executeGradleCommand 'clean jar'

        then:
        jarContains 'org/foo/bar/dummy.txt'

        cleanup:
        resources.deleteDir()
    }

    def "Includes project sources if instructed"() {
        when:
        buildScript.append '\nbundle { instructions << ["-sources": true] }'
        executeGradleCommand 'clean jar'

        then:
        jarContains 'OSGI-OPT/src/org/foo/bar/TestActivator.java'
        jarContains 'OSGI-OPT/src/org/foo/bar/More.java'
    }

    def "Supports old OSGI plugin instruction format"() {
        when:
        buildScript.append '\nbundle { instruction "Built-By", "ab", "c"\ninstruction "Built-By", "x", "y", "z" }'
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
        buildScript.append '\nbundle { instructions << ["Bundle-Activator": "org.foo.bar.NotExistingActivator"] }'
        executeGradleCommand 'clean jar'

        then:
        stdout =~ /(?m)^BUILD SUCCESSFUL$/
        stderr =~ /Bundle-Activator not found/
    }

    def "Can trace bnd build process"() {
        when:
        buildScript.append '\nbundle { trace = true }'
        executeGradleCommand 'clean jar'

        then:
        stderr =~ /(?m)^# build$/
    }

    @Issue(1)
    def "Saves manifest under build/tmp"() {
        when:
        executeGradleCommand 'clean jar'

        then:
        resolve(projectDir, 'build/tmp/jar/MANIFEST.MF').text == manifest.replaceAll('(?m)^Bnd-LastModified: \\d+$\r\n', '')
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
        buildScript.append '\nbundle { instructions << ["Built-By": "xyz"] }'
        executeGradleCommand 'jar'

        then:
        stdout =~ /(?m)^:jar$/
    }

    @Issue(8)
    def "Uses instructions (Private-Package) from an included file"() {
        setup:
        resolve(projectDir, 'bnd.bnd').write 'Private-Package: org.springframework.*'

        when:
        buildScript.append """
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
        resolve(projectDir, resource).write 'this resource should be included'

        when:
        buildScript.append "\nbundle { instruction 'Include-Resource', '${projectDir}/${resource}' }"
        executeGradleCommand 'clean jar'

        then:
        jarContains resource
    }

    @Issue(9)
    def "Supports -includeresource directive"() {
        def resource = 'test-resource.txt'

        setup:
        resolve(projectDir, resource).write 'this resource should be included'

        when:
        buildScript.append "\nbundle { instruction '-includeresource', '${projectDir}/${resource}' }"
        executeGradleCommand 'clean jar'

        then:
        jarContains resource
    }

    @Issue(22)
    def "-include instruction expects baseDir to be correct"() {
        setup:
        copyToProject('bnd.bnd')

        when:
        buildScript.append '\nbundle { instructions << ["-include": "bnd.bnd"] }'
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Bundle-Description: Bundle Description Test'
    }

    @Issue(13)
    def "Supports -dsannotations directive"() {
        setup:
        copyFromResources('TestComponent.java', 'org/foo/bar/TestComponent.java')

        when:
        buildScript.append """
            dependencies { compile 'org.osgi:org.osgi.compendium:5.0.0' }
            bundle { instructions << ["-dsannotations": "*"] }"""
        executeGradleCommand 'clean jar'

        then:
        manifestContains 'Service-Component: OSGI-INF/org.foo.bar.TestComponent.xml'
        jarContains 'OSGI-INF/org.foo.bar.TestComponent.xml'
    }

    def "jar task actions contain only a bundle generator action"() {
        when:
        buildScript.append "task actionscheck { doLast { println jar.actions.size() + \" \" + jar.actions[0].@action.getClass().getSimpleName() } }"
        executeGradleCommand 'actionscheck'

        then:
        stdout =~ /1 BundleGenerator/
    }

    private static File createTempDir() {
        def temp = resolve(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString())
        temp.mkdirs()
        temp
    }

    private static File resolve(File dir, String path) {
        new File(dir, path)
    }

    private static File resolve(String dir, String path) {
        new File(dir, path)
    }

    private def executeGradleCommand(cmd) {
		def command = isWindows() ? "cmd /c " : ""
		command += "${gradleHome}/bin/gradle $cmd -b $projectDir/build.gradle"
		
		def out = new StringBuilder()
		def err = new StringBuilder()
		
        def process = command.execute()
		process.waitForProcessOutput(out, err)

        stdout = out.toString()
        stderr = err.toString()

        LOG.info stdout
        LOG.error stderr

        assert process.exitValue() == 0: stderr
    }
	
	private static def isWindows() {
		System.getProperty("os.name").toLowerCase() ==~ /win.*/ 
	}

    private def manifestContains(String line) {
        manifest =~ "(?m)^$line\$"
    }

    private def getManifest() {
        jarFile.getInputStream(new ZipEntry('META-INF/MANIFEST.MF')).text
    }

    private ZipFile getJarFile() {
        new ZipFile(resolve(projectDir, jarName ?: "build/libs/${projectDir.name}.jar"))
    }

    private def jarContains(String entry) {
        jarFile.getEntry(entry) != null
    }
}