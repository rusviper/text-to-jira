package ru.rusviper.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureCORS() {
    install(CORS) {
        // todo: ограничить корс через конфигурацию
        anyHost()
        //allowHost("0.0.0.0:8081")
        allowHeader(HttpHeaders.ContentType)
    }
}
