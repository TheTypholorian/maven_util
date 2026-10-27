package net.typho.maven_util.pom

import net.typho.maven_util.ArtifactName

/**
 * [Documentation](https://maven.apache.org/pom.html#dependencies)
 */
data class DependencyInfo(
    @JvmField
    val name: ArtifactName,
    /**
     * Default options:
     * - `compile` (default)
     * - `provided`
     * - `runtime`
     * - `test`
     * - `system`
     */
    @JvmField
    val scope: String,
    /**
     * Never null when [scope] is `system`, always null otherwise
     */
    @JvmField
    val systemPath: String?,
    @JvmField
    val optional: Boolean,
    @JvmField
    val exclusions: List<Exclusion>
) {
    data class Exclusion(
        @JvmField
        val groupId: String,
        @JvmField
        val artifactId: String
    )
}