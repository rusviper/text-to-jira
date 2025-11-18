plugins {
    `kotlin-dsl`
}
group = "ru.rusviper"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

task("startAllServices") {
    dependsOn(":text-to-jira-backend:runFatJar")
    dependsOn(":text-to-jira-frontend:jsBrowserDevelopmentRun")
}