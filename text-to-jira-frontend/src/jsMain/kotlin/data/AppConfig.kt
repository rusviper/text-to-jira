package data

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val apiUrl: String,
    val timeout: Int
)

/**
 * Пока временная модель конфигурации
 */
fun loadConfig(): AppConfig {
    return AppConfig("http://localhost:8082", 3000)
}
//
//suspend fun saveConfig(appConfig: AppConfig) {
//    val configJson = Json.encodeToString(appConfig)
//    val configFile = File("config.json")
//    configFile.writeText(configJson)
//}

