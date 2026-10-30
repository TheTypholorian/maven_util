package net.typho.maven_util

import net.typho.data_util.codec.Codec

data class ArtifactName(
    /**
     * The author of the artifact, in reverse DNS, ex. `net.typho`
     */
    @JvmField
    val groupId: String,
    /**
     * The name of the artifact, ex. `maven_util`
     */
    @JvmField
    val artifactId: String
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<ArtifactName>()
    }

    /**
     * Reads from the array, syntax: `{groupId}:{artifactId}`
     */
    constructor(coordinates: List<String>) : this(
        coordinates[0],
        coordinates[1]
    )

    /**
     * Parses the input coordinates, syntax: `{groupId}:{artifactId}`
     */
    constructor(coordinates: String) : this(coordinates.split(':').also {
        if (it.size != 2) {
            throw IllegalArgumentException("Coordinates must be in the syntax {groupId}:{artifactId}")
        }
    })

    override fun toString() = "$groupId:$artifactId"
}