package org.dm.gradle.plugins.bundle

import aQute.bnd.osgi.Builder
import aQute.bnd.osgi.Jar
import org.gradle.api.Nullable
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.jar.Manifest

import static aQute.bnd.osgi.Constants.INCLUDERESOURCE
import static aQute.bnd.osgi.Constants.INCLUDE_RESOURCE
import static org.dm.gradle.plugins.bundle.Objects.requireNonNull

/**
 * A jar generator, which is basically a wrapper
 * around bnd {@link Builder}.
 */
class JarBuilder {
    private final static Logger LOG = Logging.getLogger(JarBuilder.class)

    protected final Builder builder
    protected Jar jar

    JarBuilder() {
        this(new Builder())
    }

    JarBuilder(Builder builder) {
        this.builder = requireNonNull(builder)
    }

    JarBuilder withVersion(String version) {
        if (builder.bundleVersion == null) {
            builder.bundleVersion = version
        }
        this
    }

    JarBuilder withName(String name) {
        if (builder.bundleSymbolicName == null) {
            builder.bundleSymbolicName = name
        }
        this
    }

    JarBuilder withResources(files) {
        addToResources files
        this
    }

    private addToResources(files) {
        if (files == []) {
            return
        }
        def resources = files.join(',')
        def existingResources = builder.getProperty(INCLUDERESOURCE) ?: builder.getProperty(INCLUDE_RESOURCE)
        if (existingResources != null) {
            resources = existingResources + ',' + resources
        }
        builder.setProperty INCLUDERESOURCE, resources
    }

    JarBuilder withClasspath(files) {
        builder.setClasspath files as File[]
        this
    }

    JarBuilder withBase(base) {
        builder.setBase base
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

    void writeManifestTo(OutputStream outputStream, @Nullable Closure c) {
        build()

        def manifest = jar.manifest.clone() as Manifest
        if (c != null) {
            c manifest
        }
        Jar.writeManifest manifest, outputStream
    }

    void writeManifestTo(OutputStream outputStream) {
        writeManifestTo outputStream, null
    }

    private void build() {
        if (jar != null) {
            return
        }
        traceClasspath()
        jar = builder.build()
        traceErrors()
    }

    void writeJarTo(File output) {
        build()

        output.getParentFile().mkdirs()
        jar.write output
    }

    private void traceClasspath() {
        LOG.debug "The Builder is about to generate a jar using classpath: ${builder.classpath.collect { it.source }}"
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