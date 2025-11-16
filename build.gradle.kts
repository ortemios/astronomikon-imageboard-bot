plugins {
    kotlin("jvm") version "2.2.21"
    id("com.google.devtools.ksp") version "2.3.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.6")
    // tg bot
    implementation("eu.vendeli:telegram-bot:8.4.1")
    ksp("eu.vendeli:ksp:8.4.1")
    // db
    implementation("org.xerial:sqlite-jdbc:3.51.0.0")
    implementation("org.flywaydb:flyway-core:9.22.3")
    // http
    implementation("io.ktor:ktor-client-okhttp:3.3.2")
    // logging
    implementation("org.slf4j:slf4j-simple:2.1.0-alpha1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}