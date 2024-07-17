package ru.rusviper.logic

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppConfigReaderTest {

    @Test
    fun testReadConfig() {
        val config = AppConfigReader.readConfig()
        // Check the configuration is read correctly and without errors
        assertEquals("user", config.app.jira.login)
        assertEquals("passw", config.app.jira.password)
        assertEquals("https://jira.bfg-soft.ru/", config.app.jira.jiraUrl)
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun testReadConfigWithIncorrectStructure() {
        // Create a fake config file with incorrect structure (jira -> mira)
        val badConfig = """
            ktor {
                deployment {
                    port = 8081
                }
            }
            app {
                mira {
                    login = user
                }
            }
        """.trimIndent()

        assertFailsWith<MissingFieldException> { AppConfigReader.readConfig(badConfig) }
    }
}