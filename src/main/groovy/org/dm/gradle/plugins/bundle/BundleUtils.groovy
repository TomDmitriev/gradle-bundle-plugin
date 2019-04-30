package org.dm.gradle.plugins.bundle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.tasks.bundling.Jar

import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipException
import java.util.zip.ZipFile

import static java.util.regex.Pattern.compile

/**
 * A set of bundle utils.
 */
final class BundleUtils {
    /**
     * Bundle-Version must match this pattern
     */
    private static final Pattern OSGI_VERSION_PATTERN = compile("[0-9]+(\\.[0-9]+(\\.[0-9]+(\\.[0-9A-Za-z_-]+)?)?)?")
    private static final Pattern ONLY_NUMBERS = compile("[0-9]+")
    private static final Pattern QUALIFIER = compile("[0-9A-Za-z_\\-]*")

    private BundleUtils() {
        throw new AssertionError()
    }

    static def getProperties(Jar jarTask) {
        def attrs = attributes(jarTask.manifest)
        def entries = jarTask.project.bundle.instructions
        def props = (attrs + entries).collectEntries { key, value ->
            [key as String, String.valueOf(value)]
        }
        if (jarTask.project.bundle.passProjectProperties) {
            props << jarTask.project.properties.findAll {
                it.value instanceof String
            }
        }
        props
    }

    //Visible for testing
    static def attributes(Manifest manifest) {
        def effManifest = manifest.effectiveManifest
        (effManifest.sections.values() + effManifest.attributes).inject([:]) { allAttrs, attrs ->
            allAttrs << attrs
        }.findAll {
            it.key != 'Manifest-Version'
        }
    }

    static File[] getClasspath(Jar jarTask) {
        def project = jarTask.project
        def excludeDependencies = project.bundle.excludeDependencies
        def buildPathConfigurations = project.bundle.buildPathConfigurations
        buildPathConfigurations.inject([]) { classpath, configurationName ->
            def configuration = project.configurations.findByName(configurationName)
            if (configuration) {
                configuration = configuration.copyRecursive().setTransitive(project.bundle.includeTransitiveDependencies)
                excludeDependencies.each {
                    configuration.exclude(it)
                }
                classpath += configuration.files.findAll { File file ->
                    if (!file.exists()) {
                        return false
                    }
                    if (file.directory) {
                        return true
                    }
                    try {
                        new ZipFile(file).withCloseable { ZipFile zip ->
                            zip.entries() // make sure it is a valid zip file and not a pom
                        }
                    } catch (ZipException e) {
                        return false
                    }
                    return true
                }
            }
            classpath
        }
    }

    static File getBase(Project project) {
        project.buildFile.getParentFile()
    }

    static File[] getSources(Project project) {
        project.sourceSets.main.allSource.srcDirs.findAll {
            it.exists()
        }
    }

    static File[] getResources(Jar jarTask) {
        def paths = []
        jarTask.source.visit { entry ->
            if ('MANIFEST.MF' != entry.relativePath.pathString) {
                def path = entry.file.parentFile.canonicalPath
                if (paths.every { !path.startsWith(it) }) {
                    paths << path + File.separator
                }
            }
        }
        paths.unique().collect { path -> new File(path as String) }
    }

    static boolean getFailOnError(Jar jarTask) {
        jarTask.project.bundle.failOnError
    }

    static File getOutput(Jar jarTask) {
        jarTask.archivePath
    }

    static String getVersion(Project project) {
        def projectVersion = project.version
        projectVersion == Project.DEFAULT_VERSION ? '0' : version(projectVersion as String)
    }

    //Visible for testing
    /**
     * Gets an OSGI compliant version from the given version.
     * <p/>
     * The method is copied from the old osgi plugin helper.
     */
    static String version(String version) {
        /* If it's already OSGi compliant don't touch it */
        final Matcher m = OSGI_VERSION_PATTERN.matcher(version)
        if (m.matches()) {
            return version
        }

        int group = 0
        boolean groupToken = true
        String[] groups = ['0', '0', '0', '']
        StringTokenizer st = new StringTokenizer(version, ",./;'?:\\|=+-_*&^%\$#@!~", true)
        while (st.hasMoreTokens()) {
            String token = st.nextToken()
            if (groupToken) {
                if (group < 3) {
                    if (ONLY_NUMBERS.matcher(token).matches()) {
                        groups[group++] = token
                        groupToken = false
                    } else {
                        // if not a number, i.e. 2.ABD
                        groups[3] = token + fillQualifier(st)
                    }
                } else {
                    // Last group; what ever is left take that replace all characters that are not alphanum or '_' or '-'
                    groups[3] = token + fillQualifier(st)
                }
            } else {
                // If a delimiter; if dot, swap to groupToken, otherwise the rest belongs in qualifier.
                if (".".equals(token)) {
                    groupToken = true
                } else {
                    groups[3] = fillQualifier(st)
                }
            }
        }
        String ver = "${groups[0]}.${groups[1]}.${groups[2]}"
        String result = groups[3].length() > 0 ? "$ver.${groups[3]}" : ver
        if (!OSGI_VERSION_PATTERN.matcher(result).matches()) {
            throw new GradleException('Bundle plugin unable to convert version to a compliant version')
        }
        result
    }

    private static String fillQualifier(StringTokenizer st) {
        StringBuilder buf = new StringBuilder()
        while (st.hasMoreTokens()) {
            String token = st.nextToken()
            buf.append(QUALIFIER.matcher(token).matches() ? token : '_')
        }
        buf
    }

    /**
     * Get the symbolic name as group + "." + archivesBaseName, with the following exceptions
     * <ul>
     * <li>
     * if group has only one section (no dots) and archivesBaseName is not null then the first package
     * name with classes is returned. eg. commons-logging:commons-logging -> org.apache.commons.logging
     * </li>
     * <li>
     * if archivesBaseName is equal to last section of group then group is returned.
     * eg. org.gradle:gradle -> org.gradle
     * </li>
     * <li>
     * if archivesBaseName starts with last section of group that portion is removed.
     * eg. org.gradle:gradle-core -> org.gradle.core
     * </li>
     * <li>
     * if archivesBaseName starts with the full group, the archivesBaseName is return,
     * e.g. org.gradle:org.gradle.core -> org.gradle.core
     * </li>
     * </ul>
     * The method is copied from the old osgi plugin helper.
     *
     * @param project The project being processed.
     *
     * @return Returns the SymbolicName that should be used for the bundle.
     */
    static String getBundleSymbolicName(Project project) {
        String group = project.getGroup().toString()
        String archiveBaseName = project.convention.
                getPlugin(BasePluginConvention.class).archivesBaseName
        if (archiveBaseName.startsWith(group)) {
            return archiveBaseName
        }
        int i = group.lastIndexOf('.')
        String lastSection = group.substring(++i)
        if (archiveBaseName.equals(lastSection)) {
            return group
        }
        if (archiveBaseName.startsWith(lastSection)) {
            String artifactId = archiveBaseName.substring(lastSection.length())
            return Character.isLetterOrDigit(artifactId.charAt(0)) ?
                    bundleSymbolicName(group, artifactId) :
                    bundleSymbolicName(group, artifactId.substring(1))
        }
        bundleSymbolicName(group, archiveBaseName)
    }

    private static String bundleSymbolicName(String groupId, String artifactId) {
        return "$groupId.$artifactId"
    }
}
