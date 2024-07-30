plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(projects.danmaku.api)
    api(projects.utils.logging)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.json)

    api(libs.ktor.client.core)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}