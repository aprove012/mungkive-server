import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val id: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class ProfileRequest(
    val name: String,
    val breed: String,
    val age: Int,
    val profilePicture: String = ""
)

@Serializable
data class ProfileResponse(
    val userId: String,
    val name: String,
    val breed: String,
    val age: Int,
    val profilePicture: String = ""
)

@Serializable
data class PostRequest(val content: String, val picture: String = "", val locate: String, val locName: String, val likes: Int)

@Serializable
data class PostResponse(
    val id: Int,
    val userId: String,
    val userName: String,
    val userPic: String,
    val content: String,
    val picture: String = "",
    val locate: String,
    val locName: String,
    val likes: Int
)

@Serializable
data class CommentResponse(
    val id: Int,
    val postId: Int,
    val userId: String,
    val userPic: String,
    val content: String,
    val created: String
)

