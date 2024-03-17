package ru.restaurant

import java.sql.DriverManager

class Admin(val admin: User, val restaurant: Restaurant) {
    private val jdbcUrl = "jdbc:mysql://localhost:3306/restaurant" // url вашей БД
    private val dbUsername = "root"                               // название вашей БД
    private val dbPassword = ""                              // пароль вашей БД

    private fun addToDB(title: String, amount: Int, price: Int, cook_time: Int) : Boolean{
        val sql = "INSERT INTO dishes (title, amount, price, cook_time) VALUES (?, ?, ?, ?)"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, title)
                    statement.setInt(2, amount)
                    statement.setInt(3, price)
                    statement.setInt(4, cook_time)

                    val rowsInserted = statement.executeUpdate()
                    return rowsInserted > 0
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
            return false
        }
    }

    private fun dishCheck(dish: String) : Boolean {
        val sql = "SELECT COUNT(*) FROM dishes WHERE title = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, dish)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val count = resultSet.getInt(1)
                        return count > 0
                    }
                }
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        return false
    }

    private fun deleteFromDB(dishName: String) : Boolean {
        val sql = "DELETE FROM dishes WHERE title = ?"

        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, dishName)
                    val rowsDeleted = statement.executeUpdate()
                    return rowsDeleted > 0
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error while getting rid of a dish: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    private fun updateDish(oldName: String, newName: String?, amount: Int?, price: Int?, time: Int?): Boolean {
        val connection = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)
        try {
            connection.autoCommit = false
            if (newName != null) {
                val sqlUpdateName = "UPDATE dishes SET title = ? WHERE title = ?"
                connection.prepareStatement(sqlUpdateName).use { statement ->
                    statement.setString(1, newName)
                    statement.setString(2, oldName)
                    statement.executeUpdate()
                }
            }
            if (amount != null) {
                val sqlUpdateAmount = "UPDATE dishes SET amount = ? WHERE title = ?"
                connection.prepareStatement(sqlUpdateAmount).use { statement ->
                    statement.setInt(1, amount)
                    statement.setString(2, oldName)
                    statement.executeUpdate()
                }
            }
            if (price != null) {
                val sqlUpdatePrice = "UPDATE dishes SET price = ? WHERE title = ?"
                connection.prepareStatement(sqlUpdatePrice).use { statement ->
                    statement.setInt(1, price)
                    statement.setString(2, oldName)
                    statement.executeUpdate()
                }
            }
            if (time != null) {
                val sqlUpdateTime = "UPDATE dishes SET cook_time = ? WHERE title = ?"
                connection.prepareStatement(sqlUpdateTime).use { statement ->
                    statement.setInt(1, time)
                    statement.setString(2, oldName)
                    statement.executeUpdate()
                }
            }
            connection.commit()
            return true
        } catch (e: Exception) {
            connection.rollback()
            println(coloredMessage("Error while updating a dish: ${e.message}", ConsoleColor.RED))
            return false
        } finally {
            connection.close()
        }
    }


    fun revenue() {
        val sql = "SELECT SUM(total_price) AS revenue FROM orders WHERE status = 'Оплачен'"
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val revenue = resultSet.getInt("revenue")
                        print(coloredMessage("\nTotal revenue: ", ConsoleColor.BLUE))
                        println("$revenue rub.")
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error while getting a revenue: ${e.message}", ConsoleColor.RED))
        }
    }

    fun addDish() : Boolean {
        while(true) {
            try {
                print(coloredMessage("\nEnter dish name: ", ConsoleColor.CYAN))
                val dishName = readlnOrNull().toString()
                val dishAmount = readValidNumber(coloredMessage("Quantity: ", ConsoleColor.CYAN))
                val price = readValidNumber(coloredMessage("Price: ", ConsoleColor.CYAN))
                val cookTime = readValidNumber(coloredMessage("Cooking time in minutes: ", ConsoleColor.CYAN))
                val dish = Dish(dishName, dishAmount!!.toInt(), price!!.toInt(), cookTime!!.toInt())
                return if (dish.dishParse()) {
                    return if(!admin.dbRepeatCheck(dishName)) {
                        addToDB(dishName, dishAmount, price, cookTime)
                        println(coloredMessage("\nDone!", ConsoleColor.GREEN))
                        false
                    } else {
                        println(coloredMessage("We dont have a dish with that kind of a name.", ConsoleColor.RED))
                        true
                    }
                } else {
                    println(
                        coloredMessage(
                            "Wrong data. Please try again.", ConsoleColor.RED
                        )
                    )
                    true
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
                return true
            }
        }
    }

    fun editDish() {
        while (true) {
            try {
                val menu: Map<String, Int> = restaurant.getMenu()
                if(menu.isEmpty()) {
                    println(coloredMessage("\nThe menu is empty", ConsoleColor.CYAN))
                    break
                }
                restaurant.showMenu()
                print(coloredMessage("\nEnter a dish name to edit or \"Esc\" to exit: ", ConsoleColor.CYAN))
                val dishName = readlnOrNull().toString()

                if (dishName.equals("Esc", ignoreCase = true)) {
                    println(coloredMessage("\nEdit of a dish aborted.", ConsoleColor.CYAN))
                    break
                }

                if (!dishCheck(dishName)) {
                    println(coloredMessage("We dont have that kind of a dish.", ConsoleColor.RED))
                    continue
                }

                println(coloredMessage("\nEnter parameters to edit (numbers separated with commas):\n1. Name\n2. Quantity\n3. Price\n4. Time to cook", ConsoleColor.WHITE))
                print(coloredMessage("Your pick: ", ConsoleColor.CYAN))
                val choicesInput = readln().split(",").map { it.trim().toIntOrNull() }

                if (choicesInput.isEmpty() || choicesInput.any { it !in 1..4 }) {
                    println(coloredMessage("Error. Enter parameters to edit (enter numbers from 1 to 4).", ConsoleColor.RED))
                    continue
                }

                val choices = choicesInput.mapNotNull { it }

                var newName: String? = null
                var newAmount: Int? = null
                var newPrice: Int? = null
                var newCookTime: Int? = null

                if (choices.contains(1)) {
                    print(coloredMessage("Name of a dish: ", ConsoleColor.CYAN))
                    newName = readlnOrNull()
                }
                if (choices.contains(2)) {
                    newAmount = readValidNumber(coloredMessage("How many: ", ConsoleColor.CYAN))
                }
                if (choices.contains(3)) {
                    newPrice = readValidNumber(coloredMessage("Dish price: ", ConsoleColor.CYAN))
                }
                if (choices.contains(4)) {
                    newCookTime = readValidNumber(coloredMessage("Time to cook: ", ConsoleColor.CYAN))
                }

                if (updateDish(dishName, newName, newAmount, newPrice, newCookTime)) {
                    println(coloredMessage("\nDish is updated!", ConsoleColor.CYAN))
                    break
                } else {
                    println(coloredMessage("\nError updating a dish occurred.", ConsoleColor.RED))
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
            }
        }
    }

    fun deleteDish() {
        while (true) {
            try {
                val menu: Map<String, Int> = restaurant.getMenu()
                if(menu.isEmpty()) {
                    println(coloredMessage("\nThe menu is empty", ConsoleColor.CYAN))
                    break
                }
                restaurant.showMenu()
                print(coloredMessage("\nEnter a dish name to delete or \"Esc\" to exit: ", ConsoleColor.CYAN))
                val userInput = readlnOrNull()?.toString() ?: continue

                if (userInput.equals("Esc", ignoreCase = true)) {
                    println(coloredMessage("\nDeleting is aborted.", ConsoleColor.CYAN))
                    return
                }

                if (dishCheck(userInput)) {
                    if (deleteFromDB(userInput)) {
                        println(coloredMessage("\nDish is deleted.", ConsoleColor.GREEN))
                        break
                    } else {
                        println(coloredMessage("Error. Dish is not found.", ConsoleColor.RED))
                    }
                } else {
                    println(
                        coloredMessage(
                            "Wrong data. Please try again.", ConsoleColor.RED
                        )
                    )
                }
            } catch (e: Exception) {
                println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
            }
        }
    }

    fun showFeedbacks() {
        val sql = "SELECT * FROM feedback"
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    var count = 1
                    if (!resultSet.isBeforeFirst) {
                        println(coloredMessage("\nNo feedbacks yet.", ConsoleColor.CYAN))
                    } else {
                        println(coloredMessage("\nFeedbacks: ", ConsoleColor.CYAN))
                        while (resultSet.next()) {
                            val feedbackId = resultSet.getInt("id")
                            val orderId = resultSet.getInt("order_id")
                            val rating = resultSet.getInt("rating")
                            val feedbackText = resultSet.getString("comment")
                            println("$count. Feedback #$feedbackId on order with ID $orderId: Rate: $rating | Comment: $feedbackText")
                            count++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error getting feedbacks occurred: ${e.message}", ConsoleColor.RED))
        }
    }

    fun usersInSystem() {
        val sql = "SELECT username, status FROM users"
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    var count = 1
                    println(coloredMessage("\nUsers: ", ConsoleColor.BLUE))
                    println("User | Status")
                    while (resultSet.next()) {
                        val username = resultSet.getString("username")
                        val status = resultSet.getString("status")
                        println("$count. $username | $status")
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Error getting users occurred: ${e.message}", ConsoleColor.RED))
        }
    }
}