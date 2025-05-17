import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val id: String, val password: String)