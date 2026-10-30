package net.typho.maven_util

sealed interface LookupResult<R : Any> : AutoCloseable {
    val artifact: ArtifactCoords
    val value: R?

    fun orThrow(): R

    fun <N : Any> cast(): LookupResult<N>

    data class Success<R : Any>(
        override val artifact: ArtifactCoords,
        override val value: R
    ) : LookupResult<R> {
        override fun orThrow() = value

        override fun close() {
            (value as? AutoCloseable)?.close()
        }

        override fun toString(): String {
            return value.toString()
        }

        @Suppress("UNCHECKED_CAST")
        override fun <N : Any> cast(): LookupResult<N> {
            return Success(artifact, value as N)
        }
    }

    data class Error<R : Any>(
        override val artifact: ArtifactCoords,
        @JvmField
        val error: String
    ) : LookupResult<R> {
        override val value: R?
            get() = null

        override fun orThrow() = throw ArtifactLookupException(error)

        override fun close() {
        }

        override fun toString(): String {
            return error
        }

        @Suppress("UNCHECKED_CAST")
        override fun <N : Any> cast(): LookupResult<N> {
            return Error(artifact, error)
        }
    }
}