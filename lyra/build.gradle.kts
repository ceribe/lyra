plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.7.21"
    application
}

group = "com.kagamiapps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register("prepareKotlinBuildScriptModel"){}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
}