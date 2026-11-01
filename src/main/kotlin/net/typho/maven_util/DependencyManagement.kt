package net.typho.maven_util

import net.typho.data_util.codec.Codec

data class DependencyManagement(
    @JvmField
    val dependencies: List<RawPOM.DependencyInfo>
) {
    companion object {
        @JvmField
        val CODEC = Codec.reflect<DependencyManagement> {
            field(DependencyManagement::dependencies, RawPOM.DependencyInfo.CODEC.listOf())
        }
    }
}