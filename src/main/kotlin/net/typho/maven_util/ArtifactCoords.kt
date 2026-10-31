package net.typho.maven_util

import net.typho.data_util.codec.Codec

data class ArtifactCoords(
    /**
     * The author of the artifact, in reverse DNS, ex. `net.typho`
     */
    @JvmField
    val groupId: String,
    /**
     * The name of the artifact, ex. `maven_util`
     */
    @JvmField
    val artifactId: String,
    /**
     * The version of the artifact, ex. `1.0.0`
     */
    @JvmField
    val version: String,
    /**
     * The classifier (typically used to publish extra content that isn't needed in the main artifact), ex. `sources`, `javadoc`
     */
    @JvmField
    val classifier: String? = null,
    /**
     * The file type of the artifact (also known as the extension), ex. `jar`
     *
     * Defaults to `jar`
     */
    @JvmField
    val extension: String? = null
) {
    val name: ArtifactName
        get() = ArtifactName(groupId, artifactId)

    companion object {
        @JvmField
        val CODEC = Codec.reflect<ArtifactCoords>()
    }

    /**
     * Reads from the array, syntax: `{groupId}:{artifactId}:{version}[:{classifier}[:{extension}]]`
     */
    constructor(coordinates: List<String>) : this(
        coordinates[0],
        coordinates[1],
        coordinates[2],
        coordinates.getOrNull(3),
        coordinates.getOrNull(4)
    )

    /**
     * Parses the input coordinates, syntax: `{groupId}:{artifactId}:{version}[:{classifier}[:{extension}]]`
     */
    constructor(coordinates: String) : this(coordinates.split(':').also {
        if (it.size !in 3..5) {
            throw IllegalArgumentException("Coordinates must be in the syntax {groupId}:{artifactId}[:{version}[:{classifier}[:{extension}]]], got $it")
        }
    })

    override fun toString() = listOf(
        groupId,
        artifactId,
        version,
        classifier ?: "",
        extension ?: "jar"
    ).joinToString(separator = ":")

    fun toPath() = listOf(
        groupId.replace('.', '/'),
        artifactId,
        version,
        if (classifier == null) {
            "$artifactId-$version.${extension ?: "jar"}"
        } else {
            "$artifactId-$version-$classifier.${extension ?: "jar"}"
        }
    ).joinToString(separator = "/")
}
