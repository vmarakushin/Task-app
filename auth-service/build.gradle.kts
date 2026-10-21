plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

val lombokVersion = "1.18.32"
val openApiVersion = "2.5.0"
val testcontainersVersion = "2.0.5"
val keycloakAdminVersion = "26.0.9"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":event-contracts"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.keycloak:keycloak-admin-client:${keycloakAdminVersion}")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openApiVersion")

    implementation("org.springframework.boot:spring-boot-starter-actuator")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test"){
        exclude(group = "org.testcontainers")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers-kafka:${testcontainersVersion}")
}
