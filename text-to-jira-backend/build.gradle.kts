val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val jira_client_version: String by project
val jira_client_fugue_version: String by project


plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.10"
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
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-jackson-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // jira client
    implementation("com.atlassian.jira:jira-rest-java-client-core:$jira_client_version")
    implementation("io.atlassian.fugue:fugue:$jira_client_fugue_version")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.7.1")

    // https://github.com/lightbend/config
    implementation("io.github.config4k:config4k:0.7.0") // ConfigFactory

    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    implementation("io.ktor:ktor-server-cors:$ktor_version")

    // logging
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
