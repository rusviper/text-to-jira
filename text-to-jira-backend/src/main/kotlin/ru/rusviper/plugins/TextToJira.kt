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
import ru.rusviper.logic.WorkLogTextParser
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


data class JiraIssue(val issue: String)

data class WorkLogRecords(
    /** Записи в лог **/
    val workLogRows: List<WorkLogRow>
)

data class WorkLogCheckInputParameters(val expectedCount: Int?, val worklogText: String?)
data class WorkLogCheckResult(val rowsCount: Int)

data class GetJiraStatusResult(val status: String,
                               val ping: Long, val jiraLink: String,
                               val serverInfo: String, val serverTitle: String)


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

        post("/text/check") {
            // проверяем введённый текст лога
            val workLog = call.receive<WorkLogCheckInputParameters>()
            val logRecords = parseLogText(workLog.worklogText?:"")
            call.respond(HttpStatusCode.OK, WorkLogCheckResult(logRecords.size))
        }

        get("/jira/check") {
            // проверяем доступность жиры
            val jiraStatus = pingJira()
            call.respond(HttpStatusCode.OK, jiraStatus)
        }
    }
}

fun pingJira(): GetJiraStatusResult {
    // todo исключить повторное чтение конфигурации
    val appConfig = AppConfigReader.readConfig()
    val client = JiraClient(appConfig.app.jira)

    val startDate = LocalDateTime.now()
    val serverInfo = client.getServerInfo()
    val endTime = LocalDateTime.now()

    return GetJiraStatusResult(
        status = "OK",
        ping = ChronoUnit.MILLIS.between(startDate, endTime),
        jiraLink = appConfig.app.jira.jiraUrl!!,
        serverInfo = serverInfo.version,
        serverTitle = serverInfo.serverTitle,
    )
}

fun parseLogText(text: String): List<WorkLogRow> {
    return WorkLogTextParser().parseDayWorkLogs(text)
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
