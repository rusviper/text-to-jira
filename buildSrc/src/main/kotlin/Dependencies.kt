package ru.rusviper.gradle

/**
 * Объект включает в себя все зависимости библиотек, используемые в проекте
 */
object Dependencies {

    // web-server
    const val ktor = "io.ktor:ktor-server-core:${Versions.ktorVersion}"
    const val kotlinGrpc = "io.grpc:grpc-kotlin-stub:${Versions.kotlinGrpc}"

    // web-client
    const val ktorClientCore = "io.ktor:ktor-client-core:${Versions.ktorVersion}"
    const val ktorClientCIO = "io.ktor:ktor-client-cio:${Versions.ktorVersion}"
    const val ktorClientContent = "io.ktor:ktor-client-content-negotiation:${Versions.ktorVersion}"
    const val ktorKotlinxSerialization = "io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}"

    // di
    const val ktorKoin = "io.insert-koin:koin-ktor:${Versions.koinVersion}"

    // Версия SLF4J, совместимая с Log4j2
    const val ktorKoinSlf4j = "io.insert-koin:koin-logger-slf4j:${Versions.koinVersion}"
}