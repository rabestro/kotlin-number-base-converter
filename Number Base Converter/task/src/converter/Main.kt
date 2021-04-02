package converter

fun main() {
    menu1()
}

fun menu1() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        when (val base = readLine()!!) {
            "/exit" -> return
            else -> menu2(base)
        }
    }
}

fun menu2(base: String) {
    val (sourceBase, targetBase) = base.split(' ').map(String::toInt)
    while (true) {
        println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
        when (val number = readLine()!!) {
            "/back" -> return
            else -> convert(sourceBase, targetBase, number)
        }
    }
}

fun convert(sourceBase: Int, targetBase: Int, number: String) {
    val digits = "0123456789abcdefghijklmnopqrstuvwxyz"

    print("Conversion result: ")

    if (number.contains('.')) {
        val (sourceWhole, sourceFraction) = number.split('.')
        val targetWhole = sourceWhole.toBigInteger(sourceBase).toString(targetBase)
        var decimalFraction = 0.0
        var divider = sourceBase.toDouble()
        sourceFraction.forEach {
            decimalFraction += digits.indexOf(it) / divider
            divider *= sourceBase
        }
        var targetFraction = "";
        repeat(5) {
            decimalFraction *= targetBase
            val index = decimalFraction.toInt()
            targetFraction += digits[index]
            decimalFraction -= index
        }
        println("$targetWhole.$targetFraction")
    } else {
        println(number.toBigInteger(sourceBase).toString(targetBase))
    }
}
