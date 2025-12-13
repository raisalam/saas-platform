plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}


group = "com.saas.platform.gateway"
version = "1.0.0"
description = "Gateway application for microservices"


repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2025.1.0"
dependencies {
    implementation ("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")
    implementation ("com.fasterxml.jackson.module:jackson-module-afterburner")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")  // latest stable in 2025
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.test {
    useJUnitPlatform()
}