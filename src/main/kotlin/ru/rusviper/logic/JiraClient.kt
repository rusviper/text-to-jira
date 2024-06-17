package ru.rusviper.logic

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.Comment
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.api.domain.input.WorklogInputBuilder
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import io.atlassian.util.concurrent.Promise
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import ru.rusviper.data.WorkLogRow
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.ceil


class JiraClient(val username: String, val password: String, val jiraUrl: String) {

    private val restClient: JiraRestClient

    private val requestTimeoutMs = 5000L

    val logger = Logger.getLogger(this.javaClass.name)

    init {
        restClient = AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(getJiraUri(), username, password)
    }

    private fun getJiraUri() = URI.create(jiraUrl)

    fun getIssue(issueId: String): Issue =
        restClient.issueClient.getIssue(issueId).get()

    fun addComment(issue: Issue, commentBody: String?): Promise<Void>? {
        return restClient.issueClient
            .addComment(issue.commentsUri, Comment.valueOf(commentBody))
    }

    fun addWorkLog(issue: Issue, commentBody: String, startDate: DateTime, hoursSpent: Double): Promise<Void> {
        val worklogInput = WorklogInputBuilder(issue.self).apply {
            setComment(commentBody)
            setStartDate(startDate)
            setMinutesSpent(ceil(hoursSpent * 12).toInt() * 5)    // округляем до 5 мин
        }.build()

        return restClient.issueClient
            .addWorklog(issue.worklogUri, worklogInput)
    }

    fun addWorkLog(wlRow: WorkLogRow): Promise<Void> {
        val issue = getIssue(wlRow.issue)
        val date = timeToYoda(wlRow.date)

        logger.log(Level.INFO) {
            "!@# Добавлена запись в лог: $wlRow"
        }
        return addWorkLog(issue, wlRow.comment, date, wlRow.durationHours)
    }

    private fun timeToYoda(time: LocalDateTime): DateTime {
        return DateTime(time.year, time.monthValue, time.dayOfMonth,
            time.hour, time.minute, time.second, time.nano / 1000000)
    }

    private fun yodaToTime(time: DateTime): ZonedDateTime {
        return ZonedDateTime.ofLocal(
            LocalDateTime.of(
                time.year,
                time.monthOfYear,
                time.dayOfMonth,
                time.hourOfDay,
                time.minuteOfHour,
                time.secondOfMinute,
                time.millisOfSecond * 1000000
            ),
            ZoneId.of(time.zone.id, ZoneId.SHORT_IDS),
            ZoneOffset.ofTotalSeconds(time.zone.getOffset(time) / 1000)
        )
    }

    fun addWorkLogs(rows: List<WorkLogRow>) {
        for (row in rows) {
            addWorkLog(row)
                // ожидаем завершения запроса
                .get(requestTimeoutMs, TimeUnit.MILLISECONDS)
        }
        logger.log(Level.INFO) {
            "!@# Залогированы следующие записи: ${rows.sortedBy { it.date }.joinToString(
                separator = "\n"
            )}"
        }
    }
}