package org.dm.gradle.plugins.bundle

class Objects {

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