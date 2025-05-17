package com.example.server

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val id: String, val password: String)
@Serializable
data class AuthResponse(val token: String)
@Serializable
data class MypageResponse(val message: String)
@Serializable
data class PostRequest(val title: String, val content: String)

fun Route.authRoutes() {

    post("/register") {
        val request = call.receive<AuthRequest>()
        val success = UserRepository.registerUser(request.id, request.password)
        if (success) {
            val token = JwtConfig.generateToken(request.id)
            call.respond(HttpStatusCode.Created, AuthResponse(token))
        } else {
            call.respond(HttpStatusCode.Conflict, "이미 존재하는 사용자입니다.")
        }
    }

    post("/login") {
        val request = call.receive<AuthRequest>()
        val valid = UserRepository.validateUser(request.id, request.password)
        if (valid) {
            val token = JwtConfig.generateToken(request.id)
            call.respond(HttpStatusCode.OK, AuthResponse(token))
        } else {
            call.respond(HttpStatusCode.Unauthorized, "아이디 또는 비밀번호가 올바르지 않습니다.")
        }
    }

    authenticate("auth-jwt") {
        get("/mypage") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            val response = MypageResponse("로그인한 사용자 ID: $userId")
            call.respond(HttpStatusCode.OK, response)
        }

        post("/post") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val post = call.receive<PostRequest>()
            val success = UserRepository.createPost(userId, post.title, post.content)
            if (success) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "게시글 저장 실패")
            }
        }
    }
}