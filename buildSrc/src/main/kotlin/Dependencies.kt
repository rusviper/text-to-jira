package ru.rusviper.gradle

/**
 * Объект включает в себя все зависимости библиотек, используемые в проекте
 */
object Dependencies {

    // web-client
    const val ktorClientCore = "io.ktor:ktor-client-core"
    const val ktorClientCIO = "io.ktor:ktor-client-cio"
    const val ktorClientContent = "io.ktor:ktor-client-content-negotiation"
    const val ktorKotlinxSerialization = "io.ktor:ktor-serialization-kotlinx-json"

    // di
    const val ktorKoin = "io.insert-koin:koin-ktor:${Versions.koinVersion}"

    // Версия SLF4J, совместимая с Log4j2
    const val ktorKoinSlf4j = "io.insert-koin:koin-logger-slf4j:${Versions.koinVersion}"
}