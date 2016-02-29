package org.dm.gradle.plugins.bundle

import aQute.bnd.osgi.Builder
import aQute.bnd.osgi.Jar
import org.gradle.api.Nullable
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.util.jar.Manifest

import static aQute.bnd.osgi.Constants.INCLUDERESOURCE
import static aQute.bnd.osgi.Constants.INCLUDE_RESOURCE

/**
 * A jar generator, which is basically a wrapper
 * around bnd {@link Builder}.
 */
class JarBuilder {
    private final static Logger LOG = Logging.getLogger(JarBuilder.class)

    private String version
    private String name
    private def base
    private def resources
    private def classpath
    private def sourcepath
    private def properties
    private def trace

    JarBuilder withVersion(String version) {
        LOG.debug "Setting version {}", version
        this.version = version
        this
    }

    JarBuilder withName(String name) {
        LOG.debug "Setting name {}", name
        this.name = name
        this
    }

    JarBuilder withResources(resources) {
        LOG.debug "Setting resources {}", resources
        this.resources = resources
        this
    }

    JarBuilder withClasspath(classpath) {
        LOG.debug "Setting classpath {}", classpath
        this.classpath = classpath
        this
    }

    JarBuilder withBase(base) {
        LOG.debug "Setting base {}", base
        this.base = base
        this
    }

    JarBuilder withSourcepath(sourcepath) {
        LOG.debug "Setting source path {}", sourcepath
        this.sourcepath = sourcepath
        this
    }

    JarBuilder withProperties(properties) {
        LOG.debug "Setting properties {}", properties
        this.properties = properties
        this
    }

    JarBuilder withTrace(trace) {
        LOG.debug "Setting trace {}", trace
        this.trace = trace
        this
    }

    void writeManifestTo(OutputStream outputStream, @Nullable Closure c) {
        def builder = build()

        def manifest = builder.jar.manifest.clone() as Manifest
        if (c != null) {
            c manifest
        }
        try {
            Jar.writeManifest manifest, outputStream
        } finally {
            outputStream.close()
            closeBuilder(builder)
        }
    }

    private static void closeBuilder(Builder builder) {
        if (builder != null) {
            try {
                builder.close();
            } catch (IOException e) {
                LOG.warning("Caught exception during builder.close(): " + e);
            }
        }
    }

    void writeManifestTo(OutputStream outputStream) {
        writeManifestTo outputStream, null
    }

    private def build() {
        def builder = new Builder()
        if (builder.bundleVersion == null) {
            builder.bundleVersion = version
        }

        if (builder.bundleSymbolicName == null) {
            builder.bundleSymbolicName = name
        }

        builder.trace = trace
        builder.base = base
        builder.properties = properties as Properties
        builder.sourcepath = sourcepath as File[]
        builder.setClasspath classpath as File[]
        builder.addClasspath resources as Collection<File>
        addToResources builder, resources

        traceClasspath(builder)
        builder.build()
        traceErrors(builder)
        builder
    }

    private static addToResources(builder, files) {
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

    void writeJarTo(File output) {
        def builder = null
        try {
            builder = build()

            output.getParentFile().mkdirs()
            builder.jar.write output
        } finally {
            closeBuilder(builder)
        }
    }

    private static void traceClasspath(builder) {
        LOG.debug "The Builder is about to generate a jar using classpath: ${builder.classpath.collect { it.source }}"
    }

    private static void traceErrors(builder) {
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