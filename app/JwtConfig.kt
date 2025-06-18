import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import java.util.*

object JwtConfig {
    private const val secret = "5팀"
    private const val issuer = "모프"
    private const val audience = "팀플"
    private const val expirationInMs = 36_000_00 * 10

    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(userId: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationInMs))
            .sign(algorithm)
    }

    fun configureKtorJwt(config: JWTAuthenticationProvider.Config) {
        config.verifier(
            JWT
                .require(algorithm)
                .withAudience(audience)
                .withIssuer(issuer)
                .build()
        )
        config.validate { credential ->
            if (credential.payload.getClaim("userId").asString().isNotEmpty()) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }
}
