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
    val properties: Map<String, String?>,
    @JvmField
    val dependencyManagement: DependencyManagement,
    @JvmField
    val dependencies: List<DependencyInfo>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<RawPOM> {
            field(RawPOM::parent, ArtifactCoords.CODEC.optional())
            field(RawPOM::properties, Codec.dynamicMap(Codec.STRING.optional()).optional(mapOf()))
            field(RawPOM::dependencyManagement, DependencyManagement.CODEC.optional(DependencyManagement(listOf())))
            field(RawPOM::dependencies, DependencyInfo.CODEC.listOf().optional(listOf()))
        }

        @JvmStatic
        internal fun String.template(properties: Map<String, String?>) = Regex("\\$\\{([^}]+)}").replace(this) { match ->
            val key = match.groupValues[1]
            properties[key] ?: match.value
        }
    }

    @JvmOverloads
    fun refine(lookup: Function<ArtifactCoords, RawPOM>? = null): RefinedPOM {
        fun overlayDependencies(management: DependencyManagement, dependencies: List<DependencyInfo>) = dependencies.map { dep ->
            management.dependencies.fold(dep) { accum, it ->
                if (it.groupId == dep.groupId && it.artifactId == dep.artifactId) {
                    accum.overlay(it)
                } else {
                    accum
                }
            }
        }

        var properties = properties.toMutableMap()
        var dependencyManagement = dependencyManagement
        val coords = if (parent == null) {
            ArtifactCoords(
                (groupId ?: throw NullPointerException("Dependency is missing a groupId")).template(properties),
                (artifactId ?: throw NullPointerException("Dependency is missing a artifactId")).template(properties),
                (version ?: throw NullPointerException("Dependency is missing a version")).template(properties),
                classifier?.template(properties),
                type?.template(properties)
            )
        } else {
            if (lookup == null) {
                throw IllegalStateException("Artifact has parent but no artifact lookup was passed")
            }

            val rawParent = lookup.apply(parent.copy(extension = "pom"))
            val parent = rawParent.refine(lookup)
            rawParent.properties.forEach(properties::putIfAbsent)
            dependencyManagement = DependencyManagement(rawParent.dependencyManagement.dependencies + dependencyManagement.dependencies)

            ArtifactCoords(
                (groupId ?: parent.coords.groupId).template(properties),
                (artifactId ?: parent.coords.artifactId).template(properties),
                (version ?: parent.coords.version).template(properties),
                (classifier ?: parent.coords.classifier)?.template(properties),
                (type ?: parent.coords.extension)?.template(properties)
            )
        }

        properties["project.groupId"] = coords.groupId
        properties["project.artifactId"] = coords.artifactId
        properties["project.version"] = coords.version
        properties = properties.mapValuesTo(mutableMapOf()) { (key, value) -> value?.template(properties) }

        return RefinedPOM(
            coords,
            overlayDependencies(dependencyManagement, dependencies).map { it.refine(properties) },
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

        fun refine(properties: Map<String, String?>) = RefinedPOM.DependencyInfo(
            ArtifactCoords(
                groupId.template(properties),
                artifactId.template(properties),
                (version ?: throw NullPointerException("Dependency $groupId:$artifactId is missing a version")).template(properties),
                classifier?.template(properties),
                type?.template(properties) ?: "jar"
            ),
            scope?.template(properties) ?: "compile",
            systemPath?.template(properties),
            optional ?: false,
            exclusions?.map { ArtifactName(it.groupId.template(properties), it.artifactId.template(properties)) } ?: listOf()
        )
    }
}