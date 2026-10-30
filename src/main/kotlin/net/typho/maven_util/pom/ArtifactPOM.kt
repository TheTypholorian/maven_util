package net.typho.maven_util.pom

import net.typho.data_util.anno.FieldCodec
import net.typho.data_util.codec.Codec

/**
 * [Documentation](https://maven.apache.org/pom.html)
 */
data class ArtifactPOM(
    @JvmField
    val groupId: String,
    @JvmField
    val artifactId: String,
    @JvmField
    val version: String,
    @FieldCodec("Codec.STRING.optional()")
    @JvmField
    val classifier: String?,
    /**
     * Defaults to `jar`
     */
    @FieldCodec("Codec.STRING.optional()")
    @JvmField
    val type: String?,
    @FieldCodec("ArtifactPOM.DependencyManagement.CODEC.optional(ArtifactPOM.DependencyManagement(listOf()))")
    @JvmField
    val dependencyManagement: DependencyManagement,
    @FieldCodec("ArtifactPOM.DependencyInfo.CODEC.listOf().optional(listOf())")
    @JvmField
    val dependencies: List<DependencyInfo>
) {
    /**
     * Returns the list of dependencies with values from dependency management applied
     */
    fun getOverlayedDependencies() = dependencies.map { dep ->
        val overlay = dependencyManagement.dependencies.firstOrNull { it.groupId == dep.groupId && it.artifactId == dep.artifactId }

        if (overlay == null) dep else dep.overlay(overlay)
    }

    companion object {
        @JvmField
        val CODEC = Codec.reflect<ArtifactPOM>()
    }

    data class DependencyManagement(
        @FieldCodec("ArtifactPOM.DependencyInfo.CODEC.listOf().optional(listOf())")
        @JvmField
        val dependencies: List<DependencyInfo>
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyManagement>()
        }
    }

    /**
     * [Documentation](https://maven.apache.org/pom.html#dependencies)
     */
    data class DependencyInfo(
        @JvmField
        val groupId: String,
        @JvmField
        val artifactId: String,
        @FieldCodec("Codec.STRING.optional()")
        @JvmField
        val version: String?,
        @FieldCodec("Codec.STRING.optional()")
        @JvmField
        val classifier: String?,
        /**
         * Defaults to `jar`
         */
        @FieldCodec("Codec.STRING.optional()")
        @JvmField
        val type: String?,
        /**
         * Default options:
         * - `compile` (default)
         * - `provided`
         * - `runtime`
         * - `test`
         * - `system`
         */
        @FieldCodec("Codec.STRING.optional()")
        @JvmField
        val scope: String?,
        /**
         * Never null when [scope] is `system`, always null otherwise
         */
        @FieldCodec("Codec.STRING.optional()")
        @JvmField
        val systemPath: String?,
        /**
         * Defaults to false
         */
        @FieldCodec("Codec.BOOL.optional()")
        @JvmField
        val optional: Boolean?,
        @FieldCodec("DependencyInfo.Exclusion.CODEC.listOf().optional()")
        @JvmField
        val exclusions: List<Exclusion>?
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyInfo>()
        }

        fun overlay(info: DependencyInfo) = copy(
            version = version ?: info.version,
            classifier = classifier ?: info.classifier,
            type = type ?: info.type,
            scope = scope ?: info.scope,
            systemPath = systemPath ?: info.systemPath,
            optional = optional ?: info.optional,
            exclusions = exclusions ?: info.exclusions
        )

        data class Exclusion(
            @JvmField
            val groupId: String,
            @JvmField
            val artifactId: String
        ) {
            companion object {
                @JvmField
                val CODEC = Codec.reflect<Exclusion>()
            }
        }
    }
}