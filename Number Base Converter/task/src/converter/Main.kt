package converter

fun main() {
    println("Enter number in decimal system:")
    val number = readLine()!!.toLong()
    println("Enter target base:")
    val radix = readLine()!!.toInt()
    val result = number.toString(radix)
    println("Conversion result: $result")
}