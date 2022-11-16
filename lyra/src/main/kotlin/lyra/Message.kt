package lyra

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
abstract class Message {
    abstract fun react()
    fun serialize(): String {
        return Json.encodeToString(this)
    }
}