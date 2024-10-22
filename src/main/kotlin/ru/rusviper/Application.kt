package ru.rusviper

import io.ktor.server.application.*
import ru.rusviper.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureCORS()
    configureMonitoring()
    configureSerialization()
    configureAdministration()
    configureRouting()
    configureTextToJira()
}
