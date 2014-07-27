package org.dm.gradle.plugins.bundle

final class Objects {
    private Objects() {
        throw new AssertionError()
    }

	def static <T> T requireNonNull(T t) {
		requireNonNull(t, null)
	}

	def static <T> T requireNonNull(T t, String message) {
		if (t == null) {
			throw new NullPointerException(message)
		}

		t
	}

}