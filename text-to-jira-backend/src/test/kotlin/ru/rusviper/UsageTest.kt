package ru.rusviper

import AppConfigReader
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import ru.rusviper.data.WorkLogRow
import ru.rusviper.logic.JiraClient
import ru.rusviper.logic.JiraHttpClient
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
    //@Ignore
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
        assertEquals(182, parseDayWorkLogs.size)

        // publish to jira
        JiraClient(config.app.jira).apply {
            addWorkLogs(parseDayWorkLogs)
        }
    }

    /**
     * Данная логика модифицирует год записей о работе в жира.
     * Проводится поиск задач, где есть worklog определённого пользователя за определённый период.
     * Для найденных записей изменяется год записи с 2025 на 2024.
     */
    @Ignore
    @Test
    fun testModifyRows() {

        val confFile = Paths.get("/home/rusviper/bfg/Projects/text-to-jira/application-local.conf").toFile()
        // read file
        val config = AppConfigReader.readConfig(confFile)

        // check right config
        assertEquals("rvsuhih", config.app.jira.login)

        runBlocking {
            val manager = JiraHttpClient(
                jiraUrl = config.app.jira.jiraUrl!!,
                username = config.app.jira.login!!,
                password = config.app.jira.password!!,
                // тестируем сначала, если true - модификация не проводится
                fakeUpdate = true
            )

            if (!manager.testConnection())
                throw RuntimeException("Нет соединения")

            try {
                // Вариант 1: Простой поиск с пагинацией
                val searchResult = manager.searchIssues(
                    jql = """worklogAuthor = "john.doe" AND worklogDate >= "2025-11-21" """,
                    fields = listOf("key", "summary", "status"),
                    maxResults = 50
                )

                println("Найдено задач: ${searchResult.total}")
                searchResult.issues.forEach { issue ->
                    println("  - ${issue.key}: ${issue.fields?.summary}")
                }

                // Вариант 2: Получить все задачи автоматически с пагинацией
                val allIssues = manager.getAllIssues(
                    jql = """worklogAuthor = "john.doe" AND worklogDate >= "2025-11-21" AND worklogDate <= "2025-12-31" """,
                    fields = listOf("key", "summary")
                )

                println("Всего задач с пагинацией: ${allIssues.size}")

                // Вариант 3: Получить worklog для конкретной задачи
                val worklogs = manager.getWorklogs("IA-123")
                println("Worklog в задаче IA-123:")
                worklogs.worklogs.forEach { worklog ->
                    println("  - ID: ${worklog.id}")
                    println("    Автор: ${worklog.author.displayName}")
                    println("    Дата: ${worklog.started}")
                    println("    Время: ${worklog.timeSpentSeconds}s")
                    println("    Комментарий: ${worklog.comment}")
                }

                // Вариант 4: Полная миграция worklog
                val result = manager.fixWorklogYearTyped(
                    targetUsername = "testUser",
                    wrongYearStart = LocalDateTime.of(2025, 11, 21, 0, 0),
                    wrongYearEnd = LocalDateTime.of(2025, 12, 31, 23, 59),
                    correctYear = 2024
                )

                println("\n--- Результаты миграции ---")
                println("Обновлено записей: ${result.totalUpdated}")
                println("Обработано задач: ${result.processedIssues.size}")
                if (result.errors.isNotEmpty()) {
                    println("\nОшибки:")
                    result.errors.forEach { println("  - $it") }
                }
                println("\nОбработанные задачи:")
                result.processedIssues.forEach { println("  - $it") }

            } finally {
                manager.close()
            }
        }
    }



}