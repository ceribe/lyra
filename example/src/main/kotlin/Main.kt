fun main() {
    Lyra().apply {
        register<ExampleMessage>()
    }
//    println(ExampleMessage(42).serialize())
}