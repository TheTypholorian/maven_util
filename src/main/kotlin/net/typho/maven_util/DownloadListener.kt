package net.typho.maven_util

import java.io.InputStream
import java.nio.file.Path

interface DownloadListener {
    fun onDownload(
        artifact: ArtifactPointer<InputStream>,
        to: Path
    )
}