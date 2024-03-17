package ru.restaurant

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.sql.Statement


class Restaurant {
    private val jdbcUrl = "jdbc:mysql://localhost:3306/restaurant" // url вашей БД
    private val dbUsername = "root"                               // название вашей БД
    private val dbPassword = ""                           // пароль вашей БД

    private fun getDishPrice(dishName: String): Double {
        var price = 0.0
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT price FROM dishes WHERE title = ?"
                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, dishName)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        price = resultSet.getDouble("price")
                    }
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while getting a dish price: ${e.message}", ConsoleColor.RED))
        }
        return price
    }

    private fun isOrderExists(connection: Connection, orderId: Int): Boolean {
        val sql = "SELECT COUNT(*) FROM orders WHERE id = ?"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1, orderId)
        val resultSet = statement.executeQuery()
        val exists = resultSet.next() && resultSet.getInt(1) > 0
        resultSet.close()
        statement.close()
        return exists
    }

    private fun getOrderItems(connection: Connection, orderId: Int): Map<String, Int> {
        val orderItems = mutableMapOf<String, Int>()
        val sql = "SELECT dish_name, quantity FROM order_items WHERE order_id = ?"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1, orderId)
        val resultSet = statement.executeQuery()
        while (resultSet.next()) {
            val dishName = resultSet.getString("dish_name")
            val quantity = resultSet.getInt("quantity")
            orderItems[dishName] = quantity
        }
        resultSet.close()
        statement.close()
        return orderItems
    }

    private fun getCurrentQuantity(connection: Connection, orderId: Int, dishName: String): Int {
        val sql = "SELECT quantity FROM order_items WHERE order_id = ? AND dish_name = ?"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1, orderId)
        statement.setString(2, dishName)
        val resultSet = statement.executeQuery()
        val quantity = if (resultSet.next()) resultSet.getInt("quantity") else 0
        resultSet.close()
        statement.close()
        return quantity
    }

    private fun calculateTotalPrice(order: Map<String, Int>): Double {
        var totalPrice = 0.0
        for ((dishName, quantity) in order) {
            val dishPrice = getDishPrice(dishName)
            totalPrice += dishPrice * quantity
        }
        return totalPrice
    }


    fun getMenu(): Map<String, Int> {
        val menu = mutableMapOf<String, Int>()
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT title, amount FROM dishes"
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val name = resultSet.getString("title")
                        val amount = resultSet.getInt("amount")
                        menu[name] = amount
                    }
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while trying to get a menu: ${e.message}", ConsoleColor.RED))
        }
        return menu
    }

    fun showMenu() {
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT title, amount, price, cook_time FROM dishes ORDER BY title"
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    var index = 1
                    println(coloredMessage("\nMenu:", ConsoleColor.CYAN))
                    if (!resultSet.next()) {
                        println(coloredMessage("\nThe menu is empty", ConsoleColor.CYAN))
                    } else {
                        do {
                            val name = resultSet.getString("title")
                            val amount = resultSet.getInt("amount")
                            val price = resultSet.getDouble("price")
                            val cookTime = resultSet.getInt("cook_time")
                            println("$index. $name: How many: $amount, Price: $price, Time to cook: $cookTime min.")
                            index++
                        } while (resultSet.next())
                    }
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while trying to get an order: ${e.message}", ConsoleColor.RED))
        }
    }

    fun cancelOrder(orderId: Int) {
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.autoCommit = false

                val orderItems = getOrderItems(connection, orderId)

                for ((dishName, quantity) in orderItems) {
                    val updateSql = "UPDATE dishes SET amount = amount + ? WHERE title = ?"
                    val updateStatement = connection.prepareStatement(updateSql)
                    updateStatement.setInt(1, quantity)
                    updateStatement.setString(2, dishName)
                    updateStatement.executeUpdate()
                    updateStatement.close()
                }

                val updateOrderSql = "UPDATE orders SET status = 'Отменен' WHERE id = ?"
                val updateOrderStatement = connection.prepareStatement(updateOrderSql)
                updateOrderStatement.setInt(1, orderId)
                updateOrderStatement.executeUpdate()
                updateOrderStatement.close()

                connection.commit()
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while trying to cancel the order: ${e.message}", ConsoleColor.RED))
        }
    }
