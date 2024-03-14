import java.util.*

class Funcs {
    private val restaurant: Restaurant = Restaurant()
    val user = User()

    private fun logOut() : Boolean {
        println(coloredMessage("\nЧтобы выйти из аккаунта, выберите один из вариантов:", ConsoleColor.BLUE))
        println("1. Выйти из аккаунта")
        println("2. Остаться в аккаунте")

        val choice = readValidNumber(coloredMessage("Введите номер соответствующего действия: ", ConsoleColor.MAGENTA))
        when (choice) {
            1 -> {
                println(coloredMessage("\nВыход из аккаунта...", ConsoleColor.WHITE))

                return true
            }
            2 -> {
                println(coloredMessage("\nОстаемся в аккаунте.", ConsoleColor.WHITE))
                if(user.userStatus == "Admin" || user.userStatus == "admin") {
                    adminFuncs(user)
                } else {
                    visitorFuncs(user)
                }
            }
            else -> {
                println(coloredMessage("Неверный выбор. Пожалуйста, введите номер 1 или 2.", ConsoleColor.RED))
            }
        }
        return false
    }


    fun logIn() : Boolean {
        println(coloredMessage("\nУ вас уже есть аккаунт? (Да/Нет)\nДля выхода введите Esc.", ConsoleColor.YELLOW))
        var help = true
        while(help) {
            print(coloredMessage("Ваш ввод: ", ConsoleColor.MAGENTA))
            val answer = readlnOrNull().toString().lowercase(Locale.getDefault())
            when (answer) {
                "да" -> {
                    user.authentication()
                    help = false
                }

                "нет" -> {
                    user.registration()
                    help = false
                }

                "esc" -> {
                    println(coloredMessage("\nДо свидания!", ConsoleColor.GREEN))
                    return false
                }

                else -> {
                    println(coloredMessage("Пожалуйста, введите 'Да' или 'Нет' (или 'Esc' для выхода).", ConsoleColor.RED))
                    help = true
                }
            }
        }
        return true
    }

    fun visitorFuncs(user: User) {
        while (true) {
            println(coloredMessage("\nВыберите действие:", ConsoleColor.BLUE))
            println("1. Сделать заказ")
            println("2. Отменить заказ")
            println("3. Добавить блюдо в заказ")
            println("4. Посмотреть статус заказа")
            println("5. Оплатить заказ")
            println("6. Оценить заказ")
            println("7. Выйти из аккаунта")

            val orderChoice = readValidNumber(coloredMessage("Введите номер соответствующего действия: ", ConsoleColor.BLUE))
            val visitor = Visitor(user, restaurant)
            when (orderChoice) {
                1 -> {
                    visitor.makeOrder()
                }
                2 -> {
                    visitor.cancelOrder()
                }
                3 -> {
                    visitor.addDish()
                }
                4 -> {
                    visitor.showOrderStatus()
                }
                5 -> {
                    visitor.payOrder()
                }
                6 -> {
                    visitor.feedback()
                }
                7 -> {
                    logOut()
                    return
                }
                else -> {
                    println(coloredMessage("Неверный выбор. Пожалуйста, введите номер от 1 до 6.", ConsoleColor.RED))
                }
            }
        }
    }

    fun adminFuncs(user: User) {
        while(true) {
            println(coloredMessage("\nВыберите действие:", ConsoleColor.BLUE))
            println("1. Добавить блюдо")
            println("2. Посмотреть меню")
            println("3. Изменить блюдо")
            println("4. Удалить блюдо")
            println("5. Посмотреть отзывы")
            println("6. Сумма выручки")
            println("7. Пользователи в системе")
            println("8. Выйти из аккаунта")

            val choice = readValidNumber(coloredMessage("Введите номер соответствующего действия: ", ConsoleColor.BLUE))
            val admin = Admin(user, restaurant)
            when(choice) {
                1 -> {
                    admin.addDish()
                }
                2 -> {
                    admin.restaurant.showMenu()
                }
                3 -> {
                    admin.editDish()
                }
                4 -> {
                    admin.deleteDish()
                }
                5 -> {
                    admin.showFeedbacks()
                }
                6 -> {
                    admin.revenue()
                }
                7 -> {
                    admin.usersInSystem()
                }
                8 -> {
                    logOut()
                    return
                }
            }
        }
    }
}
