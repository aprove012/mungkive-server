import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import java.io.File
import java.util.Base64
import java.util.UUID

fun saveBase64Image(base64Data: String): String {
    val decodedBytes = Base64.getDecoder().decode(base64Data)
    val fileName = "${UUID.randomUUID()}.jpg"
    val filePath = "uploads/$fileName"
    File(filePath).apply {
        parentFile.mkdirs()
        writeBytes(decodedBytes)
    }
    return filePath
}

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
        get("/profile") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val profile = UserRepository.getProfile(userId)
            if (profile != null) {
                call.respond(HttpStatusCode.OK, profile)
            } else {
                call.respond(HttpStatusCode.NotFound, "프로필 정보가 없습니다.")
            }
        }

        post("/profile/edit") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val profile = call.receive<ProfileRequest>()
            val picturePath = saveBase64Image(profile.profilePicture)

            val success = UserRepository.updateProfile(
                userId, profile.name, profile.breed, profile.age, picturePath)

            if (success) {
                call.respond(HttpStatusCode.OK, "프로필이 업데이트되었습니다.")
            } else {
                call.respond(HttpStatusCode.InternalServerError, "업데이트 실패")
            }
        }

        post("/post") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val post = call.receive<PostRequest>()
            val profile = UserRepository.getProfile(userId)
            val userName = profile!!.name
            val imagePath = saveBase64Image(post.picture)

            val success = UserRepository.createPost(userId, userName, post.content, imagePath, post.locate, post.likes)
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
