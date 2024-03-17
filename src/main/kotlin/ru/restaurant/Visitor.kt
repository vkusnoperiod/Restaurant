import java.util.*

class Visitor(val visitor: User, val restaurant: Restaurant) {
    fun addDish(): Boolean {
        try {
            val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готовится")
            if (orders.isEmpty()) {
                println(coloredMessage("\nУ вас пока нет заказов, которые готовятся.", ConsoleColor.YELLOW))
                return false
            }
            println(coloredMessage("\nВаши заказы, которые готовятся:", ConsoleColor.BLUE))
            orders.forEachIndexed { index, order ->
                println("${index + 1}. Номер заказа: ${order.id} | Цена: ${order.totalAmount} | Статус: ${order.status}")
            }
            print(coloredMessage("Введите номер, к которому хотите добавить блюдо: ", ConsoleColor.MAGENTA))
            val orderId = readLine()?.toIntOrNull() ?: return false
            val order = orders.getOrNull(orderId - 1)
            if (order != null) {
                restaurant.showMenu()
                val availableDishes = restaurant.getMenu()

                print(coloredMessage("Введите название блюда: ", ConsoleColor.MAGENTA))
                val dishName = readLine() ?: ""

                if (availableDishes.containsKey(dishName)) {
                    val availableAmount = availableDishes[dishName]!!

                    print(coloredMessage("Введите количество: ", ConsoleColor.MAGENTA))
                    val quantity = readLine()?.toIntOrNull() ?: 0

                    return if (quantity in 1..availableAmount) {
                        restaurant.addDishToOrder(order.id, dishName, quantity)
                        println(coloredMessage("\nБлюдо успешно добавлено в заказ.", ConsoleColor.GREEN))
                        true
                    } else {
                        println(coloredMessage("Недостаточное количество блюда \"$dishName\" для добавления.", ConsoleColor.RED))
                        false
                    }
                } else {
                    println(coloredMessage("Блюдо \"$dishName\" отсутствует в меню.", ConsoleColor.RED))
                    return false
                }
            } else {
                println(coloredMessage("Неверный номер заказа.", ConsoleColor.RED))
                return false
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка при добавлении блюда в заказ: ${e.message}", ConsoleColor.RED))
            return false
        }
    }

    fun payOrder(): Boolean {
        while (true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готов")
                if (orders.isEmpty()) {
                    println(coloredMessage("\nУ вас пока нет готовых к оплате заказов.", ConsoleColor.YELLOW))
                    return false
                }
                println(coloredMessage("\nВаши заказы, которые готовы:", ConsoleColor.BLUE))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Номер заказа: ${order.id} | Цена: ${order.totalAmount} | Статус: ${order.status}")
                }
                var orderId: Int? = null
                while (orderId == null) {
                    print(coloredMessage("Введите номер для оплаты: ", ConsoleColor.MAGENTA))
                    orderId = readLine()?.toIntOrNull()
                    if (orderId == null || orderId !in 1..orders.size) {
                        println(coloredMessage("Неверный номер заказа.", ConsoleColor.RED))
                        orderId = null
                    }
                }
                val order = orders[orderId - 1]
                val totalAmount = restaurant.getTotalAmount(order.id)
                println(coloredMessage("Заказ готов. Сумма к оплате: $totalAmount.", ConsoleColor.BLUE))
                var userInput: String?
                while (true) {
                    print("Пожалуйста, напишите \"оплатить\", чтобы оплатить заказ: ")
                    userInput = readlnOrNull()
                    if (userInput.equals("оплатить", ignoreCase = true)) {
                        restaurant.updateOrderStatus(order.id, "Оплачен")
                        println(coloredMessage("\nЗаказ успешно оплачен.", ConsoleColor.GREEN))
                        return true
                    } else {
                        println(coloredMessage("Необходимо написать \"оплатить\", чтобы оплатить заказ.", ConsoleColor.RED))
                    }
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка при оплате заказа: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun feedback(): Boolean {
        while (true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Оплачен")
                if (orders.isEmpty()) {
                    println(coloredMessage("У вас пока нет заказов.", ConsoleColor.YELLOW))
                    return false
                }
                println(coloredMessage("\nВаши заказы, которые готовы:", ConsoleColor.BLUE))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Номер заказа: ${order.id} | Цена: ${order.totalAmount} | Статус: ${order.status}")
                }
                var orderId: Int
                var rating: Int
                var comment: String
                while (true) {
                    print(coloredMessage("Введите номер для того, чтобы оставить отзыва: ", ConsoleColor.MAGENTA))
                    val orderInput = readLine() ?: ""
                    orderId = orderInput.toIntOrNull() ?: 0
                    if (orderId !in 1..orders.size) {
                        println(coloredMessage("Некорректный номер заказа. Пожалуйста, введите снова.", ConsoleColor.RED))
                        continue
                    }
                    val order = orders[orderId - 1]
                    while (true) {
                        print(coloredMessage("Оцените заказ от 1 до 5: ", ConsoleColor.MAGENTA))
                        val ratingInput = readLine() ?: ""
                        rating = ratingInput.toIntOrNull() ?: 0
                        if (rating !in 1..5) {
                            println(coloredMessage("Некорректная оценка. Пожалуйста, введите число от 1 до 5.", ConsoleColor.RED))
                            continue
                        }
                        break
                    }
                    while (true) {
                        print(coloredMessage("Введите комментарий: ", ConsoleColor.MAGENTA))
                        comment = readLine() ?: ""
                        if (comment.isNotBlank()) {
                            break
                        } else {
                            println(coloredMessage("Комментарий не может быть пустым. Пожалуйста, введите комментарий.", ConsoleColor.RED))
                        }
                    }
                    restaurant.saveFeedback(order.id, rating, comment)
                    println(coloredMessage("\nСпасибо за ваш отзыв!", ConsoleColor.GREEN))
                    return true
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка при оставлении отзыва: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun makeOrder(): Boolean {
        while (true) {
            try {
                restaurant.showMenu()

                val availableDishes = restaurant.getMenu()
                if(availableDishes.isEmpty()) {
                    println(coloredMessage("\nВ меню пока нет блюд :(", ConsoleColor.YELLOW))
                    return false
                }

                val order = mutableMapOf<String, Int>()
                while (true) {
                    print(coloredMessage("Введите название блюда (или \"готово\", чтобы завершить заказ): ", ConsoleColor.MAGENTA))
                    val dishName = readlnOrNull() ?: ""
                    if (dishName.lowercase(Locale.getDefault()) == "готово") {
                        break
                    }

                    val quantity = readValidNumber(coloredMessage("Введите количество: ", ConsoleColor.MAGENTA))

                    if (availableDishes.containsKey(dishName)) {
                        if (quantity!!.toInt() > 0 && quantity <= availableDishes[dishName]!!) {
                            order[dishName] = quantity
                            println(coloredMessage("\nЗаказ принят! Ожидайте 2 минуты.\n", ConsoleColor.GREEN))
                        } else {
                            println(coloredMessage("Недостаточное количество блюда \"$dishName\" для заказа.", ConsoleColor.RED))
                        }
                    } else {
                        println(coloredMessage("Блюдо \"$dishName\" отсутствует в меню.", ConsoleColor.RED))
                    }
                }

                if (order.isEmpty()) {
                    println(coloredMessage("\nВыходим...", ConsoleColor.WHITE))
                    return false
                }

                val userId = visitor.getIDByUsername(visitor.userName)
                val orderId = restaurant.addOrderToDB(userId, order, "Принят")
                return if (orderId != -1) {
                    println(coloredMessage("\nЗаказ успешно размещен!", ConsoleColor.GREEN))

                    val updateStatusThread = Thread {
                        try {
                            Thread.sleep(2000)
                            restaurant.updateOrderStatus(orderId, "Готовится")

                            Thread.sleep(120000)
                            restaurant.updateOrderStatus(orderId, "Готов")
                        } catch (e: InterruptedException) {
                            println(coloredMessage("Ошибка при ожидании обновления статуса заказа: ${e.message}", ConsoleColor.RED))
                        }
                    }
                    updateStatusThread.start()
                    true
                } else {
                    println(coloredMessage("Ошибка при размещении заказа.", ConsoleColor.RED))
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка при размещении заказа: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun cancelOrder(): Boolean {
        while(true) {
            try {
                val orders = restaurant.getOrdersByStatus(visitor.getIDByUsername(visitor.userName), "Готовится")
                if (orders.isEmpty()) {
                    println(coloredMessage("\nУ вас пока нет заказов, которые готовятся.", ConsoleColor.YELLOW))
                    return false
                }
                println(coloredMessage("\nВаши заказы, которые готовятся:", ConsoleColor.BLUE))
                orders.forEachIndexed { index, order ->
                    println("${index + 1}. Номер заказа: ${order.id} | Цена: ${order.totalAmount} | Статус: ${order.status}")
                }
                print(coloredMessage("Введите номер для отмены: ", ConsoleColor.MAGENTA))
                val orderId = readLine()?.toIntOrNull() ?: return false
                val order = orders.getOrNull(orderId - 1)
                return if (order != null) {
                    restaurant.cancelOrder(order.id)
                    println(coloredMessage("\nЗаказ успешно отменен.", ConsoleColor.GREEN))
                    true
                } else {
                    println(coloredMessage("Неверный номер заказа.", ConsoleColor.RED))
                    false
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка при отмене заказа: ${e.message}", ConsoleColor.RED))
                return false
            }
        }
    }

    fun showOrderStatus() {
        val orders = restaurant.getOrdersForVisitor(Visitor(visitor, restaurant))
        if (orders.isEmpty()) {
            println(coloredMessage("У вас нет заказов.", ConsoleColor.YELLOW))
        } else {
            println(coloredMessage("\nВсе ваши заказы:", ConsoleColor.BLUE))
            for ((index, order) in orders.withIndex()) {
                println("${index + 1}. Номер заказа: ${order.id} | Статус: ${order.status}")
            }
        }
    }
}