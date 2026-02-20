package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import com.example.plugins.configureContentNegotiation
import com.example.routes.healthRoutes
import com.example.routes.rootRoute
import com.example.routes.userRoutes
import com.example.services.UserService

val userService = UserService()

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureContentNegotiation()
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        rootRoute()

        route("/api/v1") {
            userRoutes(userService)
            healthRoutes()
        }
    }
}
