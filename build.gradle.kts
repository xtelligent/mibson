plugins {
    kotlin("jvm") version "1.8.21"
    id("maven-publish")
    application
}

group = "com.xtelligent"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
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

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            /** Configure path of your package repository on Github
             *  Replace GITHUB_USERID with your/organisation Github userID and REPOSITORY with the repository name on GitHub
             */
            url = uri("https://maven.pkg.github.com/xtelligent/mibson") // Github Package
            credentials {
                //Fetch these details from the properties file or from Environment variables
                username = System.getenv("USERNAME")
                password = System.getenv("TOKEN")
            }
        }
    }
}