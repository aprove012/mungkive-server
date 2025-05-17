package com.example.server

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

    fun createPost(userId: String, title: String, content: String): Boolean {
        val rows = DatabaseFactory.statement(
            "INSERT INTO posts(userId, title, content) VALUES (?, ?, ?)"
        ) {
            it.setString(1, userId)
            it.setString(2, title)
            it.setString(3, content)
            it.executeUpdate()
        }
        return rows == 1
    }
}