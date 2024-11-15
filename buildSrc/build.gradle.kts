
group = "ru.rusviper"
version = "0.0.2"

task("startAllServices") {
    dependsOn(":text-to-jira-backend:runFatJar")
    dependsOn(":text-to-jira-frontend:jsBrowserDevelopmentRun")
}