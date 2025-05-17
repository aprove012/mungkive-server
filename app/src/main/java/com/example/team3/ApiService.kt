import com.example.team3.com.example.team3.MypageResponse
import retrofit2.http.*
import retrofit2.Response

interface ApiService {
    @POST("/register")
    suspend fun register(@Body request: AuthRequest): Response<AuthResponse>

    @POST("/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @GET("/mypage")
    suspend fun getMyPage(
        @Header("Authorization") token: String
    ): Response<MypageResponse>

    @POST("/post")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Body post: PostRequest
    ): Response<Unit>
}
