plugins {

    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.powerAssert) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ksp) apply false

    alias(libs.plugins.ktor) apply false

}

subprojects {

    group = "ru.psychologicalTesting"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.h4kt.dev/releases")
    }

}
