package gg.levely.system.hoppermq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import gg.levely.system.hoppermq.event.RabbitConsumerReadyEvent
import gg.levely.system.hoppermq.event.RabbitShutdownSignalEvent
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class DefaultRabbitConsumer(private val hopperMq: HopperMq) : Consumer {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DefaultRabbitConsumer::class.java)
    }

    private val rabbitBus = hopperMq.rabbitBus
    private val packetRegistry = hopperMq.packetRegistry

    override fun handleConsumeOk(consumerTag: String) {
        rabbitBus.publish(RabbitConsumerReadyEvent(consumerTag))
    }

    override fun handleCancelOk(consumerTag: String) = Unit

    override fun handleCancel(consumerTag: String) = Unit

    override fun handleShutdownSignal(consumerTag: String, sig: ShutdownSignalException) {
        rabbitBus.publish(RabbitShutdownSignalEvent(consumerTag, sig))
        hopperMq.removeQueue(consumerTag)
    }

    override fun handleRecoverOk(consumerTag: String) = Unit


    override fun handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties?,
        body: ByteArray,
    ) {
        val author = properties?.headers?.get("author")?.toString() ?: return

        if (author == hopperMq.author) return

        ByteArrayInputStream(body).use { byteArrayInputStream ->
            DataInputStream(byteArrayInputStream).use { dataInputStream ->
                val id = dataInputStream.readUTF()
                val packet = packetRegistry.getPacket(id)

                if (packet == null) {
                    LOGGER.warn("Packet with id $id was not found")
                    return
                }

                try {
                    packet.read(dataInputStream)
                    packet.metadata.also { it["author"] = author }
                } catch (e: Exception) {
                    LOGGER.error("Error while reading packet $id", e)
                    return
                }

                rabbitBus.publish(packet)
            }
        }
    }
}
