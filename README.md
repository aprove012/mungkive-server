# mungkive-server

Ktor 기반의 RESTful 백엔드 서버입니다.  
현재 GCP 환경에서 `.jar` 파일로 배포되어 실행 중입니다.  
데이터베이스는 sqlite3 사용했습니다.  

## 현재 구현 기능
- 회원가입 (`/register`)
- 로그인 (`/login`)
- 게시물 등록 (`/post`)

## 코드 설명
- Route.kt
  --
  통신할 데이터 클래스 생성 및 서버와 통신 설정입니다.
    
  데이터 클래스는 반드시 직렬화 해줘야 합니다. 
  ```
  @Serializable
  data class AuthRequest(val id: String, val password: String)
  ```
  데이터 post

  ```
    post("/login") {
        val request = call.receive<AuthRequest>() // 해당되는 데이터 클래스로 통신시작 
        val valid = UserRepository.validateUser(request.id, request.password) // user.kt의 함수를 통해 SQL 연결
        if (valid) {
            val token = JwtConfig.generateToken(request.id)
            call.respond(HttpStatusCode.OK, AuthResponse(token)) // 로그인 성공 시 토큰 생성
        } else {
            call.respond(HttpStatusCode.Unauthorized, "아이디 또는 비밀번호가 올바르지 않습니다.")
        }
    }
  ```
  로그인 이후 기능은 모두 authenticate 안에 있어야 합니다.
  ```
  authenticate("auth-jwt") {
  ...
  }
  ```
  
- DatabaseFactory.kt
  --
  table 생성코드입니다.  
  ```
  stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id       TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                );
                """
            )
  ```
 
- user.kt
  --
  데이터베이스 접근 함수들 입니다.
  Sql 쿼리 사용은 다음과 같습니다.
  ```
  DatabaseFactory.statement(
            "SELECT 1 FROM users WHERE id = ? AND password = ? LIMIT 1"
        ) {
            it.setString(1, id)
            it.setString(2, password)      // ─ 평문 비교
            it.executeQuery().next()
        }
- JwtConfig.kt
  -- JWT 생성 함수가 있습니다.  userId를 기준으로 인증합니다.  유효시간은 10시간으로 넉넉하게 잡아놨습니다.

- Apllication.kt
  --
  main입니다.
  일단 이 코드들을 안드로이드 스튜디오에 넣어놓고 이것만 실행해도 서버가 동작합니다.

### client branch에 클라이언트 예시 코드 있습니다.
