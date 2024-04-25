package ru.rusviper.plugins

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureTextToJira() {

    routing {
        get("/text/show_issue") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}