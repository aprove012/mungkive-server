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
                    locate   TEXT NOT NULL,
                    locName  TEXT NOT NULL,
                    picture  TEXT NOT NULL,
                    likes    INTEGER NOT NULL DEFAULT 0,
                    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(userId) REFERENCES users(id)
                );
                """
            )
            stmt.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS comments (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    postId   INTEGER NOT NULL,
                    userId   TEXT NOT NULL,
                    content  TEXT NOT NULL,
                    created  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY(postId) REFERENCES posts(id),
                    FOREIGN KEY(userId) REFERENCES users(id)
                );
                """
            )
            stmt.executeUpdate(
                """
                    CREATE TABLE IF NOT EXISTS profile (
                    userId TEXT PRIMARY KEY,
                    name TEXT,
                    breed TEXT,
                    age INTEGER,
                    profilePicture TEXT,
                    FOREIGN KEY(userId) REFERENCES users(id)
                );
                """
            )
            stmt.executeUpdate(
                """
                    CREATE TABLE IF NOT EXISTS liked (
                    userId TEXT,
                    postId INT,
                    FOREIGN KEY(postId) REFERENCES posts(id),
                    FOREIGN KEY(userId) REFERENCES users(id)
                );
                """
            )
        }
    }

    fun <T> statement(sql: String, block: (PreparedStatement) -> T): T =
        connection.prepareStatement(sql).use(block)
}
