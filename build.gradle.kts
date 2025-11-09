plugins {
    kotlin("jvm") version "2.2.21"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.danilopianini:khttp:1.6.3")
    implementation("eu.vendeli:telegram-bot:8.4.1")
    ksp("eu.vendeli:ksp:8.4.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}