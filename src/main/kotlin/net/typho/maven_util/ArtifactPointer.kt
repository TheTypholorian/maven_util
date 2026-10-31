package net.typho.maven_util

import java.net.URI

data class ArtifactPointer<A : Any>(
    @JvmField
    val repository: URI?,
    @JvmField
    val artifact: A
)
