@kotlinx.serialization.Serializable
data class ExampleMessage(val someNumber: Int) : Message {
    override fun react() {
        TODO("Not yet implemented")
    }
}