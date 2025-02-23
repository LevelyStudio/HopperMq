package gg.levely.system.hoppermq.packet

import gg.levely.system.hoppermq.event.RabbitEvent
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception

abstract class RabbitPacket : RabbitEvent {

    val metadata = mutableMapOf<String, Any>()

    @Throws(Exception::class)
    abstract fun write(output: DataOutputStream)

    @Throws(Exception::class)
    abstract fun read(input: DataInputStream)

    val author: String?
        get() = metadata["author"] as String?
}