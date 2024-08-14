package ru.rusviper.plugins

import AppConfigReader
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.rusviper.data.WorkLogRow
import ru.rusviper.logic.JiraClient


data class JiraIssue(val issue: String)

data class WorkLogRecords(
    /** Записи в лог **/
    val workLogRows: List<WorkLogRow>
)

fun Application.configureTextToJira() {
    routing {
        get("/text/ping") {
            call.respond(HttpStatusCode.OK, "pong")
        }
        get("/text/show_issue") {
            val response = routeResponse("IA-11309")
            call.respond(response)
        }
        get("/text/worklog") {
            // показываем текущий лог по задаче(ам)
            val issueRequest = call.receive<JiraIssue>()
            val logRecords = getIssueLogRecords(issueRequest.issue)
            call.respond(HttpStatusCode.OK, logRecords)
        }
        post("/text/worklog") {
            // добавляем лог по задачам
            val workLogRecords = call.receive<WorkLogRecords>()
            sendWorkLogRecords(workLogRecords)
            call.respond(HttpStatusCode.OK, "Written to Jira ${workLogRecords.workLogRows.size} log records")
        }
    }
}

fun getIssueLogRecords(issue: String): WorkLogRecords {
    val appConfig = AppConfigReader.readConfig()
    val client = JiraClient(appConfig.app.jira)
    val logRecords = client.getIssueLogRecords(issue)
    return WorkLogRecords(logRecords)
}

/**
 * Sends work log records to the Jira server.
 *
 * @param workLogRecords the work log records to be sent
 */
fun sendWorkLogRecords(workLogRecords: WorkLogRecords) {
    val appConfig = AppConfigReader.readConfig()
    val client = JiraClient(appConfig.app.jira)
    workLogRecords.workLogRows.forEach {
        client.addWorkLog(it)
    }
}

/**
 * Retrieves the description of the issue with the given ID from the Jira server.
 *
 * @param issueId The ID of the issue to retrieve.
 * @return The description of the issue, or "no issue" if the issue does not have a description.
 */
fun routeResponse(issueId: String): String {
    val appConfig = AppConfigReader.readConfig()
    val client = JiraClient(appConfig.app.jira)
    val issue = client.getIssue(issueId)
    return issue.description?:"no issue"
}
