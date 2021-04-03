package converter

fun main() {
    while (true) {
        println("Enter two numbers in format: {source base} {target base} (To quit type /exit)")
        when (val userInput = readLine()!!) {
            "/exit" -> return
            else -> convert(userInput)
        }
    }
}

fun convert(fromToBase: String) {
    val (sourceBase, targetBase) = fromToBase.split(' ').map(String::toInt)
    while (true) {
        println("Enter number in base $sourceBase to convert to base $targetBase (To go back type /back)")
        when (val number = readLine()!!) {
            "/back" -> return
            else -> println("Conversion result: " + number.fromToRadix(sourceBase, targetBase))
        }
    }
}

fun String.fromToRadix(sourceBase: Int, targetBase: Int): String {
    val digits = "0123456789abcdefghijklmnopqrstuvwxyz"
    if (!this.contains('.')) {
        return this.toBigInteger(sourceBase).toString(targetBase)
    }
    val (sourceWhole, sourceFraction) = this.split('.')
    val targetWhole = sourceWhole.toBigInteger(sourceBase).toString(targetBase)
    var decimalFraction = 0.0
    var divider = sourceBase.toDouble()
    sourceFraction.forEach {
        decimalFraction += digits.indexOf(it) / divider
        divider *= sourceBase
    }
    var targetFraction = ""
    repeat(5) {
        decimalFraction *= targetBase
        val index = decimalFraction.toInt()
        targetFraction += digits[index]
        decimalFraction -= index
    }
    return "$targetWhole.$targetFraction"
}
