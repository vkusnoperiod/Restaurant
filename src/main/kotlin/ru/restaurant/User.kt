import java.sql.DriverManager

class User {
    private val jdbcUrl = "jdbc:mysql://localhost:3306/restaurant" // url вашей БД
    private val dbUsername = "restaurant"                               // название вашей БД
    private val dbPassword = ""                           // пароль вашей БД

    var userName: String = String()
    var userPassword: String = String()
    var userStatus: String = String()

    private fun addUser(username: String, password: String, status: String): Boolean {
        val sql = "INSERT INTO users (username, password, status) VALUES (?, ?, ?)"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)
                    statement.setString(2, password)
                    statement.setString(3, status)

                    val rowsInserted = statement.executeUpdate()
                    return rowsInserted > 0
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    private fun userParse(username: String, userpassword: String) : Boolean {
            try {
                return if ((username != "" && username.isNotEmpty() && !username.startsWith(" ")) &&
                        (userpassword != "" && userpassword.isNotEmpty() && !userpassword.startsWith(" "))
                ) {
                    true
                } else {
                    println(
                        coloredMessage(
                            "\nНекорректный ввод данных. Повторите попытку.", ConsoleColor.RED
                        )
                    )
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
                return false
            }
        }

    private fun statusCheck(status: String) : Boolean {
        try {
            return if (status == "admin" || status == "Admin") {
                true
            } else if(status == "visitor" || status == "Visitor") {
                true
            } else {
                println(
                    coloredMessage(
                        "Некорректный статус. Повторите попытку.", ConsoleColor.RED
                    )
                )
                false
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
            return false
        }
    }

    private fun DBAuthentication(username: String, password: String): Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)
                    statement.setString(2, password)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        val userCount = resultSet.getInt(1)
                        return userCount > 0
                    }
                }
            }

        } catch (e: Exception) {
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    private fun getStatusByUsername(username: String): String {
        val sql = "SELECT status FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        return resultSet.getString("status")
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка при получении статуса пользователя: ${e.message}")
        }
        return ""
    }


    fun DBChek(username: String) : Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val count = resultSet.getInt(1)
                        return count > 0
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    fun registration() {
        var help = true
        while(help) {
            try {
                print(coloredMessage("\nПридумайте имя пользователя: ", ConsoleColor.MAGENTA))
                val username = readlnOrNull().toString()
                print(coloredMessage("Придумайте пароль: ", ConsoleColor.MAGENTA))
                val password = readlnOrNull().toString()
                print(coloredMessage("Укажите ваш статус(admin или visitor): ", ConsoleColor.MAGENTA))
                val status = readlnOrNull().toString()
                if (userParse(username, password) && statusCheck(status)) {
                    if(!DBChek(username)) {
                        userName = username
                        userPassword = password
                        userStatus = status
                        addUser(username, password, status)
                        println(coloredMessage("\nУспешная регистрация!", ConsoleColor.GREEN))
                        help = false
                    } else {
                        println(coloredMessage("Ошибка. Пользователь с таким логином существует.", ConsoleColor.RED))
                        help = true
                    }
                } else {
                    println("Повторите попытку.\n")
                    help = true
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
                help = true
            }
        }
    }

    fun authentication() {
        var help = true
        while(help) {
            try {
                print(coloredMessage("\nИмя пользователя: ", ConsoleColor.MAGENTA))
                val username = readlnOrNull().toString()
                print(coloredMessage("Пароль: ", ConsoleColor.MAGENTA))
                val password = readlnOrNull().toString()
                if (userParse(username, password)) {
                    if (DBAuthentication(username, password)) {
                        userName = username
                        userPassword = password
                        userStatus = getStatusByUsername(username)
                        println(coloredMessage("\nДобро пожаловать, ${username}!", ConsoleColor.GREEN))
                        help = false
                    } else {
                        println(coloredMessage("Ошибка аутентификации. Неверный логин или пароль.", ConsoleColor.RED))
                        help = true
                    }
                } else {
                    println("Повторите попытку. \n")
                    help = true
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
                help = true
            }
        }
    }

    fun getIDByUsername(username: String): Int {
        val sql = "SELECT id FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        return resultSet.getInt("id")
                    }
                }
            }
        } catch (e: Exception) {
            println("Ошибка при получении id пользователя: ${e.message}")
        }
        return 0
    }
}