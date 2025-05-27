object UserRepository {

    /** 새 사용자 등록 – 이미 존재하면 false */
    fun registerUser(id: String, password: String): Boolean {
        if (exists(id)) return false

        val rows = DatabaseFactory.statement(
            "INSERT INTO users(id, password) VALUES(?, ?)"
        ) {
            it.setString(1, id)
            it.setString(2, password)      // ─ 평문 그대로 저장
            it.executeUpdate()
        }
        return rows == 1
    }

    /** 로그인 검증 – 일치하면 true */
    fun validateUser(id: String, password: String): Boolean =
        DatabaseFactory.statement(
            "SELECT 1 FROM users WHERE id = ? AND password = ? LIMIT 1"
        ) {
            it.setString(1, id)
            it.setString(2, password)      // ─ 평문 비교
            it.executeQuery().next()
        }

    /** ID 존재 여부 */
    private fun exists(id: String): Boolean =
        DatabaseFactory.statement(
            "SELECT 1 FROM users WHERE id = ? LIMIT 1"
        ) {
            it.setString(1, id)
            it.executeQuery().next()
        }

    fun createPost(userId: String, userName: String, content: String, picture: String, locate:String, likes:Int): Boolean {
        val rows = DatabaseFactory.statement(
            "INSERT INTO posts(userId, userName, content, picture, locate, likes) VALUES (?, ?, ?, ?, ?, ?)"
        ) {
            it.setString(1, userId)
            it.setString(2, userName)
            it.setString(3, content)
            it.setString(4, picture)
            it.setString(5, locate)
            it.setInt(6, likes)
            it.executeUpdate()
        }
        return rows == 1
    }

    fun getAllPosts(): List<PostResponse> {
        return DatabaseFactory.statement("SELECT * FROM posts") { stmt ->
            val rs = stmt.executeQuery()
            val result = mutableListOf<PostResponse>()
            while (rs.next()) {
                result.add(
                    PostResponse(
                        id = rs.getInt("id"),
                        userId = rs.getString("userId"),
                        userName = rs.getString("userName"),
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate"),
                        likes = rs.getInt("likes")
                    )
                )
            }
            result
        }
    }

    fun getUserPosts(userId: String): List<PostResponse> {
        return DatabaseFactory.statement("SELECT * FROM posts WHERE userId = ?") { stmt ->
            stmt.setString(1, userId)
            val rs = stmt.executeQuery()
            val result = mutableListOf<PostResponse>()
            while (rs.next()) {
                result.add(
                    PostResponse(
                        id = rs.getInt("id"),
                        userId = rs.getString("userId"),
                        userName = rs.getString("userName"),
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate"),
                        likes = rs.getInt("likes")
                    )
                )
            }
            result
        }
    }

    fun getProfile(userId: String): ProfileResponse? {
        return DatabaseFactory.statement(
            "SELECT * FROM profile WHERE userId = ?"
        ) { stmt ->
            stmt.setString(1, userId)
            val rs = stmt.executeQuery()
            if (rs.next()) {
                ProfileResponse(
                    userId = rs.getString("userId"),
                    name = rs.getString("name"),
                    breed = rs.getString("breed"),
                    age = rs.getInt("age"),
                    profilePicture = rs.getString("profilePicture")
                )
            } else null
        }
    }

    fun updateProfile(userId: String, name: String?, breed: String?, age: Int?, profilePicture: String?): Boolean {
        val rows = DatabaseFactory.statement(
            """
        INSERT INTO profile(userId, name, breed, age, profilePicture) 
        VALUES(?, ?, ?, ?, ?)
        ON CONFLICT(userId) DO UPDATE SET 
        name=excluded.name,
        breed=excluded.breed,
        age=excluded.age, 
        profilePicture=excluded.profilePicture
        """
        ) {
            it.setString(1, userId)
            it.setString(2, name)
            it.setString(3, breed)
            it.setObject(4, age)
            it.setString(5, profilePicture)
            it.executeUpdate()
        }
        return rows > 0
    }
}