fun addOrderToDB(userId: Int, order: Map<String, Int>, status: String): Int {
    var orderId = -1
    try {
        DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
            connection.autoCommit = false

            val totalPrice = calculateTotalPrice(order)
            val insertOrderSql = "INSERT INTO orders (user_id, total_price, status) VALUES (?, ?, ?)"
            val orderStatement = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)
            orderStatement.setInt(1, userId)
            orderStatement.setDouble(2, totalPrice.toDouble()) // Убедитесь, что тип данных совместим
            orderStatement.setString(3, status)
            orderStatement.executeUpdate()

            val rs = orderStatement.generatedKeys
            if (rs.next()) {
                orderId = rs.getInt(1) // Получите сгенерированный ID заказа
            } else {
                println("Error while trying to add a dish to database.")
                return orderId
            }

            order.forEach { (dishName, quantity) ->
                val currentQuantity = getCurrentQuantity(connection, orderId, dishName)
                if (currentQuantity > 0) {
                    val updateSql = "UPDATE order_items SET quantity = quantity + ? WHERE order_id = ? AND dish_name = ?"
                    connection.prepareStatement(updateSql).use { updateStatement ->
                        updateStatement.setInt(1, quantity)
                        updateStatement.setInt(2, orderId)
                        updateStatement.setString(3, dishName)
                        updateStatement.executeUpdate()
                    }
                } else {
                    val insertSql = "INSERT INTO order_items (order_id, dish_name, quantity) VALUES (?, ?, ?)"
                    connection.prepareStatement(insertSql).use { insertStatement ->
                        insertStatement.setInt(1, orderId)
                        insertStatement.setString(2, dishName)
                        insertStatement.setInt(3, quantity)
                        insertStatement.executeUpdate()
                    }
                }

                // Предполагается, что вы хотите уменьшить количество блюд при добавлении заказа
                val updateDishSql = "UPDATE dishes SET amount = amount - ? WHERE title = ?"
                connection.prepareStatement(updateDishSql).use { updateDishStatement ->
                    updateDishStatement.setInt(1, quantity)
                    updateDishStatement.setString(2, dishName)
                    updateDishStatement.executeUpdate()
                }
            }

            connection.commit()
        }
    } catch (e: SQLException) {
        println("Error while trying to add a dish to database: ${e.message}")
    }
    return orderId
}

    fun saveFeedback(orderId: Int, rating: Int, comment: String) {
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "INSERT INTO feedback (order_id, rating, comment, created_at) VALUES (?, ?, ?, ?)"
                val statement = connection.prepareStatement(sql)
                statement.setInt(1, orderId)
                statement.setInt(2, rating)
                statement.setString(3, comment)
                statement.setTimestamp(4, Timestamp(System.currentTimeMillis()))
                statement.executeUpdate()
            }
        } catch (e: SQLException) {
            println("Error while saving a feedback: ${e.message}")
        }
    }

    fun addDishToOrder(orderId: Int, dishName: String, quantity: Int) {
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.autoCommit = false

                val orderExists = isOrderExists(connection, orderId)
                if (!orderExists) {
                    println(coloredMessage("Order with $orderId ID  is not found.", ConsoleColor.RED))
                    return
                }

                val currentQuantity = getCurrentQuantity(connection, orderId, dishName)

                if (currentQuantity > 0) {
                    val updateSql = "UPDATE order_items SET quantity = quantity + ? WHERE order_id = ? AND dish_name = ?"
                    val updateStatement = connection.prepareStatement(updateSql)
                    updateStatement.setInt(1, quantity)
                    updateStatement.setInt(2, orderId)
                    updateStatement.setString(3, dishName)
                    updateStatement.executeUpdate()
                    updateStatement.close()
                } else {
                    val insertSql = "INSERT INTO order_items (order_id, dish_name, quantity) VALUES (?, ?, ?)"
                    val insertStatement = connection.prepareStatement(insertSql)
                    insertStatement.setInt(1, orderId)
                    insertStatement.setString(2, dishName)
                    insertStatement.setInt(3, quantity)
                    insertStatement.executeUpdate()
                    insertStatement.close()
                }

                val totalPriceSql = "SELECT SUM(d.price * oi.quantity) AS total_price " +
                        "FROM order_items oi " +
                        "JOIN dishes d ON oi.dish_name = d.title " +
                        "WHERE oi.order_id = ?"
                val totalPriceStatement = connection.prepareStatement(totalPriceSql)
                totalPriceStatement.setInt(1, orderId)
                val totalPriceResultSet = totalPriceStatement.executeQuery()
                var totalPrice = 0.0
                if (totalPriceResultSet.next()) {
                    totalPrice = totalPriceResultSet.getDouble("total_price")
                }
                totalPriceStatement.close()

                val updateOrderSql = "UPDATE orders SET total_price = ? WHERE id = ?"
                val updateOrderStatement = connection.prepareStatement(updateOrderSql)
                updateOrderStatement.setDouble(1, totalPrice)
                updateOrderStatement.setInt(2, orderId)
                updateOrderStatement.executeUpdate()
                updateOrderStatement.close()

                connection.commit()
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while adding a dish to an order: ${e.message}", ConsoleColor.RED))
        }
    }

    fun getTotalAmount(orderId: Int): Double {
        var totalAmount = 0.0
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT SUM(dishes.price * order_items.quantity) AS total FROM order_items INNER JOIN dishes ON order_items.dish_name = dishes.title WHERE order_id = ?"
                val statement = connection.prepareStatement(sql)
                statement.setInt(1, orderId)
                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    totalAmount = resultSet.getDouble("total")
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while summing up an order: ${e.message}", ConsoleColor.RED))
        }
        return totalAmount
    }

    fun getOrdersByStatus(userId: Int, status: String): List<Order> {
        val orders = mutableListOf<Order>()
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT id, total_price FROM orders WHERE user_id = ? AND status = ?"
                val statement = connection.prepareStatement(sql)
                statement.setInt(1, userId)
                statement.setString(2, status)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val id = resultSet.getInt("id")
                    val totalAmount = resultSet.getDouble("total_price")
                    orders.add(Order(id, totalAmount, status))
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while getting an order: ${e.message}", ConsoleColor.RED))
        }
        return orders
    }

    fun updateOrderStatus(orderId: Int, status: String): Boolean {
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "UPDATE orders SET status = ? WHERE id = ?"
                val statement = connection.prepareStatement(sql)
                statement.setString(1, status)
                statement.setInt(2, orderId)
                statement.executeUpdate()
                return true
            }
        } catch (e: SQLException) {
            println("Error while updating status of an order: ${e.message}")
            return false
        }
    }

    fun getOrdersForVisitor(visitor: Visitor): List<Order> {
        val orders = mutableListOf<Order>()
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                val sql = "SELECT id, total_price, status FROM orders WHERE user_id = ?"
                connection.prepareStatement(sql).use { statement ->
                    statement.setInt(1, visitor.visitor.getIDByUsername(visitor.visitor.userName))
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        val orderId = resultSet.getInt("id")
                        val total_price = resultSet.getDouble("total_price")
                        val status = resultSet.getString("status")
                        val order = Order(orderId, total_price, status)
                        orders.add(order)
                    }
                }
            }
        } catch (e: SQLException) {
            println(coloredMessage("Error while trying to get an order: ${e.message}", ConsoleColor.RED))
        }
        return orders
    }
}
