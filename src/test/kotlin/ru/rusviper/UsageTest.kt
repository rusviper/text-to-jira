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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.asserter

class UsageTest {

    @Test
    fun testDoAction() = testApplication {
        application {
            configureTextToJira()
        }
        client.get("/text/ping").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("pong", bodyAsText())
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

    /**
     * Тестово добавляет одну запись в лог
     */
    @Ignore
    @Test
    fun testDoJira() = testApplication {
        val config = AppConfigReader.readConfig()
        JiraClient(config.app.jira).apply {
            addWorkLog(WorkLogRow(
                "IA-11618",
                1.5,
                "test comment",
                LocalDateTime.now()
            ))
        }
    }

    /** Ручной способ использования приложения **/
    @Ignore
    @Test
    fun testWriteRows() = testApplication {

        val confFile = Paths.get("/home/rusviper/bfg/Projects/text-to-jira/application-local.conf").toFile()
        // read file
        val config = AppConfigReader.readConfig(confFile)

        // check right config
        assertEquals("rvsuhih", config.app.jira.login)

        // input data
        val path = "/home/rusviper/bfg/Projects/text-to-jira/inputLog.txt"
        val fileString = Files.readString(Paths.get(path))

        // parse file
        val parseDayWorkLogs = WorkLogTextParser().parseDayWorkLogs(fileString)

        // check parsing
        assertEquals(33, parseDayWorkLogs.size)

        // publish to jira
        JiraClient(config.app.jira).apply {
            addWorkLogs(parseDayWorkLogs)
        }
    }
}