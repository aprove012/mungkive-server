# mungkive-server

Ktor 기반의 RESTful 백엔드 서버입니다.  
현재 GCP 환경에서 `.jar` 파일로 배포되어 실행 중입니다.  
데이터베이스는 sqlite3 사용했습니다.  

## 현재 구현 기능
- 회원가입 (`/register`)
- 로그인 (`/login`)
- 내 프로필 조회 (`/profile`)
- 내 프로필 생성/수정 (`/profile/edit`)
- 게시물 등록 (`/post`)
- 게시물 가져오기 (`/posts`)
- 내 게시물만 가져오기 (`/posts/mypost`)
- 댓글 달기 (`/post/{id}/comment`)
- 댓글 가져오기 (`/post/{id}/comments`)
- 좋아요/싫어요 (`/post/{id}/(un)like`)

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

---

# Postman API Spec

## 회원가입

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/register`
- **Body (JSON)**:
```json
{
  "id": "testuser",
  "password": "1234"
}
```
- **Response**:
  - 201 Created: `{ "token": "<JWT Token>" }`
  - 409 Conflict: "이미 존재하는 사용자입니다."

---

## 로그인

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/login`
- **Body (JSON)**:
```json
{
  "id": "testuser",
  "password": "1234"
}
```
- **Response**:
  - 200 OK: `{ "token": "<JWT Token>" }`
  - 401 Unauthorized: "아이디 또는 비밀번호가 올바르지 않습니다."

---

## 프로필 생성 및 수정

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/profile/edit`
- **Headers**: Authorization: Bearer <JWT Token>
- **Body (JSON)**:
```json
{
  "name": "홍길동",
  "breed": "푸들",
  "age": 3,
  "profilePicture": "Base64로로 인코딩된 이미지"
}
```
- **Response**:
  - 200 OK: "프로필이 업데이트되었습니다."
  - 500 Internal Server Error: "업데이트 실패"

---

## 프로필 조회

- **Method**: GET
- **URL**: `http://<서버 IP>:8080/profile`
- **Headers**: Authorization: Bearer <JWT Token>
- **Response (예시)**:
```json
{
  "userId": "testuser",
  "name": "홍길동",
  "breed": "푸들",
  "age": 3,
  "profilePicture": "uploads/abcd.jpg"
}
```

---

## 게시글 작성

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/post`
- **Headers**: Authorization: Bearer <JWT Token>
- **Body (JSON)**:
```json
{
  "content": "게시글 내용",
  "picture": "Base64로로 인코딩된 이미지",
  "locate": "서울",
  "likes": 0
}
```
- **Response**:
  - 201 Created
  - 500 Internal Server Error: "게시글 저장 실패"

---

## 전체 게시글 목록

- **Method**: GET
- **URL**: `http://<서버 IP>:8080/posts`
- **Headers**: Authorization: Bearer <JWT Token>
- **Response (예시)**:
```json
[
  {
    "id": 1,
    "userId": "testuser",
    "userName": "홍길동",
    "content": "게시글 내용",
    "picture": "uploads/abcd.jpg",
    "locate": "서울",
    "likes": 5
  },
  ...
]
```

---

## 내가 작성한 게시글 목록

- **Method**: GET
- **URL**: `http://<서버 IP>:8080/posts/mypost`
- **Headers**: Authorization: Bearer <JWT Token>
- **Response (예시)**:
```json
[
    {
        "id": 1,
        "userId": "testuser",
        "userName": "홍길동",
        "content": "게시글 내용",
        "picture": "uploads/31447e84-8124-4fa0-ba79-b9df3b3234a5.jpg",
        "locate": "서울",
        "likes": 0
    },
    ...
    
]
```
---

## 댓글 작성

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/post/{postId}/comment`
- **Headers**: Authorization: Bearer <JWT Token>
- **Body (JSON)**:
```json
{
  "content": "댓글 내용"
}
```
- **Response**:
  - 201 Created: "댓글이 추가되었습니다."
  - 400 Bad Request: "댓글 내용을 입력해주세요."
  - 500 Internal Server Error: "댓글 추가 실패"

---

## 게시글의 댓글 조회

- **Method**: GET
- **URL**: `http://<서버 IP>:8080/post/{postId}/comments`
- **Headers**: Authorization: Bearer <JWT Token>
- **Response (예시)**:
```json
[
    {
        "id": 1,
        "postId": 2,
        "userId": "testuser",
        "content": "댓글 내용",
        "created": "2025-05-28 07:30:03"
    },
    ...

]
```
---

## 게시글 좋아요 추가

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/post/{postId}/like`
- **Headers**: Authorization: Bearer <JWT Token>

---

## 게시글 좋아요 취소

- **Method**: POST
- **URL**: `http://<서버 IP>:8080/post/{postId}/unlike`
- **Headers**: Authorization: Bearer <JWT Token>

---

## 게시글 삭제 (댓글도 함께 삭제됨)

- **Method**: DELETE
- **URL**: `http://<서버 IP>:8080/post/{postId}`
- **Headers**: Authorization: Bearer <JWT Token>

---




