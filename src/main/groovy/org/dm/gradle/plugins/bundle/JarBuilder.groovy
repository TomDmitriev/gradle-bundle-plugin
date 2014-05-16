package org.dm.gradle.plugins.bundle

import aQute.bnd.osgi.Builder
import aQute.bnd.osgi.Jar
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static aQute.bnd.osgi.Constants.INCLUDERESOURCE
import static java.nio.file.Files.createDirectories as createDirs

class JarBuilder {
    private final static Logger LOG = Logging.getLogger(JarBuilder.class)

    protected final Builder builder

    JarBuilder() {
        builder = new Builder()
    }

    JarBuilder withVersion(String version) {
        if (builder.bundleVersion == null) {
            builder.bundleVersion = version
        }
        this
    }

    JarBuilder withResources(files) {
        builder.setProperty INCLUDERESOURCE, files.join(',')
        builder.addClasspath files as Collection<File>
        this
    }

    JarBuilder withClasspath(files) {
        builder.setClasspath files as File[]
        this
    }

    JarBuilder withSourcepath(files) {
        builder.sourcepath = files as File[]
        this
    }

    JarBuilder withProperties(properties) {
        builder.properties = properties
        this
    }

    JarBuilder withTrace(trace) {
        builder.trace = trace
        this
    }

    void writeTo(File output) {
        traceClasspath()
        Jar jar = builder.build()
        traceErrors()

        createDirs output.toPath().parent
        jar.write output
    }

    private void traceClasspath() {
        LOG.debug "The Builder is about to produce a jar using classpath: ${builder.classpath.collect { it.source }}"
    }

    private void traceErrors() {
        def errors = builder.errors
        if (!errors.isEmpty()) {
            LOG.error errors as String
        }

        def warnings = builder.warnings
        if (!warnings.isEmpty()) {
            LOG.warn warnings as String
        }
    }
}