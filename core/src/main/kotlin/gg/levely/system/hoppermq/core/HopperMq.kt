package gg.levely.system.hoppermq.core

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import gg.levely.system.eventbus.EventBus
import gg.levely.system.hoppermq.core.event.RabbitEvent
import gg.levely.system.hoppermq.core.packet.RabbitPacket
import gg.levely.system.hoppermq.core.packet.RabbitPacketRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.DataOutputStream
import java.io.IOException

class HopperMq(url: String, val author: String) : Closeable {

    private val logger: Logger = LoggerFactory.getLogger(HopperMq::class.java)

    val connection: Connection
    val channel: Channel
    val rabbitBus = EventBus<RabbitEvent>()
    val rabbitPacketRegistry = RabbitPacketRegistry()

    val isConnected: Boolean
        get() = connection.isOpen

    private val defaultRabbitConsumer = DefaultRabbitConsumer(this)
    private val queues = mutableMapOf<String, RabbitQueue>()

    constructor(url: String, selfQueue: RabbitQueue) : this(url, selfQueue.getQueue()) {
        bindQueue(selfQueue)
    }

    init {
        try {
            val connectionFactory = ConnectionFactory().apply {
                setUri(url)
                requestedHeartbeat = 60
                isAutomaticRecoveryEnabled = true
                isTopologyRecoveryEnabled = true
                setErrorOnWriteListener { _, exception -> logger.error("Write error in RabbitMQ", exception) }
            }

            connection = connectionFactory.newConnection(author)
            channel = connection.createChannel()

            logger.info("HopperMq successfully initialized for author: $author")
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize HopperMq", e)
        }
    }

    fun bindQueue(rabbitQueue: RabbitQueue) {
        try {
            queues[rabbitQueue.getQueue()] = rabbitQueue

            if (rabbitQueue is RabbitEQueue) {
                val queue = channel.queueDeclare().queue
                channel.exchangeDeclare(
                    rabbitQueue.getExchange(),
                    rabbitQueue.getType(),
                    rabbitQueue.isDurable(),
                    rabbitQueue.isAutoDelete(),
                    null
                )
                channel.queueBind(queue, rabbitQueue.getExchange(), rabbitQueue.getQueue())
                channel.basicConsume(queue, true, defaultRabbitConsumer)
            } else {
                channel.queueDeclare(
                    rabbitQueue.getQueue(),
                    rabbitQueue.isDurable(),
                    false,
                    rabbitQueue.isAutoDelete(),
                    null
                )
                channel.basicConsume(rabbitQueue.getQueue(), true, defaultRabbitConsumer)
            }
        } catch (e: IOException) {
            logger.error("Error binding queue ${rabbitQueue.getQueue()}", e)
        }
    }

    fun publish(queue: String, packet: RabbitPacket) {
        val rabbitQueue = queues.getOrPut(queue) { queueBuilder(queue) }
        publish(rabbitQueue, packet)
    }

    fun publish(rabbitQueue: RabbitQueue, packet: RabbitPacket, sendToSelf: Boolean = false) {
        try {
            val id = rabbitPacketRegistry.getId(packet::class.java) ?: return
            val byteArrayOutputStream = ByteArrayOutputStream()
            DataOutputStream(byteArrayOutputStream).use { dataOutput ->
                dataOutput.writeUTF(id)
                packet.write(dataOutput)
            }

            val properties = mutableMapOf<String, Any>()
            properties["author"] = author

            packet.metadata.forEach { (key, value) -> properties[key] = value }

            val amqpProperties = AMQP.BasicProperties.Builder()
                .headers(properties)
                .build()

            if (rabbitQueue is RabbitEQueue) {
                channel.basicPublish(
                    rabbitQueue.getExchange(),
                    rabbitQueue.getQueue(),
                    amqpProperties,
                    byteArrayOutputStream.toByteArray()
                )
            } else {
                channel.basicPublish("", rabbitQueue.getQueue(), amqpProperties, byteArrayOutputStream.toByteArray())
            }

            if (sendToSelf) {
                rabbitBus.publish(packet)
            }

        } catch (e: Exception) {
            logger.error("Failed to publish packet to ${rabbitQueue.getQueue()}", e)
        }
    }

    fun deleteQueue(queue: String) {
        try {
            channel.queueDelete(queue)
            queues.remove(queue)
        } catch (e: IOException) {
            logger.error("Error deleting queue $queue", e)
        }
    }

    fun removeQueue(queue: String) {
        queues.remove(queue)
    }

    fun removeQueue(rabbitQueue: RabbitQueue) {
        removeQueue(rabbitQueue.getQueue())
    }

    override fun close() {
        try {
            connection.close()
            logger.info("HopperMq closed successfully")
        } catch (e: IOException) {
            logger.error("Error while closing RabbitDriver", e)
        }
    }

}