package org.dm.gradle.plugins.bundle

import java.nio.charset.Charset

import static java.util.Objects.requireNonNull

final class WriterToOutputStreamAdapter extends OutputStream {
    private final Writer writer
    private final Charset charset

    WriterToOutputStreamAdapter(Writer writer, Charset charset) {
        this.writer = requireNonNull(writer)
        this.charset = requireNonNull(charset)
    }

    @Override
    void write(int b) throws IOException {
        writer.write(b)
    }

    @Override
    void write(byte[] b) throws IOException {
        writer.write(toCharArray(b))
    }

    private char[] toCharArray(byte[] b) {
        new String(b, charset).toCharArray()
    }

    @Override
    void write(byte[] b, int off, int len) throws IOException {
        writer.write(toCharArray(b), off, len)
    }

    @Override
    void flush() throws IOException {
        writer.flush()
    }

    @Override
    void close() throws IOException {
        writer.close()
    }
}
