package net.typho.maven_util

import net.typho.data_util.SerializedData
import net.typho.data_util.xml.XMLFormat
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.util.function.Function
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object MavenUtil {
    @JvmField
    val MAVEN_LOCAL = Path(System.getProperty("user.home")).resolve(".m2/repository/")
    @JvmField
    val MAVEN_CENTRAL = URI("https://repo1.maven.org/maven2/")
    @JvmField
    val TYPHO_NET = URI("https://typho.net/maven/")

    /**
     * Returned [InputStream] must be closed by the caller
     */
    fun lookup(artifact: ArtifactCoords, repositories: List<URI>): LookupResult<InputStream> {
        val path = artifact.toPath()
        val errors = repositories.map { repo ->
            try {
                return LookupResult.Success(artifact, repo.resolve(path).toURL().openStream())
            } catch (e: IOException) {
                "\tTried '$repo' ($e)"
            }
        }

        return LookupResult.Error(artifact, "Cannot find artifact $artifact:\n" + errors.joinToString(separator = "\n"))
    }

    /**
     * Returned [InputStream] must be closed by the caller
     */
    fun cachedLookup(artifact: ArtifactCoords, cache: Path, repositories: List<URI>): LookupResult<Path> {
        val cached = cache.resolve(artifact.toPath())

        if (cached.exists()) {
            return LookupResult.Success(artifact, cached)
        }

        val actual = lookup(artifact, repositories)
        val read = actual.value ?: return actual.cast()
        val path = cache.resolve(artifact.toPath())
        path.parent.createDirectories()

        read.use {
            path.outputStream().use { write ->
                read.copyTo(write)
            }
        }

        return LookupResult.Success(artifact, path)
    }

    /**
     * [input] must be closed by the caller
     */
    fun readPOM(input: InputStream) = XMLFormat.read(ArtifactPOM.CODEC, SerializedData(input.readBytes()))

    fun cachedLookupDependencies(artifact: ArtifactCoords, cache: Path, repositories: List<URI>) = lookupDependencies(artifact) { cachedLookup(it, cache, repositories).map { it.inputStream() } }

    fun lookupDependencies(artifact: ArtifactCoords, repositories: List<URI>) = lookupDependencies(artifact) { lookup(it, repositories) }

    fun lookupDependencies(artifact: ArtifactCoords, lookup: Function<ArtifactCoords, LookupResult<InputStream>>): LookupResult<List<ArtifactCoords>> {
        val dependencies = mutableMapOf<ArtifactName, ArtifactCoords>()
        val queue = mutableListOf(artifact)

        while (queue.isNotEmpty()) {
            val process = queue.toList()
            queue.clear()

            for (artifact in process) {
                val result = lookup.apply(artifact.copy(extension = "pom"))
                val pom = (result.value ?: return result.cast()).use { readPOM(it) }

                for (dep in pom.getOverlayedDependencies()) {
                    val coords = ArtifactCoords(
                        dep.groupId,
                        dep.artifactId,
                        dep.version ?: return LookupResult.Error(artifact, "Dependency ${dep.groupId}:${dep.artifactId} from artifact $artifact is missing a version"),
                        dep.classifier,
                        dep.type
                    )
                    dependencies.putIfAbsent(coords.name, coords)
                    queue.add(coords)
                }
            }
        }

        return LookupResult.Success(artifact, dependencies.values.toList())
    }
}