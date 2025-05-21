package com.example.server

import com.example.server.server.PostResponse

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

    fun createPost(userId: String, content: String, picture: String, locate:String): Boolean {
        val rows = DatabaseFactory.statement(
            "INSERT INTO posts(userId, content, picture, locate) VALUES (?, ?, ?, ?)"
        ) {
            it.setString(1, userId)
            it.setString(2, content)
            it.setString(3, picture)
            it.setString(4, locate)
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
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate")
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
                        content = rs.getString("content"),
                        picture = rs.getString("picture"),
                        locate = rs.getString("locate")
                    )
                )
            }
            result
        }
    }
}
