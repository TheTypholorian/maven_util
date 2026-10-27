plugins {
    kotlin("jvm") version "2.4.21"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:1.4.2")
    implementation("org.semver4j:semver4j:6.0.0")
}