import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import kotlinx.serialization.hocon.encodeToConfig
import ru.rusviper.data.AppConfig

object AppConfigReader {

    fun readConfig(): AppConfig {
        val config = ConfigFactory.load()
        return readConfig(config)
    }

    fun readConfig(configFile: String): AppConfig {
        val config = ConfigFactory.load(configFile)
        return readConfig(config)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun readConfig(config: Config): AppConfig {
        return Hocon.decodeFromConfig(config)
    }
}