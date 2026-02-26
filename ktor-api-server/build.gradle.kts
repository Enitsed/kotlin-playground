plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

group = "com.example"
version = "1.0.0"

application {
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-netty:2.3.6")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Logging
    implementation("io.ktor:ktor-server-call-logging:2.3.6")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Testing
    testImplementation("io.ktor:ktor-server-tests:2.3.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.21")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`

    // Set environment based on project property or KTOR_ENV env var
    val env = project.findProperty("env")?.toString() ?: System.getenv("KTOR_ENV") ?: "dev"
    environment("KTOR_ENV", env)

    // Allow passing environment from command line: ./gradlew run -Penv=prod
    doFirst {
        println("Running Ktor with environment: $env")
    }
}

tasks.test {
    useJUnitPlatform()
}
