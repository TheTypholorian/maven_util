package net.typho.maven_util

import java.net.URI
import java.nio.file.Path

fun interface DownloadListener {
    fun onDownload(
        repository: URI?,
        artifact: ArtifactCoords,
        to: Path
    )
}