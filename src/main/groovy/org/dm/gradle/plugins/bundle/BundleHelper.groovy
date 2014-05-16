package org.dm.gradle.plugins.bundle

import org.gradle.api.GradleException

import java.util.regex.Matcher
import java.util.regex.Pattern

import static java.util.regex.Pattern.compile

/**
 * Copied from the old osgi plugin helper.
 */
enum BundleHelper {
    INSTANCE

    /**
     * Bundle-Version must match this pattern
     */
    private static final Pattern OSGI_VERSION_PATTERN = compile("[0-9]+(\\.[0-9]+(\\.[0-9]+(\\.[0-9A-Za-z_-]+)?)?)?")

    private static final Pattern ONLY_NUMBERS = compile("[0-9]+")
    private static final Pattern QUALIFIER = compile("[0-9A-Za-z_\\-]*")

    public String getVersion(String version) {
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
        StringBuilder buf = new StringBuilder();
        while (st.hasMoreTokens()) {
            String token = st.nextToken()
            buf.append(QUALIFIER.matcher(token).matches() ? token : '_')
        }
        buf
    }
}