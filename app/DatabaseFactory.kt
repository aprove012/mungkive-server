package com.example.server

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

object DatabaseFactory {

    private const val JDBC_URL = "jdbc:sqlite:users.db"
    private val connection: Connection

    init {
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection(JDBC_URL)

        connection.createStatement().use { stmt ->
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS users (
                    id       TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                );
                """
            )
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS posts (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    userId   TEXT NOT NULL,
                    content  TEXT NOT NULL,
                    picture  TEXT NOT NULL,
                    locate TEXT NOT NULL
                );
                """
            )
        }
    }

    fun <T> statement(sql: String, block: (PreparedStatement) -> T): T =
        connection.prepareStatement(sql).use(block)
}
