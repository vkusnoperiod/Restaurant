import java.sql.DriverManager

class Admin(val admin: User, val restaurant: Restaurant) {
    private val jdbcUrl = "jdbc:mysql://localhost:3306/restaurant" // url вашей БД
    private val dbUsername = "restaurant"                               // название вашей БД
    private val dbPassword = ""                           // пароль вашей БД

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
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
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
            println("Ошибка: ${e.message}")
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
            println(coloredMessage("Ошибка при удалении блюда: ${e.message}", ConsoleColor.RED))
        }
        return false
    }

    private fun updateDishInDB(oldName: String, newName: String?, amount: Int?, price: Int?, time: Int?): Boolean {
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
            println(coloredMessage("Ошибка при обновлении блюда: ${e.message}", ConsoleColor.RED))
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
                        print(coloredMessage("\nСумма выручки составила: ", ConsoleColor.BLUE))
                        println("$revenue руб.")
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка при получении выручки: ${e.message}", ConsoleColor.RED))
        }
    }

    fun addDish() : Boolean {
        while(true) {
            try {
                print(coloredMessage("\nВведите название блюда: ", ConsoleColor.MAGENTA))
                val dishName = readlnOrNull().toString()
                val dishAmount = readValidNumber(coloredMessage("Введите количество: ", ConsoleColor.MAGENTA))
                val price = readValidNumber(coloredMessage("Укажите цену блюда: ", ConsoleColor.MAGENTA))
                val cookTime = readValidNumber(coloredMessage("Укажите время приготовления (в минутах): ", ConsoleColor.MAGENTA))
                val dish = Dish(dishName, dishAmount!!.toInt(), price!!.toInt(), cookTime!!.toInt())
                return if (dish.dishParse()) {
                    return if(!admin.DBChek(dishName)) {
                        addToDB(dishName, dishAmount, price, cookTime)
                        println(coloredMessage("\nБлюдо успешно добавлено!", ConsoleColor.GREEN))
                        false
                    } else {
                        println(coloredMessage("Блюдо с таким названием уже существует.", ConsoleColor.RED))
                        true
                    }
                } else {
                    println(
                        coloredMessage(
                            "Некорректный ввод данных. Повторите попытку.", ConsoleColor.RED
                        )
                    )
                    true
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
                return true
            }
        }
    }

    fun editDish() {
        while (true) {
            try {
                val menu: Map<String, Int> = restaurant.getMenu()
                if(menu.isEmpty()) {
                    println(coloredMessage("\nВ меню пока нет блюд :(", ConsoleColor.YELLOW))
                    break
                }
                restaurant.showMenu()
                print(coloredMessage("\nВведите название блюда для редактирования или 'Esc' для выхода: ", ConsoleColor.MAGENTA))
                val dishName = readlnOrNull().toString()

                if (dishName.equals("Esc", ignoreCase = true)) {
                    println(coloredMessage("\nРедактирование блюда отменено.", ConsoleColor.WHITE))
                    break
                }

                if (!dishCheck(dishName)) {
                    println(coloredMessage("Блюдо с таким названием не найдено.", ConsoleColor.RED))
                    continue
                }

                println(coloredMessage("\nВыберите параметры для редактирования (введите числа через запятую):\n1. Название\n2. Количество\n3. Цена\n4. Время приготовления", ConsoleColor.BLUE))
                print(coloredMessage("Ваш ввод: ", ConsoleColor.MAGENTA))
                val choicesInput = readln().split(",").map { it.trim().toIntOrNull() }

                if (choicesInput.isEmpty() || choicesInput.any { it !in 1..4 }) {
                    println(coloredMessage("Ошибка. Выберите параметры для редактирования (введите числа от 1 до 4).", ConsoleColor.RED))
                    continue
                }

                val choices = choicesInput.mapNotNull { it }

                var newName: String? = null
                var newAmount: Int? = null
                var newPrice: Int? = null
                var newCookTime: Int? = null

                if (choices.contains(1)) {
                    print(coloredMessage("Введите новое название блюда: ", ConsoleColor.MAGENTA))
                    newName = readlnOrNull()
                }
                if (choices.contains(2)) {
                    newAmount = readValidNumber(coloredMessage("Введите новое количество: ", ConsoleColor.MAGENTA))
                }
                if (choices.contains(3)) {
                    newPrice = readValidNumber(coloredMessage("Введите новую цену блюда: ", ConsoleColor.MAGENTA))
                }
                if (choices.contains(4)) {
                    newCookTime = readValidNumber(coloredMessage("Введите новое время приготовления (в минутах): ", ConsoleColor.MAGENTA))
                }

                if (updateDishInDB(dishName, newName, newAmount, newPrice, newCookTime)) {
                    println(coloredMessage("\nБлюдо успешно обновлено!", ConsoleColor.GREEN))
                    break
                } else {
                    println(coloredMessage("\nОшибка при обновлении блюда.", ConsoleColor.RED))
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
            }
        }
    }

    fun deleteDish() {
        while (true) {
            try {
                val menu: Map<String, Int> = restaurant.getMenu()
                if(menu.isEmpty()) {
                    println(coloredMessage("\nВ меню пока нет блюд :(", ConsoleColor.YELLOW))
                    break
                }
                restaurant.showMenu()
                print(coloredMessage("\nВведите название блюда для удаления или 'Esc' для выхода: ", ConsoleColor.MAGENTA))
                val userInput = readlnOrNull()?.toString() ?: continue

                if (userInput.equals("Esc", ignoreCase = true)) {
                    println(coloredMessage("\nУдаление блюда отменено.", ConsoleColor.WHITE))
                    return
                }

                if (dishCheck(userInput)) {
                    if (deleteFromDB(userInput)) {
                        println(coloredMessage("\nБлюдо успешно удалено из меню.", ConsoleColor.GREEN))
                        break
                    } else {
                        println(coloredMessage("Ошибка. Блюдо с таким названием не найдено.", ConsoleColor.RED))
                    }
                } else {
                    println(
                        coloredMessage(
                            "Некорректный ввод данных. Повторите попытку.", ConsoleColor.RED
                        )
                    )
                }
            } catch (e: Exception) {
                println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
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
                        println(coloredMessage("\nПользователи еще не оставили отзывы :(", ConsoleColor.YELLOW))
                    } else {
                        println(coloredMessage("\nОтзывы: ", ConsoleColor.BLUE))
                        while (resultSet.next()) {
                            val feedbackId = resultSet.getInt("id")
                            val orderId = resultSet.getInt("order_id")
                            val rating = resultSet.getInt("rating")
                            val feedbackText = resultSet.getString("comment")
                            println("$count. Отзыв #$feedbackId от заказа с ID $orderId: оценка: $rating | комментарий: $feedbackText")
                            count++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка при получении отзывов: ${e.message}", ConsoleColor.RED))
        }
    }

    fun usersInSystem() {
        val sql = "SELECT username, status FROM users"
        try {
            DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword).use { connection ->
                connection.prepareStatement(sql).use { statement ->
                    val resultSet = statement.executeQuery()
                    var count = 1
                    println(coloredMessage("\nПользователи в системе: ", ConsoleColor.BLUE))
                    println("Имя пользователя | Статус")
                    while (resultSet.next()) {
                        val username = resultSet.getString("username")
                        val status = resultSet.getString("status")
                        println("$count. $username | $status")
                        count++
                    }
                }
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка при получении пользователей: ${e.message}", ConsoleColor.RED))
        }
    }
}