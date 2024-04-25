package ru.rusviper.plugins

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.rusviper.logic.JiraClient

fun Application.configureTextToJira() {

    routing {
        get("/text/show_issue") {
            val response = routeResponse()
            call.respond(response)
        }
    }
}

fun routeResponse(): String {
    val client = JiraClient("rvsuhih", "Heckfy", "https://jira.bfg-soft.ru/")
    val issue = client.getIssue("IA-11309")
    return issue.description?:"no issue"
}
