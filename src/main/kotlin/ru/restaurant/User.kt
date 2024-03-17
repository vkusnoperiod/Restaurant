package ru.restaurant

import java.sql.DriverManager

class User {
    private val jdbcUrl = "jdbc:mysql://localhost:3306/restaurant" // database url
    private val sqlUsername = "root"                               // user that is allowed to connect to database
    private val sqlPassword = ""                                   // password of an allowed user

    var userName: String = String()
    var userPassword: String = String()
    var userStatus: String = String()

    private fun addUser(username: String, password: String, status: String): Boolean { // SQL-query for adding a new user
        val sql = "INSERT INTO users (username, password, status) VALUES (?, ?, ?)"

        try {
            DriverManager.getConnection(jdbcUrl, sqlUsername, sqlPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)
                    statement.setString(2, password)
                    statement.setString(3, status)

                    val rowsInserted = statement.executeUpdate()
                    return rowsInserted > 0
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
        }
        return false
    }
    /* SQL-query for checking status of a user */
    private fun userParse(username: String, userpassword: String) : Boolean { // SQL-query for trying to log user in
            try {
                return if ((username != "" && username.isNotEmpty() && !username.startsWith(" ")) &&
                        (userpassword != "" && userpassword.isNotEmpty() && !userpassword.startsWith(" "))
                ) {
                    true
                } else {
                    println(
                        coloredMessage(
                            "\nWrong name. Please, try again.", ConsoleColor.RED
                        )
                    )
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    /* SQL-query for checking status of a user */
    private fun statusCheck(status: String) : Boolean { // SQL-query for checking status of a user
        try {
            return if (status.lowercase() == "admin" || status.lowercase() == "visitor"){
                true
            } else {
                println(
                    coloredMessage(
                        "Wrong user status. Please try again.", ConsoleColor.RED
                    )
                )
                false
            }
        } catch (e: Exception) {
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
            return false
        }
    }

    private fun dbAuthentication(username: String, password: String): Boolean { // SQL-query for login-password match
        val sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?"

        try {
            DriverManager.getConnection(jdbcUrl, sqlUsername, sqlPassword).use { connection ->
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
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    /* SQL-query for checking status of a user */
    private fun getStatusByUsername(username: String): String {
        val sql = "SELECT status FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, sqlUsername, sqlPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        return resultSet.getString("status")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error while getting user status: ${e.message}")
        }
        return ""
    }


    /* SQL-query for checking status of a user */
    fun dbRepeatCheck(username: String) : Boolean {
        val sql = "SELECT COUNT(*) FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, sqlUsername, sqlPassword).use { connection ->
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
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    /* SQL-query for checking status of a user */
    fun registration() {
        var help = true
        while(help) {
            try {
                print(coloredMessage("\nCreate a username: ", ConsoleColor.CYAN))
                val username = readlnOrNull().toString()
                print(coloredMessage("Create a password: ", ConsoleColor.CYAN))
                val password = readlnOrNull().toString()
                print(coloredMessage("Choose your status - \"admin\" or \"visitor\": ", ConsoleColor.CYAN))
                val status = readlnOrNull().toString()
                if (userParse(username, password) && statusCheck(status)) {
                    if(!dbRepeatCheck(username)) {
                        userName = username
                        userPassword = password
                        userStatus = status
                        addUser(username, password, status)
                        println(coloredMessage("\nSuccess!", ConsoleColor.GREEN))
                        help = false
                    } else {
                        println(coloredMessage("We already got a person with that username." +
                                " Lets try again", ConsoleColor.RED))
                        help = true
                    }
                } else {
                    println("Try again.\n")
                    help = true
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
                help = true
            }
        }
    }

    /* SQL-query for checking status of a user */
    fun authentication() {
        var help = true
        while(help) {
            try {
                print(coloredMessage("\nUsername: ", ConsoleColor.CYAN))
                val username = readlnOrNull().toString()
                print(coloredMessage("Password: ", ConsoleColor.CYAN))
                val password = readlnOrNull().toString()
                if (userParse(username, password)) {
                    if (dbAuthentication(username, password)) {
                        userName = username
                        userPassword = password
                        userStatus = getStatusByUsername(username)
                        println(coloredMessage("\nWelcome back, ${username}!", ConsoleColor.GREEN))
                        help = false
                    } else {
                        println(coloredMessage("Auth issue. Wrong login or password.", ConsoleColor.RED))
                        help = true
                    }
                } else {
                    println("Pls try again. \n")
                    help = true
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
                help = true
            }
        }
    }

    /* SQL-query for checking status of a user */
    fun getIDByUsername(username: String): Int {
        val sql = "SELECT id FROM users WHERE username = ?"

        try {
            DriverManager.getConnection(jdbcUrl, sqlUsername, sqlPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, username)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        return resultSet.getInt("id")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error while trying to get username id occurred: ${e.message}")
        }
        return 0
    }
}