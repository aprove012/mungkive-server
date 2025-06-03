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

    fun createPost(
        userId: String,
        userName: String,
        userPic: String,
        content: String,
        picture: String,
        locate: String,
        locName: String,
        likes: Int
    ): Boolean {
        val rows = DatabaseFactory.statement(
            "INSERT INTO posts(userId, userName, userPic, content, picture, locate, locName, likes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        ) {
            it.setString(1, userId)
            it.setString(2, userName)
            it.setString(3, userPic)
            it.setString(4, content)
            it.setString(5, picture)
            it.setString(6, locate)
            it.setString(7, locName)
            it.setInt(8, likes)
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
                        userPic = rs.getString("userPic"),
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate"),
                        locName = rs.getString("locName"),
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
                        userPic = rs.getString("userPic"),
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate"),
                        locName = rs.getString("locName"),
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

    fun updateProfile(
        userId: String,
        name: String?,
        breed: String?,
        age: Int?,
        profilePicture: String?
    ): Boolean {
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

    fun addComment(postId: Int, userId: String, userPic: String, content: String): Boolean {
        val rows = DatabaseFactory.statement(
            "INSERT INTO comments(postId, userId, userPic, content) VALUES (?, ?, ?, ?)"
        ) {
            it.setInt(1, postId)
            it.setString(2, userId)
            it.setString(3, userPic)
            it.setString(4, content)
            it.executeUpdate()
        }
        return rows == 1
    }

    fun getCommentsForPost(postId: Int): List<CommentResponse> {
        return DatabaseFactory.statement(
            "SELECT * FROM comments WHERE postId = ? ORDER BY created ASC"
        ) { stmt ->
            stmt.setInt(1, postId)
            val rs = stmt.executeQuery()
            val result = mutableListOf<CommentResponse>()
            while (rs.next()) {
                result.add(
                    CommentResponse(
                        id = rs.getInt("id"),
                        postId = rs.getInt("postId"),
                        userId = rs.getString("userId"),
                        userPic = rs.getString("userPic"),
                        content = rs.getString("content"),
                        created = rs.getString("created")
                    )
                )
            }
            result
        }
    }

    fun updateLikes(postId: Int, increment: Int): Boolean {
        val rows = DatabaseFactory.statement(
            """
                UPDATE posts
                SET likes = CASE
                WHEN likes + ? < 0 THEN 0
                ELSE likes + ?
                END
                WHERE id = ?
            """
        ) {
            it.setInt(1, increment)
            it.setInt(2, increment)
            it.setInt(3, postId)
            it.executeUpdate()
        }
        return rows == 1
    }


    fun deletePostAndComments(postId: Int, userId: String): Boolean {
        return DatabaseFactory.statement(
            """
        DELETE FROM comments WHERE postId = ?;
        DELETE FROM posts WHERE id = ? AND userId = ?;
        """
        ) { stmt ->
            // (댓글 삭제)
            stmt.setInt(1, postId)
            stmt.addBatch()
            // (게시글 삭제)
            stmt.setInt(1, postId)
            stmt.setString(2, userId)
            stmt.addBatch()

            val results = stmt.executeBatch()
            results.all { it >= 0 }
        }
    }
}
