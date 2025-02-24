package gg.levely.system.hoppermq.packet

import org.reflections.Reflections
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor

class PacketRegistry {

    private val logger: Logger = LoggerFactory.getLogger(PacketRegistry::class.java)

    private val idToPacket: MutableMap<String, Class<out RabbitPacket>> = mutableMapOf()
    private val packetToId: MutableMap<Class<out RabbitPacket>, String> = mutableMapOf()
    private val constructorPacket: MutableMap<Class<out RabbitPacket>, Constructor<out RabbitPacket>> = mutableMapOf()

    fun register(packet: Class<out RabbitPacket>) {

        if (!packet.isAnnotationPresent(RabbitPacketLabel::class.java)) {
            logger.error("${packet.simpleName} does not have RabbitPacketLabel annotation")
            return
        }

        val label = packet.getAnnotation<RabbitPacketLabel>(RabbitPacketLabel::class.java)
        val id = label.value.takeIf { it.isNotEmpty() } ?: packet.simpleName


        if (id in idToPacket) {
            logger.error("$id has already been used by another packet (${idToPacket[id]})")
            return
        }

        idToPacket[id] = packet
        packetToId[packet] = id

        try {
            val declaredConstructor = packet.getDeclaredConstructor().apply { isAccessible = true }
            constructorPacket[packet] = declaredConstructor
            logger.debug("Registered packet ${packet.simpleName} with id $id")
        } catch (e: NoSuchMethodException) {
            logger.error("Cannot find empty constructor in ${packet.simpleName}", e)
        }
    }

    fun register(packageName: String) {
        val reflections = Reflections(packageName)

        reflections.getTypesAnnotatedWith(RabbitPacketLabel::class.java).forEach { clazz ->
            @Suppress("UNCHECKED_CAST")
            this.register(clazz as Class<out RabbitPacket>)
        }
    }

    fun getPacket(id: String): RabbitPacket? {
        val packetClass = idToPacket[id] ?: return null
        val constructor = constructorPacket[packetClass] ?: return null

        return runCatching {
            constructor.newInstance()
        }.getOrElse {
            logger.error("Cannot create instance for ${packetClass.simpleName}", it)
            null
        }
    }

    fun getId(clazz: Class<out RabbitPacket>): String? = packetToId[clazz]
}