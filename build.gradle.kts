import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val springBootVersion = "3.3.2"
val springDependencyManagementVersion = "1.1.6"
val lombokVersion = "1.18.32"
val openApiVersion = "2.5.0"
val testcontainersVersion = "1.20.1"

plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // --- Kotlin ---
    implementation(kotlin("stdlib-jdk8"))

    // --- Spring Boot core ---
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // --- Kafka ---
    implementation("org.springframework.kafka:spring-kafka")

    // --- DB / Migration ---
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // --- OpenAPI ---
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openApiVersion")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    // --- Tests ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // --- Testcontainers ---
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:kafka:$testcontainersVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}