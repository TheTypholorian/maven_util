plugins {
    kotlin("jvm") version "2.4.21"
    id("net.typho.typho_publish") version "1.0.4"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:2.3.6")
}