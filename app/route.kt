package com.example.server

import com.example.server.server.AuthRequest
import com.example.server.server.AuthResponse
import com.example.server.server.MypageResponse
import com.example.server.server.PostRequest
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import kotlinx.serialization.Serializable

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
            val success = UserRepository.createPost(userId, post.content, post.picture, post.locate)
            if (success) {
                call.respond(HttpStatusCode.Created)
            } else {
                call.respond(HttpStatusCode.InternalServerError, "게시글 저장 실패")
            }
        }

        get("/posts") {
            val principal = call.principal<JWTPrincipal>()
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val posts = UserRepository.getAllPosts()
            call.respond(posts)
        }

        get("/posts/mypost") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)

            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val posts = UserRepository.getUserPosts(userId)
            call.respond(posts)
        }
    }
}
