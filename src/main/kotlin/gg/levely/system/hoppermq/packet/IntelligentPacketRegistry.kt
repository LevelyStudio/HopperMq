package gg.levely.system.hoppermq.packet

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object IntelligentPacketRegistry {

    private val logger: Logger = LoggerFactory.getLogger(IntelligentPacketRegistry::class.java)

    fun register(packageName: String, packetRegistry: PacketRegistry) {
        val reflections = Reflections(packageName)

        reflections.getTypesAnnotatedWith(RabbitPacketLabel::class.java).forEach { clazz ->
            clazz.getAnnotation(RabbitPacketLabel::class.java)?.let { annotation ->
                val id = annotation.value.takeIf { it.isNotEmpty() } ?: clazz.simpleName
                @Suppress("UNCHECKED_CAST")
                packetRegistry.register(id, clazz as Class<out RabbitPacket>)
                logger.info("$id has been registered into packetRegistry")
            }
        }
    }
}
