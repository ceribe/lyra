package example

import lyra.Lyra

fun main() {
    Lyra().apply {
        register<ExampleMessage>()
    }
//    println(example.ExampleMessage(42).serialize())
}