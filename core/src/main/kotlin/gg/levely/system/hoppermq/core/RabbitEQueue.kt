package gg.levely.system.hoppermq.core

import com.rabbitmq.client.BuiltinExchangeType

interface RabbitEQueue : RabbitQueue {

    fun getExchange(): String

    fun getType(): BuiltinExchangeType

}

fun queueBuilder(
    queue: String,
    exchange: String,
    type: BuiltinExchangeType,
    property: DefaultRabbitEQueue.() -> Unit = {}
): RabbitEQueue {
    return DefaultRabbitEQueue(queue, exchange, type).apply(property)
}

data class DefaultRabbitEQueue(
    private val queue: String,
    private val exchange: String,
    private val type: BuiltinExchangeType,
    private var durable: Boolean = false,
    private var autoDelete: Boolean = true
) : RabbitEQueue {

    override fun getQueue(): String = queue

    override fun getExchange(): String = exchange

    override fun getType(): BuiltinExchangeType = type

    override fun isDurable(): Boolean = durable

    override fun isAutoDelete(): Boolean = autoDelete

    fun setDurable(durable: Boolean): DefaultRabbitEQueue {
        this.durable = durable
        return this
    }

    fun setAutoDelete(autoDelete: Boolean): DefaultRabbitEQueue {
        this.autoDelete = autoDelete
        return this
    }

}