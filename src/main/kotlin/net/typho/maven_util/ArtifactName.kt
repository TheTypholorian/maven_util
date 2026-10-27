package net.typho.maven_util

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
    val artifactId: String,
    /**
     * The version of the artifact, ex. `1.0.0`
     */
    @JvmField
    val version: String?,
    /**
     * The classifier (typically used to publish extra content that isn't needed in the main artifact), empty string if none, ex. `sources`, `javadoc`
     */
    @JvmField
    val classifier: String?,
    /**
     * The file type of the artifact, ex. `jar`
     */
    @JvmField
    val type: String?
) {
    /**
     * Reads from the array, syntax: `{groupId}:{artifactId}[:{version}[:{classifier}[:{type}]]]`
     */
    constructor(coordinates: List<String>) : this(
        coordinates[0],
        coordinates[1],
        coordinates.getOrNull(2),
        coordinates.getOrNull(3),
        coordinates.getOrNull(4)
    )

    /**
     * Parses the input coordinates, syntax: `{groupId}:{artifactId}[:{version}[:{classifier}[:{type}]]]`
     */
    constructor(coordinates: String) : this(coordinates.split(':').also {
        if (it.size !in 2..5) {
            throw IllegalArgumentException("Coordinates must be in the syntax {groupId}:{artifactId}[:{version}[:{classifier}[:{type}]]], got $it")
        }
    })

    override fun toString() = listOf(
        groupId,
        artifactId,
        version ?: "",
        classifier ?: "",
        type ?: "jar"
    ).joinToString(separator = ":")
}
