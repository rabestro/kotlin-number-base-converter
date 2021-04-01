package converter

fun main() {
    menu1()
}

fun menu1() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        val data = readLine()!!
        if (data == "/exit") {
            return
        }
        val (sourceBase, targetBase) = data.split(' ').map(String::toInt)
        menu2(sourceBase, targetBase)
    }
}

fun menu2(sourceBase: Int, targetBase: Int) {
    while (true) {
        println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
        val data = readLine()!!
        if (data == "/back") {
            return
        }
        val number = data.toBigInteger(sourceBase)
        val result = number.toString(targetBase)
        println("Conversion result: $result")
    }
}
