package ru.restaurant

import java.util.*

class Actions {
    private val restaurant: Restaurant = Restaurant()
    val user = User()

    private fun logOut() : Boolean {
        println(coloredMessage("\nAre you sure you want to log out?", ConsoleColor.CYAN))
        println("1. Log out")
        println("2. Stay logged in")

        val choice = readValidNumber(coloredMessage("Enter action number: ", ConsoleColor.CYAN))
        when (choice) {
            1 -> {
                println(coloredMessage("\nLogging out...", ConsoleColor.CYAN))

                return true
            }
            2 -> {
                println(coloredMessage("\nStaying logged in.", ConsoleColor.CYAN))
                if(user.userStatus.lowercase() == "admin") {
                    adminActions(user)
                } else {
                    visitorAction(user)
                }
            }
            else -> {
                println(coloredMessage("Wrong number. Please, type in 1 or 2.", ConsoleColor.RED))
            }
        }
        return false
    }


    fun logIn() : Boolean {
        println(coloredMessage("\nDo you already have an account? (Yes or No)\nTo exit type in \"Esc\".", ConsoleColor.CYAN))
        var help = true
        while(help) {
            print(coloredMessage("Your pick: ", ConsoleColor.CYAN))
            val answer = readlnOrNull().toString().lowercase(Locale.getDefault())
            when (answer) {
                "yes" -> {
                    user.authentication()
                    help = false
                }

                "no" -> {
                    user.registration()
                    help = false
                }

                "esc" -> {
                    println(coloredMessage("\nSee you!", ConsoleColor.GREEN))
                    return false
                }

                else -> {
                    println(coloredMessage("Please type in \"Yes\" or \"No\".To exit type \"Esc\".", ConsoleColor.RED))
                    help = true
                }
            }
        }
        return true
    }

    fun visitorAction(user: User) {
        while (true) {
            println(coloredMessage("\nWhat do you want to do?", ConsoleColor.CYAN))
            println("1. Make an order")
            println("2. Pay the order")
            println("3. Add a dish to an order")
            println("4. Check status of an order")
            println("5. Leave a feedback on order")
            println("6. Cancel order")
            println("7. Log out")

            val orderChoice = readValidNumber(coloredMessage("Your pick: ", ConsoleColor.CYAN))
            val visitor = Visitor(user, restaurant)
            when (orderChoice) {
                1 -> {
                    visitor.createOrder()
                }
                2 -> {
                    visitor.payOrder()
                }
                3 -> {
                    visitor.addDish()
                }
                4 -> {
                    visitor.showOrderStatus()
                }
                5 -> {
                    visitor.feedback()
                }
                6 -> {
                    visitor.cancelOrder()
                }
                7 -> {
                    logOut()
                    return
                }
                else -> {
                    println(coloredMessage("Wrong number. Please, choose between 1 and 7.", ConsoleColor.RED))
                }
            }
        }
    }

    fun adminActions(user: User) {
        while(true) {
            println(coloredMessage("\nWhat do you want to do?", ConsoleColor.CYAN))
            println("1. Show the menu")
            println("2. Add a new dish")
            println("3. Revenue")
            println("4. Edit a dish")
            println("5. Delete a dish")
            println("6. Total users")
            println("7. Reviews")
            println("8. Log out")

            val choice = readValidNumber(coloredMessage("Enter an action number: ", ConsoleColor.CYAN))
            val admin = Admin(user, restaurant)
            when(choice) {
                1 -> {
                    admin.restaurant.showMenu()
                }
                2 -> {
                    admin.addDish()
                }
                3 -> {
                    admin.revenue()
                }
                4 -> {
                    admin.editDish()
                }
                5 -> {
                    admin.deleteDish()
                }
                6 -> {
                    admin.usersInSystem()
                }
                7 -> {
                    admin.showFeedbacks()
                }
                8 -> {
                    logOut()
                    return
                }
            }
        }
    }
}
