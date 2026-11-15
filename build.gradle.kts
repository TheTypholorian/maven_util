plugins {
    kotlin("jvm") version "2.4.21"
    id("net.typho.typho_publish") version "1.0.5"
}

group = "net.typho"
version = "1.5.2"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:2.5.3")
    implementation("net.typho:parsing_util:2.0.3")
}

kotlin {
    jvmToolchain(8)
}