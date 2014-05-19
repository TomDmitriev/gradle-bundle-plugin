package org.dm.gradle.plugins.bundle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.tasks.bundling.Jar

import java.util.regex.Matcher
import java.util.regex.Pattern

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

    static JarBuilder newJarBuilder(Jar jarTask) {
        jarTask.project.bundle.jarBuilderFactory.create()
    }

    static def getProperties(Jar jarTask) {
        attributes(jarTask.manifest) + jarTask.project.bundle.instructions.collectEntries { key, value ->
            [key, value as String]
        }
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

    static File[] getClasspath(Project project) {
        project.configurations.runtime.files
    }

    static File[] getSources(Project project) {
        project.sourceSets.main.allSource.srcDirs.findAll {
            it.exists()
        }
    }

    static File[] getResources(Project project) {
        def output = project.sourceSets.main.output
        [output.classesDir, output.resourcesDir].findAll {
            it.exists()
        }
    }

    static def getTrace(Jar jarTask) {
        jarTask.project.bundle.trace
    }

    static def getOutput(Jar jarTask) {
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
