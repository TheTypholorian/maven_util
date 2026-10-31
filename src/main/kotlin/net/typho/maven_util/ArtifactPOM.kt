package net.typho.maven_util

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
    @JvmField
    val classifier: String?,
    /**
     * Defaults to `jar`
     */
    @JvmField
    val type: String?,
    @JvmField
    val dependencyManagement: DependencyManagement,
    @JvmField
    val dependencies: List<DependencyInfo>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<ArtifactPOM> {
            field(ArtifactPOM::dependencyManagement, DependencyManagement.CODEC.optional(DependencyManagement(listOf())))
            field(ArtifactPOM::dependencies, DependencyInfo.CODEC.listOf().optional(listOf()))
        }
    }

    /**
     * Returns the list of dependencies with values from dependency management applied
     */
    fun getOverlayedDependencies() = dependencies.map { dep ->
        val overlay = dependencyManagement.dependencies.firstOrNull { it.groupId == dep.groupId && it.artifactId == dep.artifactId }

        if (overlay == null) dep else dep.overlay(overlay)
    }

    data class DependencyManagement(
        @JvmField
        val dependencies: List<DependencyInfo>
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyManagement> {
                field(DependencyManagement::dependencies, DependencyInfo.CODEC.listOf())
            }
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
        @JvmField
        val version: String?,
        @JvmField
        val classifier: String?,
        /**
         * Defaults to `jar`
         */
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
        @JvmField
        val scope: String?,
        /**
         * Never null when [scope] is `system`, always null otherwise
         */
        @JvmField
        val systemPath: String?,
        /**
         * Defaults to false
         */
        @JvmField
        val optional: Boolean?,
        @JvmField
        val exclusions: List<Exclusion>?
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyInfo> {
                field(DependencyInfo::exclusions, Exclusion.CODEC.listOf().optional())
            }
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