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
                        "Некорректный ввод данных. Повторите попытку.", ConsoleColor.RED
                    )
                )
                false
            }
        } catch (e: Exception) {
            println(coloredMessage("Ошибка: ${e.message}", ConsoleColor.RED))
            return false
        }
    }
}