plugins {
    kotlin("jvm") version "1.9.21"
    application
}
application {
    mainClass.set("MainKt")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("mysql:mysql-connector-java:8.0.29")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}