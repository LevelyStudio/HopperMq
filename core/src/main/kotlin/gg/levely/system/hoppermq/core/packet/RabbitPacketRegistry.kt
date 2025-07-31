package gg.levely.system.hoppermq.core.packet

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaConstructor

class RabbitPacketRegistry {

    private val logger: Logger = LoggerFactory.getLogger(RabbitPacketRegistry::class.java)

    private val idToPacket: MutableMap<String, KClass<out RabbitPacket>> = mutableMapOf()
    private val packetToId: MutableMap<KClass<out RabbitPacket>, String> = mutableMapOf()
    private val constructorPacket: MutableMap<KClass<out RabbitPacket>, KFunction<RabbitPacket>> = mutableMapOf()

    fun register(packet: KClass<out RabbitPacket>) {
        val rabbitPacketLabel = packet.findAnnotation<RabbitPacketLabel>()
        if (rabbitPacketLabel == null) {
            logger.error("${packet.simpleName} does not have RabbitPacketLabel annotation")
            return
        }

        val id = rabbitPacketLabel.value.takeIf { it.isNotEmpty() } ?: packet.simpleName

        if (id == null) {
            logger.error("Packet ${packet.simpleName} does not have a valid id")
            return
        }

        if (id in idToPacket) {
            logger.error("$id has already been used by another packet (${idToPacket[id]})")
            return
        }

        try {
            val noArgsConstructor = packet.constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
                ?: throw IllegalArgumentException("Class should have a single no-arg constructor: ${packet.simpleName}")

            noArgsConstructor.javaConstructor?.isAccessible = true

            constructorPacket[packet] = noArgsConstructor
            idToPacket[id] = packet
            packetToId[packet] = id

            logger.debug("Registered packet ${packet.simpleName} with id $id")
        } catch (e: NoSuchMethodException) {
            logger.error("Cannot find empty constructor in ${packet.simpleName}", e)
        }
    }

    fun getPacket(id: String): RabbitPacket? {
        val packetClass = idToPacket[id] ?: return null
        val constructor = constructorPacket[packetClass] ?: return null

        return runCatching {
            constructor.call()
        }.getOrElse {
            logger.error("Cannot create instance for ${packetClass.simpleName}", it)
            null
        }
    }

    fun getId(clazz: KClass<out RabbitPacket>): String? = packetToId[clazz]

    fun getAllPackets(): Map<String, KClass<out RabbitPacket>> = idToPacket.toMap()
}