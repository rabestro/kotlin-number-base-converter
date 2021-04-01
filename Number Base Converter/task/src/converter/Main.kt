package converter

fun main() {
    menu1()
}

fun menu1() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        when (val data = readLine()!!) {
            "/exit" -> return
            else -> menu2(data)
        }
    }
}

fun menu2(data: String) {
    val (sourceBase, targetBase) = data.split(' ').map(String::toInt)
    while (true) {
        println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
        when (val number = readLine()!!) {
            "/back" -> return
            else -> println("Conversion result: ${number.toBigInteger(sourceBase).toString(targetBase)}")
        }
    }
}
