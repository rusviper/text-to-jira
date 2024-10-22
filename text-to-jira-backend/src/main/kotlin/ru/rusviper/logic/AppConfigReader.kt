import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import ru.rusviper.data.AppConfig
import java.io.File

object AppConfigReader {

    fun readConfig(): AppConfig {
        val config = ConfigFactory.load()
        return readConfig(config)
    }

    fun readConfig(configText: String): AppConfig {
        return readConfig(ConfigFactory.parseString(configText))
    }

    fun readConfig(configFile: File): AppConfig {
        return readConfig(ConfigFactory.parseFile(configFile))
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readConfig(config: Config): AppConfig {
        return Hocon.decodeFromConfig(config)
    }
}