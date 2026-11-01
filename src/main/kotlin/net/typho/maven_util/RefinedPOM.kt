package net.typho.maven_util

/**
 * [Documentation](https://maven.apache.org/pom.html)
 */
data class RefinedPOM(
    @JvmField
    val coords: ArtifactCoords,
    @JvmField
    val dependencies: List<DependencyInfo>
) {
    /**
     * [Documentation](https://maven.apache.org/pom.html#dependencies)
     */
    data class DependencyInfo(
        @JvmField
        val coords: ArtifactCoords,
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
        /**
         * Defaults to false
         */
        @JvmField
        val optional: Boolean,
        @JvmField
        val exclusions: List<ArtifactName>
    )
}