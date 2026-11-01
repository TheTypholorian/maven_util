package net.typho.maven_util

import net.typho.data_util.codec.Codec
import java.util.function.Function

/**
 * [Documentation](https://maven.apache.org/pom.html)
 *
 * This object stores the raw .pom data, call [refine] to apply dependency management and inheritance
 */
data class RawPOM(
    @JvmField
    val parent: ArtifactCoords?,
    @JvmField
    val groupId: String?,
    @JvmField
    val artifactId: String?,
    @JvmField
    val version: String?,
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
        val CODEC = Codec.reflect<RawPOM> {
            field(RawPOM::parent, ArtifactCoords.CODEC.optional())
            field(RawPOM::dependencyManagement, DependencyManagement.CODEC.optional(DependencyManagement(listOf())))
            field(RawPOM::dependencies, DependencyInfo.CODEC.listOf().optional(listOf()))
        }
    }

    @JvmOverloads
    fun refine(lookup: Function<ArtifactCoords, RawPOM>? = null): RefinedPOM {
        fun overlayDependencies(management: DependencyManagement, dependencies: List<DependencyInfo>) = dependencies.map { dep ->
            management.dependencies.foldRight(dep) { accum, it ->
                if (it.groupId == dep.groupId && it.artifactId == dep.artifactId) {
                    accum.overlay(it)
                } else {
                    accum
                }
            }
        }

        parent?.let {
            if (lookup == null) {
                throw IllegalStateException("Artifact has parent but no artifact lookup was passed")
            }

            val rawParent = lookup.apply(it)
            val parent = rawParent.refine(lookup)

            return RefinedPOM(
                ArtifactCoords(
                    groupId ?: parent.coords.groupId,
                    artifactId ?: parent.coords.artifactId,
                    version ?: parent.coords.version,
                    classifier ?: parent.coords.classifier,
                    type ?: parent.coords.extension
                ),
                parent.dependencies + overlayDependencies(DependencyManagement(dependencyManagement.dependencies + rawParent.dependencyManagement.dependencies), dependencies).map { it.refine() },
            )
        }

        return RefinedPOM(
            ArtifactCoords(
                groupId ?: throw NullPointerException("Dependency is missing a groupId"),
                artifactId ?: throw NullPointerException("Dependency is missing a artifactId"),
                version ?: throw NullPointerException("Dependency is missing a version"),
                classifier,
                type
            ),
            overlayDependencies(dependencyManagement, dependencies).map { it.refine() },
        )
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
        val exclusions: List<ArtifactName>?
    ) {
        companion object {
            @JvmField
            val CODEC = Codec.reflect<DependencyInfo> {
                field(DependencyInfo::exclusions, ArtifactName.CODEC.listOf().optional())
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

        fun refine() = RefinedPOM.DependencyInfo(
            ArtifactCoords(
                groupId,
                artifactId,
                version ?: throw NullPointerException("Dependency $groupId:$artifactId is missing a version"),
                classifier,
                type ?: "jar"
            ),
            scope ?: "compile",
            systemPath,
            optional ?: false,
            exclusions ?: listOf()
        )
    }
}