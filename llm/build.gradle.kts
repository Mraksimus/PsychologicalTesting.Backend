plugins {
    kotlin("jvm")
}

group = "ru.psychologicalTesting"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation(project(":common"))

    testImplementation(kotlin("test"))

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
