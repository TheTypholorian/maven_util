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
    fun lookup(artifact: ArtifactCoords, repositories: List<URI>): LookupResult<ArtifactPointer<InputStream>> {
        val path = artifact.toPath()
        val errors = repositories.map { repo ->
            try {
                return LookupResult.Success(ArtifactPointer(repo, repo.resolve(path).toURL().openStream()))
            } catch (e: IOException) {
                "Tried '$repo' ($e)"
            }
        }

        return LookupResult.Error("Cannot find artifact $artifact:\n" + errors.joinToString(separator = "\n\t"))
    }

    /**
     * Returned [InputStream] must be closed by the caller
     */
    fun cachedLookup(artifact: ArtifactCoords, cache: Path, repositories: List<URI>): LookupResult<ArtifactPointer<Path>> {
        val cached = cache.resolve(artifact.toPath())

        if (cached.exists()) {
            return LookupResult.Success(ArtifactPointer(null, cached))
        }

        val actual = lookup(artifact, repositories)
        val read = actual.value ?: return actual.cast()
        val path = cache.resolve(artifact.toPath())
        path.parent.createDirectories()

        read.artifact.use {
            path.outputStream().use { write ->
                read.artifact.copyTo(write)
            }
        }

        return LookupResult.Success(ArtifactPointer(read.repository, path))
    }

    /**
     * [input] must be closed by the caller
     */
    fun readPOM(input: InputStream) = XMLFormat.read(ArtifactPOM.CODEC, SerializedData(input.readBytes()))

    /**
     * Returned list contains the input artifacts
     */
    fun cachedLookupDependencies(artifacts: List<ArtifactCoords>, cache: Path, repositories: List<URI>) = lookupDependencies(artifacts) { cachedLookup(it, cache, repositories).map { ArtifactPointer(it.repository, it.artifact.inputStream()) } }

    /**
     * Returned list contains the input artifacts
     */
    fun lookupDependencies(artifacts: List<ArtifactCoords>, repositories: List<URI>) = lookupDependencies(artifacts) { lookup(it, repositories) }

    /**
     * Returned list contains the input artifacts
     */
    fun lookupDependencies(artifacts: List<ArtifactCoords>, lookup: Function<ArtifactCoords, LookupResult<ArtifactPointer<InputStream>>>): LookupResult<List<ArtifactPointer<ArtifactCoords>>> {
        val dependencies = mutableMapOf<ArtifactName, ArtifactPointer<ArtifactCoords>>()
        val processed = mutableSetOf<ArtifactName>()
        val queue = artifacts.toMutableList()

        while (queue.isNotEmpty()) {
            val queue1 = queue.toList()
            queue.clear()

            for (artifact in queue1) {
                val result = lookup.apply(artifact.copy(extension = "pom")).let { it.value ?: return it.cast() }
                val pom = readPOM(result.artifact)
                dependencies.putIfAbsent(artifact.name, ArtifactPointer(result.repository, artifact))

                for (dep in pom.getOverlayedDependencies()) {
                    if (processed.add(ArtifactName(dep.groupId, dep.artifactId))) {
                        queue.add(ArtifactCoords(
                            dep.groupId,
                            dep.artifactId,
                            dep.version ?: return LookupResult.Error("Dependency ${dep.groupId}:${dep.artifactId} from artifact $artifact is missing a version"),
                            dep.classifier,
                            dep.type
                        ))
                    }
                }
            }
        }

        return LookupResult.Success(dependencies.values.toList())
    }
}