plugins {
    kotlin("jvm") version "1.8.21"
    id("maven-publish")
    application
}

group = "io.xtelligent"
version = System.getenv("RELEASE_VERSION")

repositories {
    mavenCentral()
    maven {
        url = uri("https://raw.github.com/bnese/maven-repo-mibble/master/")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.percederberg.mibble:mibble-parser:2.10.1")
    implementation("com.google.code.gson:gson:2.8.9")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/xtelligent/mibson")
            credentials {
                //Fetch these details from the properties file or from Environment variables
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}