package gg.levely.system.hoppermq.core

import com.rabbitmq.client.BuiltinExchangeType
import com.sun.deploy.util.Property

interface RabbitQueue {

/*    companion object {

        @JvmStatic
        fun of(queue: String): RabbitQueue = DefaultRabbitQueue(queue)

        @JvmStatic
        fun of(exchange: String, queue: String, type: BuiltinExchangeType): RabbitQueue = DefaultRabbitEQueue(queue, exchange, type)

    }*/

    fun getQueue(): String

    fun isDurable(): Boolean = false

    fun isAutoDelete(): Boolean = true

}

fun queueBuilder(queue: String, property: DefaultRabbitQueue.() -> Unit = {}): RabbitQueue {
    return DefaultRabbitQueue(queue).apply(property)
}

data class DefaultRabbitQueue(
    private val queue: String,
    private var durable: Boolean = false,
    private var autoDelete: Boolean = true
) : RabbitQueue {

    override fun getQueue(): String = queue

    override fun isDurable(): Boolean = durable

    override fun isAutoDelete(): Boolean = autoDelete

    fun setDurable(durable: Boolean): DefaultRabbitQueue {
        this.durable = durable
        return this
    }

    fun setAutoDelete(autoDelete: Boolean): DefaultRabbitQueue {
        this.autoDelete = autoDelete
        return this
    }

}