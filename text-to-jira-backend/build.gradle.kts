import ru.rusviper.gradle.Dependencies
import ru.rusviper.gradle.Versions

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val jira_client_version: String by project
val jira_client_fugue_version: String by project


plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "3.2.3"
    application
    kotlin("plugin.serialization") version "2.0.0"
}

group = "ru.rusviper"
version = "0.0.2"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://packages.atlassian.com/maven/repository/public")
    }
}

dependencies {
    implementation(platform("io.ktor:ktor-bom:3.2.3"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-serialization-jackson-jvm")

    // ktor clent
    implementation(Dependencies.ktorClientCore)
    implementation(Dependencies.ktorClientCIO)
    implementation(Dependencies.ktorClientContent)
    implementation("io.ktor:ktor-client-auth")
    implementation(Dependencies.ktorKotlinxSerialization)
    implementation(Dependencies.ktorKoin)
    implementation(Dependencies.ktorKoinSlf4j)

    implementation("org.json:json:20231013")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-client-logging")


    // jira client
    implementation("com.atlassian.jira:jira-rest-java-client-core:${jira_client_version}")
    implementation("io.atlassian.fugue:fugue:${jira_client_fugue_version}")



    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.7.1")

    // https://github.com/lightbend/config
    implementation("io.github.config4k:config4k:0.7.0") // ConfigFactory

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")



    // logging
    implementation("ch.qos.logback:logback-classic:${logback_version}")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("org.apache.logging.log4j:log4j-core:2.9.1")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("text-to-jira-backend")
        imageTag.set(version.toString())
        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                8082,
                8082,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}
