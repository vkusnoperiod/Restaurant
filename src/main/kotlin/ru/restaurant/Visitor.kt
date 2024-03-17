package ru.restaurant

import java.util.*

class Visitor(val visitor: User, val restaurant: Restaurant) {
    fun addDish(): Boolean {
        try {
            val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готовится")
            if (orders.isEmpty()) {
                println(coloredMessage("\nYou dont have any pending orders.", ConsoleColor.CYAN))
                return false
            }
            println(coloredMessage("\nPending orders:", ConsoleColor.CYAN))
            orders.forEachIndexed { index, order ->
                println("${index + 1}. Order №: ${order.id} | Price: ${order.totalAmount} | Status: ${order.status}")
            }
            print(coloredMessage("Add a number of an order to edit: ", ConsoleColor.CYAN))
            val orderId = readLine()?.toIntOrNull() ?: return false
            val order = orders.getOrNull(orderId - 1)
            if (order != null) {
                restaurant.showMenu()
                val availableDishes = restaurant.getMenu()

                print(coloredMessage("Enter dish: ", ConsoleColor.CYAN))
                val dishName = readLine() ?: ""

                if (availableDishes.containsKey(dishName)) {
                    val availableAmount = availableDishes[dishName]!!

                    print(coloredMessage("How many: ", ConsoleColor.CYAN))
                    val quantity = readLine()?.toIntOrNull() ?: 0

                    return if (quantity in 1..availableAmount) {
                        restaurant.addDishToOrder(order.id, dishName, quantity)
                        println(coloredMessage("\nDishes are added to order.", ConsoleColor.GREEN))
                        true
                    } else {
                        println(coloredMessage("Not enough dishes \"$dishName\" left.", ConsoleColor.RED))
                        false
                    }
                } else {
                    println(coloredMessage("Dish \"$dishName\" is not in the menu.", ConsoleColor.RED))
                    return false
                }
            } else {
                println(coloredMessage("Wrong number of an order.", ConsoleColor.RED))
                return false
            }
        } catch (e: Exception) {
            println(coloredMessage("Error while adding a dish to an order: ${e.message}", ConsoleColor.RED))
            return false
        }
    }

