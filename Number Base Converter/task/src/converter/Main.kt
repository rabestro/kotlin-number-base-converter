package converter

fun main() {
    while (true) {
        println("Do you want to convert /from decimal or /to decimal? (To quit type /exit)")
        when (readLine()!!){
            "/from" -> from()
            "/to" -> to()
            "/exit"-> break
        }
    }
}

fun from() {
    println("Enter number in decimal system:")
    val number = readLine()!!.toLong()
    println("Enter target base:")
    val radix = readLine()!!.toInt()
    val result = number.toString(radix)
    println("Conversion result: $result")
}

fun to() {
    println("Enter source number:")
    val number = readLine()!!
    println("Enter source base:")
    val radix = readLine()!!.toInt()
    val result = number.toLong(radix)
    println("Conversion to decimal result: $result")
}