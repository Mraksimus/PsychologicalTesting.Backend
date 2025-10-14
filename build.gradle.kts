plugins {

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)

    alias(libs.plugins.ktor)

    id("application")

}

group = "ru.psychologicalTesting"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.h4kt.dev/releases")
    maven("https://repo.h4kt.dev/snapshots")
}

dependencies {

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    detekt(libs.detekt.cli)
    detekt(libs.detekt.formatting)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.swagger)
    implementation(libs.ktor.server.contentNegotiation)


    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.java)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.contentNegotiation)


    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.docs)

    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.annotations)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.exposed.migration)
    implementation(libs.exposed.kotlin.datetime)

    implementation(libs.hikaricp)
    implementation(libs.postgres)
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)

    implementation(libs.h2)

    ksp(libs.koin.compiler)

    testImplementation("org.jetbrains.kotlin:kotlin-test")

}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
}

detekt {
    config.setFrom("detekt.yml")
    buildUponDefaultConfig = false
}

application {
    mainClass = "ru.psychologicalTesting.main.AppKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

tasks {

    test {
        useJUnitPlatform()
    }

    getByName<JavaExec>("run") {
        args = listOf("-config=application.conf")
        workingDir = File("run/development")
    }

}
