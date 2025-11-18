package ru.rusviper.logic

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Клиент к jira, используется http-api.
 * Использует API v2, но может быть адаптирован к v3.
 **/
class JiraHttpClient(
    private val jiraUrl: String,
    private val username: String,
    private val password: String,
    private val fakeUpdate: Boolean = true
) {
    private val logger = Logger.getLogger(this.javaClass.name)

    /** Версия API Jira - v2 **/
    private val jiraRestPath = "/rest/api/2"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Флаг для отслеживания статуса аутентификации
    private var isAuthenticated = false

    // Хранилище cookies, которое мы будем использовать
    private val cookieStorage = AcceptAllCookiesStorage()

    // настраиваем сервер
    private val client = HttpClient(CIO) {
        // Включаем поддержку cookies
        install(HttpCookies) {
            // Хранилище cookies в памяти
            storage = cookieStorage
        }
//        install(Auth) {
//            basic {
//                credentials {
//                    BasicAuthCredentials(username = username, password = password)
//                }
//            }
//        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
        install(ContentNegotiation) {
            json(json)
        }
        // Добавляем обработку ошибок и редиректов
        expectSuccess = true

        // Настраиваем таймауты
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 60000
        }


    }


    // region auth

    /**
     * Аутентификация в Jira через REST API и получение session cookie
     */
    private suspend fun authenticate(): Boolean {
        return try {
            logger.info("Starting authentication for user: $username")

            val authUrl = "$jiraUrl/rest/auth/1/session"

            val response: HttpResponse = client.post(authUrl) {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(
                    """
                    {
                        "username": "$username",
                        "password": "$password"
                    }
                    """.trimIndent()
                )
            }

            if (response.status.isSuccess()) {
                val authResponse: AuthResponse = response.body()
                logger.info("Authentication successful for user: ${authResponse.session?.name}")
                isAuthenticated = true
                true
            } else {
                val errorBody = response.body<String>()
                logger.severe("Authentication failed: ${response.status}, body: $errorBody")
                false
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Authentication error", e)
            false
        }
    }



    /**
     * Проверка аутентификации и повторная аутентификация при необходимости
     */
    private suspend fun ensureAuthenticated() {
        if (!isAuthenticated) {
            val success = authenticate()
            if (!success) {
                throw RuntimeException("Authentication failed. Check username and password.")
            }
        }

        // Упрощаем проверку сессии - если аутентификация прошла, считаем что сессия валидна
        // Детальную проверку делаем только при проблемах
        if (!checkSession()) {
            logger.warning("Session may be expired, trying to reauthenticate...")
            isAuthenticated = false
            val success = authenticate()
            if (!success) {
                throw RuntimeException("Reauthentication failed")
            }
        }
    }

    /**
     * Метод для проверки текущей сессии
     */
    suspend fun checkSession(): Boolean {
        return try {
            val response: HttpResponse = client.get("$jiraUrl/rest/auth/1/session") {
                header("Accept", "application/json")
            }

            if (response.status.isSuccess()) {
                // Пробуем разные форматы ответа
                try {
                    // Сначала пробуем как AuthResponse (для совместимости)
                    val authResponse: AuthResponse = response.body()
                    if (authResponse.session != null) {
                        logger.info("Session is active for user: ${authResponse.session.name}")
                        true
                    } else {
                        // Если session null, пробуем другой формат
                        val sessionCheck: SessionCheckResponse = response.body()
                        val username = response.headers["X-AUSERNAME"]
                        logger.info("Session is active for user: $username, name=${sessionCheck.name}")
                        true
                    }
                } catch (e: Exception) {
                    // Если не удалось десериализовать, проверяем по заголовкам
                    val username = response.headers["X-AUSERNAME"]
                    if (username != null && username != "anonymous") {
                        logger.info("Session is active for user: $username (from headers)")
                        true
                    } else {
                        logger.warning("Session check failed: no valid user in response")
                        false
                    }
                }
            } else {
                logger.warning("Session check failed: ${response.status}")
                isAuthenticated = false
                false
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Session check error", e)
            isAuthenticated = false
            false
        }
    }

    /**
     * Выход из системы
     */
    suspend fun logout() {
        try {
            val response: HttpResponse = client.delete("$jiraUrl/rest/auth/1/session") {
                header("Accept", "application/json")
            }

            if (response.status.isSuccess()) {
                logger.info("Successfully logged out")
            } else {
                logger.warning("Logout request returned: ${response.status}")
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Error during logout", e)
        } finally {
            isAuthenticated = false
            // Мы не очищаем cookies, так при следующей аутентификации они будут заменены
        }
    }


    // endregion auth

    //

    suspend fun testConnection(): Boolean {

        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        return try {
            val response: HttpResponse = client.get("$jiraUrl$jiraRestPath/myself") {
                header("Accept", "application/json")
            }

            if (response.status.isSuccess()) {
                logger.info("Connection test successful")
                true
            } else {
                logger.warning("Connection test failed: ${response.status}")
                false
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Connection test failed", e)
            false
        }
    }



    /**
     * Поиск задач через JQL с типизированным ответом
     */
    suspend fun searchIssues(
        jql: String,
        fields: List<String> = listOf("key", "summary", "worklog"),
        startAt: Int = 0,
        maxResults: Int = 50
    ): SearchResponse {
        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        val encodedJql = URLEncoder.encode(jql, "UTF-8")
        val fieldsList = fields.joinToString(",")
        val url =
            "$jiraUrl$jiraRestPath/search?jql=$encodedJql&startAt=$startAt&maxResults=$maxResults&fields=$fieldsList"

        return try {
            logger.info("Making request to: $url")

            val response: HttpResponse = client.get(url) {
                header("Accept", "application/json")
                header("Content-Type", "application/json")
            }

            // Проверяем статус ответа
            if (!response.status.isSuccess()) {

                // Если получили 401 - Unauthorized, сбрасываем аутентификацию
                if (response.status == HttpStatusCode.Unauthorized) {
                    isAuthenticated = false
                    logger.warning("Received 401 Unauthorized, reauthenticating...")
                    return searchIssues(jql, fields, startAt, maxResults)
                }

                // Проверяем, нет ли в ошибке указания на неверный endpoint
                if (response.status == HttpStatusCode.NotFound) {
                    throw RuntimeException("API endpoint not found (404). This Jira version (9.4.1) may not support the requested endpoint. URL: $url")
                }

                val errorBody = response.body<String>()
                logger.severe("Request failed with status: ${response.status}, body: $errorBody")
                throw RuntimeException("Request failed: ${response.status}")
            }

            // Проверяем Content-Type
            val contentType = response.contentType()?.toString()
            if (contentType?.contains("application/json") != true) {
                val bodyPreview = response.body<String>().take(500)
                logger.severe("Expected JSON but got: $contentType, body: $bodyPreview")

                // Если получили HTML вместо JSON, вероятно проблема с аутентификацией
                if (contentType?.contains("text/html") == true) {
                    isAuthenticated = false // Сбрасываем флаг аутентификации
                    throw RuntimeException("Received HTML instead of JSON. Authentication may have expired.")
                }

                throw RuntimeException("Expected JSON response but got: $contentType")
            }

            val responseBody: SearchResponse = response.body()
            logger.info("Search returned ${responseBody.issues.size} issues (total: ${responseBody.total})")
            responseBody
        } catch (e: ClientRequestException) {
            // Обработка HTTP ошибок
            if (e.response.status == HttpStatusCode.Unauthorized) {
                isAuthenticated = false
                logger.warning("Authentication expired, retrying...")
                // Пробуем переаутентифицироваться и повторить запрос
                return searchIssues(jql, fields, startAt, maxResults)
            }
            logger.log(Level.SEVERE, "HTTP error during search", e)
            throw RuntimeException("Search failed: ${e.message}")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to search issues", e)

            // Проверяем, не связана ли ошибка с аутентификацией
            if (e.message?.contains("authentication", ignoreCase = true) == true ||
                e.message?.contains("unauthorized", ignoreCase = true) == true) {
                isAuthenticated = false
            }

            throw RuntimeException("Search failed: ${e.message}", e)
        }
    }

    /**
     * Получить все задачи по JQL с автоматической пагинацией
     */
    suspend fun getAllIssues(
        jql: String,
        fields: List<String> = listOf("key", "summary", "worklog")
    ): List<Issue> {
        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        val allIssues = mutableListOf<Issue>()
        var startAt = 0
        val maxResults = 100

        try {
            do {
                val response = searchIssues(
                    jql = jql,
                    fields = fields,
                    startAt = startAt,
                    maxResults = maxResults
                )

                allIssues.addAll(response.issues)
                logger.info("Retrieved ${allIssues.size} of ${response.total} issues")

                startAt += maxResults
                if (startAt >= response.total) break
            } while (true)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error fetching all issues", e)
            throw e
        }

        return allIssues
    }

    /**
     * Получить worklog для конкретной задачи с типизацией
     */
    suspend fun getWorklogs(
        issueKey: String,
        startAt: Int = 0,
        maxResults: Int = 1000
    ): WorklogCollection {
        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        val url = "$jiraUrl$jiraRestPath/issue/$issueKey/worklog?startAt=$startAt&maxResults=$maxResults"

        return try {
            val response: WorklogCollection = client.get(url).body()
            logger.info("Issue $issueKey has ${response.worklogs.size} worklogs (total: ${response.total})")
            response
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to get worklogs for $issueKey", e)
            throw RuntimeException("Failed to get worklogs: ${e.message}")
        }
    }

    /**
     * Обновить worklog с типизацией
     */
    suspend fun updateWorklog(
        issueKey: String,
        worklog: Worklog,
        newStartDate: LocalDateTime,
        timeSpentSeconds: Int,
        comment: String? = null
    ) {
        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        val worklogId = worklog.id
        val url = "$jiraUrl$jiraRestPath/issue/$issueKey/worklog/$worklogId"

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val startedStr = newStartDate
            .atZone(java.time.ZoneId.systemDefault())
            .format(formatter)

        val body = buildJsonString(startedStr, timeSpentSeconds, comment)

        try {
            val response: HttpResponse = client.put(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            if (!response.status.isSuccess()) {
                logger.log(Level.SEVERE, "Failed to update worklog: ${response.status}")
                throw RuntimeException("Failed to update worklog: ${response.status}")
            }

            logger.log(Level.INFO, "Successfully updated worklog $worklogId in $issueKey")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error updating worklog $worklogId", e)
            throw RuntimeException("Failed to update worklog: ${e.message}")
        }
    }

    /**
     * Удалить worklog
     */
    suspend fun deleteWorklog(issueKey: String, worklogId: String) {
        // Проверяем аутентификацию перед запросом
        ensureAuthenticated()

        val url = "$jiraUrl$jiraRestPath/issue/$issueKey/worklog/$worklogId"

        try {
            val response: HttpResponse = client.delete(url)

            if (!response.status.isSuccess()) {
                logger.log(Level.SEVERE, "Failed to delete worklog: ${response.status}")
                throw RuntimeException("Failed to delete worklog: ${response.status}")
            }

            logger.log(Level.INFO, "Successfully deleted worklog $worklogId from $issueKey")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error deleting worklog $worklogId", e)
            throw RuntimeException("Failed to delete worklog: ${e.message}")
        }
    }

    /**
     * Найти и исправить worklog с неправильным годом (типизированная версия)
     */
    suspend fun fixWorklogYearTyped(
        targetUsername: String,
        wrongYearStart: LocalDateTime,
        wrongYearEnd: LocalDateTime,
        correctYear: Int
    ): WorklogFixResult {
        val result = WorklogFixResult()

        try {
            // 1. Поиск задач через JQL
            val jql = buildJqlForWorklog(targetUsername, wrongYearStart, wrongYearEnd)
            logger.info("Searching issues with JQL: $jql")

            val issues = getAllIssues(
                jql = jql,
                fields = listOf("key")
            )

            logger.info("Found ${issues.size} issues")

            // 2. Для каждой задачи обработать worklog
            for (issue in issues) {
                try {
                    val updated = processIssueWorklogsTyped(
                        issue.key,
                        targetUsername,
                        wrongYearStart,
                        wrongYearEnd,
                        correctYear
                    )
                    result.totalUpdated += updated
                    result.processedIssues.add(issue.key)
                } catch (e: Exception) {
                    logger.log(Level.SEVERE, "Error processing issue ${issue.key}", e)
                    result.errors.add("Issue ${issue.key}: ${e.message}")
                }
            }

            logger.info("Migration completed. Updated: ${result.totalUpdated}, Errors: ${result.errors.size}")
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Fatal error during migration", e)
            result.errors.add("Fatal error: ${e.message}")
        }

        return result
    }

    /**
     * Обработать worklog для конкретной задачи (типизированная версия)
     */
    private suspend fun processIssueWorklogsTyped(
        issueKey: String,
        targetUsername: String,
        wrongYearStart: LocalDateTime,
        wrongYearEnd: LocalDateTime,
        correctYear: Int
    ): Int {
        logger.info("Processing issue: $issueKey")

        var totalUpdated = 0
        var startAt = 0
        val maxResults = 1000
        var hasMore = true

        while (hasMore) {
            try {
                val worklogCollection = getWorklogs(issueKey, startAt, maxResults)

                for (worklog in worklogCollection.worklogs) {
                    val author = worklog.author.name ?: continue
                    val started = LocalDateTime.parse(
                        worklog.started.substring(0, 19),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    )

                    // Проверить, подходит ли worklog под критерии
                    if (author == targetUsername &&
                        !started.isBefore(wrongYearStart) &&
                        !started.isAfter(wrongYearEnd)
                    ) {

                        val newDate = started.withYear(correctYear)

                        logger.info("Updating worklog ${worklog.id}: $started -> $newDate")

                        // Обновить worklog
                        if (!fakeUpdate) {
                            updateWorklog(
                                issueKey,
                                worklog,
                                newDate,
                                worklog.timeSpentSeconds,
                                worklog.comment
                            )
                        }
                        totalUpdated++
                    }
                }

                startAt += maxResults
                hasMore = startAt < worklogCollection.total
            } catch (e: Exception) {
                logger.log(Level.SEVERE, "Error processing worklogs for $issueKey", e)
                throw e
            }
        }

        return totalUpdated
    }

    private fun buildJqlForWorklog(
        targetUsername: String,
        wrongYearStart: LocalDateTime,
        wrongYearEnd: LocalDateTime
    ): String {
        //return """worklogAuthor = "$targetUsername" AND worklogDate >= "${wrongYearStart.toLocalDate()}" AND worklogDate <= "${wrongYearEnd.toLocalDate()}""""
        val startDate = wrongYearStart.toLocalDate().toString()
        val endDate = wrongYearEnd.toLocalDate().toString()

        return "worklogAuthor = \"$targetUsername\" AND worklogDate >= \"$startDate\" AND worklogDate <= \"$endDate\""
    }

    private fun buildJsonString(started: String, timeSpentSeconds: Int, comment: String?): String {
        return if (comment != null) {
            """{"started":"$started","timeSpentSeconds":$timeSpentSeconds,"comment":"$comment"}"""
        } else {
            """{"started":"$started","timeSpentSeconds":$timeSpentSeconds}"""
        }
    }

    /**
     * Закрытие клиента с logout
     */
    suspend fun close() {
        try {
            logout()
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Error during cleanup", e)
        } finally {
            client.close()
        }
    }

}


// Добавляем новую модель для проверки сессии
@Serializable
data class SessionCheckResponse(
    val name: String? = null,
    val value: String? = null
)

// Обновляем AuthResponse, делаем поля опциональными
@Serializable
data class AuthResponse(
    val session: SessionInfo? = null,
    val loginInfo: LoginInfo? = null
)

@Serializable
data class SessionInfo(
    val name: String,
    val value: String
)

@Serializable
data class LoginInfo(
    val failedLoginCount: Int,
    val loginCount: Int,
    val lastFailedLoginTime: String? = null,
    val previousLoginTime: String? = null
)

data class WorklogFixResult(
    var totalUpdated: Int = 0,
    val processedIssues: MutableList<String> = mutableListOf(),
    val errors: MutableList<String> = mutableListOf()
)

@Serializable
data class WorklogResponse(
    val startAt: Int,
    val maxResults: Int,
    val total: Int,
    val worklogs: List<Worklog>
)


@Serializable
data class Author(
    val name: String,
    val accountId: String
)


@Serializable
data class SearchResponse(
    val expand: String? = null,
    val startAt: Int,
    val maxResults: Int,
    val total: Int,
    val issues: List<Issue>
)

@Serializable
data class Issue(
    val expand: String? = null,
    val id: String,
    val key: String,
    val self: String,
    val fields: IssueFields? = null,
    val changelog: Changelog? = null,
    val names: Map<String, String>? = null,
    val schema: Map<String, String>? = null
)

@Serializable
data class IssueFields(
    val summary: String? = null,
    val status: Status? = null,
    val priority: Priority? = null,
    val issuetype: IssueType? = null,
    val created: String? = null,
    val updated: String? = null,
    val project: Project? = null,
    val worklog: WorklogCollection? = null
)

@Serializable
data class Status(
    val self: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val name: String,
    val id: String,
    val statusCategory: StatusCategory? = null
)

@Serializable
data class Priority(
    val self: String,
    val iconUrl: String? = null,
    val name: String,
    val id: String
)

@Serializable
data class IssueType(
    val self: String,
    val id: String,
    val description: String? = null,
    val iconUrl: String? = null,
    val name: String,
    val subtask: Boolean = false,
    val avatarId: Int? = null
)

@Serializable
data class Project(
    val self: String,
    val id: String,
    val key: String,
    val name: String? = null,
    val projectTypeKey: String? = null,
    val simplified: Boolean = false,
    val avatarUrls: AvatarUrls? = null
)

@Serializable
data class StatusCategory(
    val self: String,
    val id: Int,
    val key: String,
    val colorName: String,
    val name: String
)

@Serializable
data class AvatarUrls(
    @SerialName("48x48")
    val size48x48: String? = null,
    @SerialName("24x24")
    val size24x24: String? = null,
    @SerialName("16x16")
    val size16x16: String? = null,
    @SerialName("32x32")
    val size32x32: String? = null
)


@Serializable
data class WorklogCollection(
    val startAt: Int,
    val maxResults: Int,
    val total: Int,
    val worklogs: List<Worklog>
)

@Serializable
data class Worklog(
    val self: String,
    val id: String,
    val issueId: String? = null,
    val author: User,
    val updated: String,
    val started: String,
    val timeSpent: String? = null,
    val timeSpentSeconds: Int,
    val comment: String? = null,
    val created: String,
    val updateAuthor: User? = null,
    val visibility: WorklogVisibility? = null
)

@Serializable
data class User(
    val self: String,
    val name: String? = null,
    val key: String? = null,  // Заменяем accountId на key для API v2
    val accountId: String? = null,    // Замена key для API v3
    val emailAddress: String? = null,
    val avatarUrls: AvatarUrls? = null,
    val displayName: String,
    val active: Boolean,
    val timeZone: String? = null,
    val accountType: String? = null,
    val locale: String? = null
)

@Serializable
data class WorklogVisibility(
    val type: String,
    val value: String? = null
)

@Serializable
data class Changelog(
    val startAt: Int,
    val maxResults: Int,
    val total: Int,
    val histories: List<History>? = null
)

@Serializable
data class History(
    val id: String,
    val author: User,
    val created: String,
    val items: List<HistoryItem>,
    val historyMetadata: HistoryMetadata? = null
)

@Serializable
data class HistoryItem(
    val field: String,
    val fieldtype: String,
    val from: String? = null,
    val fromString: String? = null,
    val to: String? = null,
    val toString: String? = null
)

@Serializable
data class HistoryMetadata(
    val type: String? = null,
    val description: String? = null,
    val actor: User? = null,
    val generator: String? = null,
    val cause: String? = null,
    val extraData: Map<String, String>? = null
)