    fun payOrder(): Boolean {
        while (true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готов")
                if (orders.isEmpty()) {
                    println(coloredMessage("\nYou dont have any orders to pay for yet.", ConsoleColor.CYAN))
                    return false
                }
                println(coloredMessage("\nCompleted orders:", ConsoleColor.CYAN))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Order №: ${order.id} | Price: ${order.totalAmount} | Status: ${order.status}")
                }
                var orderId: Int? = null
                while (orderId == null) {
                    print(coloredMessage("Enter order №: ", ConsoleColor.CYAN))
                    orderId = readLine()?.toIntOrNull()
                    if (orderId == null || orderId !in 1..orders.size) {
                        println(coloredMessage("Wrong order №. Please try again", ConsoleColor.RED))
                        orderId = null
                    }
                }
                val order = orders[orderId - 1]
                val totalAmount = restaurant.getTotalAmount(order.id)
                println(coloredMessage("Order is ready. To pay: $totalAmount.", ConsoleColor.CYAN))
                var userInput: String?
                while (true) {
                    print("Please, type in \"pay\", to pay the bill: ")
                    userInput = readlnOrNull()
                    if (userInput.equals("pay", ignoreCase = true)) {
                        restaurant.updateOrderStatus(order.id, "Оплачен")
                        println(coloredMessage("\nOrder is paid.", ConsoleColor.GREEN))
                        return true
                    } else {
                        println(coloredMessage("You have to type in \"pay\" to pay the bill.", ConsoleColor.RED))
                    }
                }
            } catch (e: Exception) {
                println(coloredMessage("Error occurred while trying to process the payment ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun feedback(): Boolean {
        while (true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Оплачен")
                if (orders.isEmpty()) {
                    println(coloredMessage("You dont have any orders yet.", ConsoleColor.WHITE))
                    return false
                }
                println(coloredMessage("\nCompleted orders:", ConsoleColor.WHITE))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Order number: ${order.id} | Price: ${order.totalAmount} | Status: ${order.status}")
                }
                var orderId: Int
                var rating: Int
                var comment: String
                while (true) {
                    print(coloredMessage("In order to leave a feedback you have to pick " +
                            "a number between 1 and 5 ", ConsoleColor.WHITE))
                    val orderInput = readLine() ?: ""
                    orderId = orderInput.toIntOrNull() ?: 0
                    if (orderId !in 1..orders.size) {
                        println(coloredMessage("Wrong order №. Please try again.", ConsoleColor.RED))
                        continue
                    }
                    val order = orders[orderId - 1]
                    while (true) {
                        print(coloredMessage("Rate an order between 1 до 5: ", ConsoleColor.CYAN))
                        val ratingInput = readLine() ?: ""
                        rating = ratingInput.toIntOrNull() ?: 0
                        if (rating !in 1..5) {
                            println(coloredMessage("Wrong rate. Please, choose between 1 and 5.", ConsoleColor.RED))
                            continue
                        }
                        break
                    }
                    while (true) {
                        print(coloredMessage("Your feedback: ", ConsoleColor.CYAN))
                        comment = readLine() ?: ""
                        if (comment.isNotBlank()) {
                            break
                        } else {
                            println(coloredMessage("Feedback cant be empty. Please type in something", ConsoleColor.RED))
                        }
                    }
                    restaurant.saveFeedback(order.id, rating, comment)
                    println(coloredMessage("\nThank you for your feedback!", ConsoleColor.GREEN))
                    return true
                }
            } catch (e: Exception) {
                println(coloredMessage("Error leaving a feedback: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun createOrder(): Boolean {
        while (true) {
            try {
                restaurant.showMenu()

                val availableDishes = restaurant.getMenu()
                if(availableDishes.isEmpty()) {
                    println(coloredMessage("\nThe menu is empty", ConsoleColor.CYAN))
                    return false
                }

                val order = mutableMapOf<String, Int>()
                while (true) {
                    print(coloredMessage("Enter dish name or \"done\" to finish the order: ", ConsoleColor.CYAN))
                    val dishName = readlnOrNull() ?: ""
                    if (dishName.lowercase(Locale.getDefault()) == "done") {
                        break
                    }

                    val quantity = readValidNumber(coloredMessage("How many: ", ConsoleColor.CYAN))

                    if (availableDishes.containsKey(dishName)) {
                        if (quantity!!.toInt() > 0 && quantity <= availableDishes[dishName]!!) {
                            order[dishName] = quantity
                            println(coloredMessage("\nOrder is accepted! Wait for about 2 minutes.\n", ConsoleColor.GREEN))
                        } else {
                            println(coloredMessage("We dont have that many \"$dishName\"(s) to order.", ConsoleColor.RED))
                        }
                    } else {
                        println(coloredMessage("\"$dishName\" is unavailable.", ConsoleColor.RED))
                    }
                }

                if (order.isEmpty()) {
                    println(coloredMessage("\nExiting..", ConsoleColor.CYAN))
                    return false
                }

                val userId = visitor.getIDByUsername(visitor.userName)
                val orderId = restaurant.addOrderToDB(userId, order, "Принят")
                return if (orderId != -1) {
                    println(coloredMessage("\nOrder has been created!", ConsoleColor.GREEN))

                    val updateStatusThread = Thread {
                        try {
                            Thread.sleep(2000)
                            restaurant.updateOrderStatus(orderId, "Готовится")

                            Thread.sleep(120000)
                            restaurant.updateOrderStatus(orderId, "Готов")
                        } catch (e: InterruptedException) {
                            println(coloredMessage("Error while waiting for the order status to update: ${e.message}", ConsoleColor.RED))
                        }
                    }
                    updateStatusThread.start()
                    true
                } else {
                    println(coloredMessage("Error occurred while trying to create an order.", ConsoleColor.RED))
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Error creating an order: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun cancelOrder(): Boolean {
        while(true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готовится")
                if (orders.isEmpty()) {
                    println(coloredMessage("\nYou dont have any orders is progress yet.", ConsoleColor.CYAN))
                    return false
                }
                println(coloredMessage("\nOrders to be done:", ConsoleColor.BLUE))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Order №: ${order.id} | Price: ${order.totalAmount} | Status: ${order.status}")
                }
                print(coloredMessage("Enter a number of an order to delete: ", ConsoleColor.WHITE))
                val orderId = readLine()?.toIntOrNull() ?: return false
                val order = orders.getOrNull(orderId - 1)
                return if (order != null) {
                    restaurant.cancelOrder(order.id)
                    println(coloredMessage("\nOrder is cancelled.", ConsoleColor.GREEN))
                    true
                } else {
                    println(coloredMessage("Wrong order number.", ConsoleColor.RED))
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Error occurred while deleting an order: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun showOrderStatus() {
        val orders = restaurant.getOrdersForVisitor(Visitor(visitor, restaurant))
        if (orders.isEmpty()) {
            println(coloredMessage("You dont have any orders yet.", ConsoleColor.CYAN))
        } else {
            println(coloredMessage("\nYour orders:", ConsoleColor.CYAN))
            for ((index, order) in orders.withIndex()) {
                println("${index + 1}. Order №: ${order.id} | Status: ${order.status}")
            }
        }
    }
}