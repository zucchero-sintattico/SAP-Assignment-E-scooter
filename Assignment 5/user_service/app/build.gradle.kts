/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.5/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    id("org.springframework.boot") version "3.2.1"
}
apply(plugin = "io.spring.dependency-management")

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation(libs.junit)

    // This dependency is used by the application.
    implementation(libs.guava)

    //vertx
    implementation("io.vertx:vertx-core:4.5.1")
    implementation("io.vertx:vertx-web:4.5.1")
    implementation("io.vertx:vertx-circuit-breaker:4.5.2")

    // MongoDB
    implementation("io.vertx:vertx-mongo-client:4.5.1")

    // Aggiungi SLF4J API
    //implementation("org.slf4j:slf4j-api:2.0.12")

    // Aggiungi un'implementazione di logging, ad esempio Logback
    //implementation("ch.qos.logback:logback-classic:1.5.6")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    /*sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20*/
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    // Define the main class for the application.
    mainClass.set("user_service.UserServiceApp")
}