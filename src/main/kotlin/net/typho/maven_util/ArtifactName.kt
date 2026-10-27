package net.typho.maven_util

import net.typho.data_util.impl.xml.XMLElement

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
     * The classifier (typically used to publish extra content that isn't needed in the main artifact), ex. `sources`, `javadoc`
     */
    @JvmField
    val classifier: String?,
    /**
     * The file type of the artifact (also known as the extension), ex. `jar`
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

    constructor(xml: XMLElement) : this(
        xml.getChild("groupId").asText().text,
        xml.getChild("artifactId").asText().text,
        xml.getChildren("version").singleOrNull()?.asText()?.text,
        xml.getChildren("classifier").singleOrNull()?.asText()?.text,
        xml.getChildren("type").singleOrNull()?.asText()?.text,
    )

    override fun toString() = listOf(
        groupId,
        artifactId,
        version ?: "",
        classifier ?: "",
        type ?: "jar"
    ).joinToString(separator = ":")
}
