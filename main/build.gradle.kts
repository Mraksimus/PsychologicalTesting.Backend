import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.ktor.plugin.features.DockerImageRegistry

plugins {

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.detekt)
    alias(libs.plugins.ksp)

    alias(libs.plugins.ktor)

    id("application")

}

repositories {
    mavenCentral()
    maven("https://repo.h4kt.dev/releases")
    maven("https://repo.h4kt.dev/snapshots")
}

dependencies {

    implementation(project(":common"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    detekt(libs.detekt.cli)
    detekt(libs.detekt.formatting)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.cors)
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
    ksp(libs.koin.compiler)

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

    implementation(libs.akkurate.core)
    implementation(libs.akkurate.ktor)
    ksp(libs.akkurate.compiler)

    implementation(libs.jbcrypt)

    testImplementation("org.jetbrains.kotlin:kotlin-test")

}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
}

// detekt {
//    config.setFrom("main/detekt.yml")
//    buildUponDefaultConfig = false
// }

application {
    mainClass = "ru.psychologicalTesting.main.AppKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

ktor {
    docker {

        localImageName = "psychological-testing-main"

        jreVersion = JavaVersion.VERSION_21

        imageTag = "latest"

        externalRegistry = DockerImageRegistry.externalRegistry(
            username = providers.environmentVariable("GITHUB_ACTOR"),
            password = providers.environmentVariable("GITHUB_TOKEN"),
            hostname = provider { "ghcr.io" },
            project = provider { "mraksimus/psychologicaltesting.backend" }
        )

    }
}

jib {
    container {
        mainClass = "ru.psychologicalTesting.main.AppKt"
    }
}

tasks.withType<ShadowJar> {
    isZip64 = true
    archiveFileName = "psychological-testing-all.jar"

    mergeServiceFiles()
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
