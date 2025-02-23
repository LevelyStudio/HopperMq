package gg.levely.system.hoppermq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import gg.levely.system.eventbus.EventBus
import gg.levely.system.hoppermq.event.RabbitEvent
import gg.levely.system.hoppermq.packet.PacketRegistry
import gg.levely.system.hoppermq.packet.RabbitPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.DataOutputStream
import java.io.IOException

class HopperMq(url: String, private val author: String) : Closeable {

    private val logger: Logger = LoggerFactory.getLogger(HopperMq::class.java)

    private val basicProperties: AMQP.BasicProperties
    private val connection: Connection
    private val channel: Channel
    private val rabbitBus = EventBus<RabbitEvent>()
    private val packetRegistry = PacketRegistry()
    private val defaultRabbitConsumer = DefaultRabbitConsumer(this)
    private val queues: MutableMap<String, RabbitQueue> = mutableMapOf()

    init {
        try {
            val connectionFactory = ConnectionFactory().apply {
                setUri(url)
                requestedHeartbeat = 60
                isAutomaticRecoveryEnabled = true
                isTopologyRecoveryEnabled = true
                setErrorOnWriteListener { _, exception -> logger.error("Write error in RabbitMQ", exception) }
            }

            basicProperties = AMQP.BasicProperties.Builder()
                .headers(mapOf("author" to author))
                .build()

            connection = connectionFactory.newConnection(author)
            channel = connection.createChannel()

            logger.info("RabbitDriver successfully initialized for author: $author")
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize RabbitDriver", e)
        }
    }

    fun bindQueue(rabbitQueue: RabbitQueue) {
        try {
            queues[rabbitQueue.getQueue()] = rabbitQueue

            if (rabbitQueue is RabbitEQueue) {
                val queue = channel.queueDeclare().queue
                channel.exchangeDeclare(rabbitQueue.getExchange(), rabbitQueue.getType(), false, true, null)
                channel.queueBind(queue, rabbitQueue.getExchange(), rabbitQueue.getQueue())
                channel.basicConsume(queue, true, defaultRabbitConsumer)
            } else {
                channel.queueDeclare(rabbitQueue.getQueue(), false, false, true, null)
                channel.basicConsume(rabbitQueue.getQueue(), true, defaultRabbitConsumer)
            }
        } catch (e: IOException) {
            logger.error("Error binding queue ${rabbitQueue.getQueue()}", e)
        }
    }

    fun publish(queue: String, packet: RabbitPacket) {
        val rabbitQueue = queues.getOrPut(queue) { RabbitQueue.of(queue) }
        publish(rabbitQueue, packet)
    }

    fun publish(rabbitQueue: RabbitQueue, packet: RabbitPacket) {
        try {
            val id = packetRegistry.getId(packet::class.java) ?: return
            val byteArrayOutputStream = ByteArrayOutputStream()
            DataOutputStream(byteArrayOutputStream).use { dataOutput ->
                dataOutput.writeUTF(id)
                packet.write(dataOutput)
            }

            if (rabbitQueue is RabbitEQueue) {
                channel.basicPublish(
                    rabbitQueue.getExchange(),
                    rabbitQueue.getQueue(),
                    basicProperties,
                    byteArrayOutputStream.toByteArray()
                )
            } else {
                channel.basicPublish("", rabbitQueue.getQueue(), basicProperties, byteArrayOutputStream.toByteArray())
            }

        } catch (e: Exception) {
            logger.error("Failed to publish packet to ${rabbitQueue.getQueue()}", e)
        }
    }

    fun removeQueue(name: String) {
        queues.remove(name)
    }

    fun isConnected(): Boolean = connection.isOpen

    override fun close() {
        try {
            connection.close()
            logger.info("RabbitDriver closed successfully")
        } catch (e: IOException) {
            logger.error("Error while closing RabbitDriver", e)
        }
    }

    fun getAuthor(): String = author

    fun getConnection(): Connection = connection

    fun getChannel(): Channel = channel

    fun getRabbitBus(): EventBus<RabbitEvent> = rabbitBus

    fun getPacketRegistry(): PacketRegistry = packetRegistry
}