package com.example.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun main() {
    embeddedServer(Netty, host = "0.0.0.0", port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    DatabaseFactory

    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        jwt("auth-jwt") {
            JwtConfig.configureKtorJwt(this)
        }
    }

    routing {
        authRoutes()
    }
}