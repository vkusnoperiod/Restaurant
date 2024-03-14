enum class ConsoleColor {
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, RESET
}

fun coloredMessage(text: String, color: ConsoleColor): String {
    val reset = "\u001b[0m"
    val colorCode = when (color) {
        ConsoleColor.BLACK -> "\u001b[30m"
        ConsoleColor.RED -> "\u001b[31m"
        ConsoleColor.GREEN -> "\u001b[32m"
        ConsoleColor.YELLOW -> "\u001b[33m"
        ConsoleColor.BLUE -> "\u001b[34m"
        ConsoleColor.MAGENTA -> "\u001b[35m"
        ConsoleColor.CYAN -> "\u001b[36m"
        ConsoleColor.WHITE -> "\u001b[37m"
        ConsoleColor.RESET -> reset
    }
    return "$colorCode$text$reset"
}

fun readValidNumber(prompt: String): Int? {
    while (true) {
        try {
            print(prompt)
            val number = readlnOrNull()?.toInt()
            return number

        } catch (e: NumberFormatException) {
            println(coloredMessage("Введенное значение не является целым числом. Повторите ввод.", ConsoleColor.RED))
        }
    }
}

fun main() {
    var running = true
    val func = Funcs()
    func.logIn()
    while (running) {
        if (func.user.userStatus == "Admin" || func.user.userStatus == "admin") {
            func.adminFuncs(func.user)
        } else if (func.user.userStatus == "Visitor" || func.user.userStatus == "visitor") {
            func.visitorFuncs(func.user)
        }
        running = func.logIn()
    }
}