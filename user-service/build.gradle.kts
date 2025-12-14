plugins {
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    java
}

group = "com.saas.platform.user"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    mavenLocal()      // ← REQUIRED to load your local lib
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }

    maven {
        url = uri("https://maven.pkg.github.com/raisalam/saas-libs")
        credentials {
            username = (project.findProperty("gpr.user") ?: "") as String?
            password = (project.findProperty("gpr.token") ?: "") as String?
        }
    }
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-clients:8.1.1-ce")


    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Use saas-platfor library
    //implementation("com.saas.platform:saas-db:1.0.1")
    //implementation("com.saas.platform:saas-common:1.0.2")
      implementation("com.saas.platform:saas-db:1.0.1")
      implementation("com.saas.platform:saas-common:1.0.7")

    implementation("com.mysql:mysql-connector-j")
}
