package ru.rusviper

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ru.rusviper.data.WorkLogRow
import ru.rusviper.logic.JiraClient
import ru.rusviper.logic.WorkLogTextParser
import ru.rusviper.plugins.configureTextToJira
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.asserter

class UsageTest {

    @Test
    fun testDoAction() = testApplication {
        application {
            configureTextToJira()
        }
        client.get("/text/show_issue").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testParseRow() = testApplication {
        val rowValue = "0.3 - 11615: ответы на комментарии в проектировании"
        val date = LocalDateTime.now()
        val row = WorkLogTextParser().parseWorkLogString(rowValue, date)

        asserter.apply {
            assertEquals("IA-11615", row.issue)
            assertEquals("ответы на комментарии в проектировании", row.comment)
            assertEquals(0.3, row.durationHours)
            assertEquals(date, row.date)
        }
    }

    //@Test
    fun testDoJira() = testApplication {
        JiraClient("rvsuhih", "Heckfy", "https://jira.bfg-soft.ru/").apply {
            addWorkLog(WorkLogRow(
                "IA-11618",
                1.5,
                "test comment",
                LocalDateTime.now()
            ))
        }
    }

    //@Test
    fun testWriteRow() = testApplication {
        val rowValue = ""
        val row = WorkLogTextParser().parseWorkLogString(rowValue, LocalDateTime.now())

        JiraClient("rvsuhih", "Heckfy", "https://jira.bfg-soft.ru/").apply {
            addWorkLog(row)
        }
    }

    //@Test
    fun testWriteRows() = testApplication {
        // read file
        val path = "/home/rusviper/bfg/Projects/text-to-jira/inputLog.txt"
        val fileString = Files.readString(Paths.get(path))

        // parse file
        val parseDayWorkLogs = WorkLogTextParser().parseDayWorkLogs(fileString)

        // publish to jira
        JiraClient("rvsuhih", "Heckfy", "https://jira.bfg-soft.ru/").apply {
            addWorkLogs(parseDayWorkLogs)
        }
    }
}