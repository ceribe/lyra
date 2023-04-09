@kotlinx.serialization.Serializable
data class ExampleMessage(val someNumber: Int) : Message<NodeState>() {
    override suspend fun react() {
        // do work
        waitFor { false } // some condition
        // do work
        waitFor { true } // some condition
        // do work
    }
}