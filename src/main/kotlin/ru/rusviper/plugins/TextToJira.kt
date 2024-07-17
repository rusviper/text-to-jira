package ru.rusviper.plugins

import AppConfigReader
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.rusviper.logic.JiraClient

fun Application.configureTextToJira() {
    routing {
        get("/text/ping") {
            call.respond(HttpStatusCode.OK, "pong")
        }
        get("/text/show_issue") {
            val response = routeResponse("IA-11309")
            call.respond(response)
        }
    }
}

fun routeResponse(issueId: String): String {
    val appConfig = AppConfigReader.readConfig()
    val client = JiraClient(appConfig.app.jira)
    val issue = client.getIssue(issueId)
    return issue.description?:"no issue"
}
