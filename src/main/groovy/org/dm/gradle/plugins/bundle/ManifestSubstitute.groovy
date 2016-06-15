package org.dm.gradle.plugins.bundle

import org.gradle.api.java.archives.Attributes
import org.gradle.api.java.archives.Manifest
import org.gradle.api.java.archives.internal.ManifestInternal
import org.gradle.api.java.archives.ManifestException
import org.gradle.api.java.archives.internal.DefaultManifest

import java.nio.charset.Charset

import static aQute.bnd.osgi.Constants.BND_LASTMODIFIED
import static org.dm.gradle.plugins.bundle.Objects.requireNonNull

class ManifestSubstitute implements ManifestInternal {
    // With accordance to manifest specification
    private Charset charset = Charset.forName('UTF-8')

    private final org.gradle.internal.Factory<JarBuilder> jarBuilderFactory
    private final Manifest wrapped

    ManifestSubstitute(org.gradle.internal.Factory<JarBuilder> jarBuilderFactory, Manifest wrapped) {
        this.jarBuilderFactory = requireNonNull(jarBuilderFactory)
        this.wrapped = wrapped ?: new DefaultManifest(null)
    }

    /* ManifestInternal implementation */

    @Override
    String getContentCharset() {
        return charset.toString()
    }

    @Override
    void setContentCharset(String name) {
        charset = Charset.forName(name);
    }

    @Override
    Manifest writeTo(OutputStream outputStream) {
        jarBuilderFactory.create().writeManifestTo(outputStream) { manifest ->
            manifest.mainAttributes.remove(new java.util.jar.Attributes.Name(BND_LASTMODIFIED))
        }
        return this
    }

    /* Manifest implementation */

    @Override
    Manifest writeTo(Writer writer) {
        jarBuilderFactory.create().writeManifestTo(new WriterToOutputStreamAdapter(writer, charset)) { manifest ->
            manifest.mainAttributes.remove(new java.util.jar.Attributes.Name(BND_LASTMODIFIED))
        }
        return this
    }

    /* Delegated methods, nothing interesting */

    @Override
    Attributes getAttributes() {
        return wrapped.attributes
    }

    @Override
    Map<String, Attributes> getSections() {
        return wrapped.sections
    }

    @Override
    Manifest attributes(Map<String, ?> attributes) throws ManifestException {
        return wrapped.attributes(attributes)
    }

    @Override
    Manifest attributes(Map<String, ?> attributes, String sectionName) throws ManifestException {
        return wrapped.attributes(attributes, sectionName)
    }

    @Override
    Manifest getEffectiveManifest() {
        return wrapped.effectiveManifest
    }

    @Override
    Manifest writeTo(Object path) {
        return wrapped.writeTo(path)
    }

    @Override
    Manifest from(Object... mergePath) {
        return wrapped.from(mergePath)
    }

    @Override
    Manifest from(Object mergePath, Closure closure) {
        return wrapped.from(mergePath, closure)
    }
}
