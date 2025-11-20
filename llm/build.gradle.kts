@file:OptIn(OpenApiPreview::class)

import io.ktor.plugin.OpenApiPreview
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
//    implementation(libs.ktor.server.openapi)
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

    implementation(libs.koog.ktor)
    implementation(libs.koog.agents)
    implementation(libs.koog.promtExecutorOllamaClient)

    testImplementation(kotlin("test"))

}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
    arg("KOIN_DEFAULT_MODULE", "false")
}

application {
    mainClass = "ru.psychologicalTesting.llm.AppKt"
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

ktor {

    docker {

        localImageName = "psychological-testing-llm"

        jreVersion = JavaVersion.VERSION_21

        imageTag = "latest"

        externalRegistry = DockerImageRegistry.externalRegistry(
            username = providers.environmentVariable("GITHUB_ACTOR"),
            password = providers.environmentVariable("GITHUB_TOKEN"),
            hostname = provider { "ghcr.io" },
            project = provider { "mraksimus/psychologicaltesting.backend/llm" }
        )

    }

    openApi {
        target = project.layout.projectDirectory.file("run/development/api.json")
        title = "Psychological testing LLM"
        description = "REST API для обработки текста через LLM"
        version = "1.0.0"
    }

}

jib {
    container {
        mainClass = "ru.psychologicalTesting.llm.AppKt"
    }
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
