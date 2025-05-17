import kotlinx.serialization.Serializable

@Serializable
data class PostRequest(
    val title: String,
    val content: String
)