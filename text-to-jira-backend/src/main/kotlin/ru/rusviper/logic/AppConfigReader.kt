import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.ExperimentalSerializationApi
import ru.rusviper.data.AppConfig
import ru.rusviper.tools.logger
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
object AppConfigReader {

    fun readConfig(): AppConfig {
        val config = ConfigFactory.load()
        return readConfig(config)
    }

    fun readConfig(configText: String): AppConfig {
        return readConfig(ConfigFactory.parseString(configText))
    }

    fun readConfig(configFile: File): AppConfig {
        return readConfig(ConfigFactory
            .parseFile(configFile)
            .withFallback(ConfigFactory.load())
        )
    }

    fun readConfig(config: Config): AppConfig {
        return proceedConfig(config)
    }

    /**
     * Преобразует HOCON конфигурацию в объектный вид, валидирует.
     * @param load конфигурация HOCON
     * @return объектная конфигурация приложения расчётов
     */
    private fun proceedConfig(load: Config): AppConfig {
        try {
            // Resolve config substitutions before deserialization
            val resolvedConfig = load.resolve()
            val appConfiguration: AppConfig = Hocon.decodeFromConfig(AppConfig.serializer(), resolvedConfig)

            checkConfig(load, appConfiguration)

            return appConfiguration
        } catch (e: ConfigException.Missing) {
            throw RuntimeException("Не найдена HOCON конфигурация. Проверьте application.conf.", e)
        }
    }

    private fun checkConfig(load: Config, appConfiguration: AppConfig) {
        val configList: String = java.lang.String.join(", ", load.root().keys)
        logger.info { "Считан файл конфигурации. Модули: " + configList }

        val login = appConfiguration.app.jira.login
        if (login == null || login.isBlank()) {
            logger.warn { "Возможна проблема при считывании конфигурации." }
        }
    }
}