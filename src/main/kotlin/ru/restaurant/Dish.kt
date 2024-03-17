package ru.restaurant

class Dish(val name: String, val amount: Int, val price: Int, val time: Int) {
    fun dishParse() : Boolean {
        try {
            return if ((name != "" && name.isNotEmpty() && !name.startsWith(" ")) ||
                (amount > 0) || (price > 0) || (time >= 0))
            {
                true
            } else {
                println(
                    coloredMessage(
                        "Wrong data. Please try again.", ConsoleColor.RED
                    )
                )
                false
            }
        } catch (e: Exception) {
            println(coloredMessage("Error: ${e.message}", ConsoleColor.RED))
            return false
        }
    }
}