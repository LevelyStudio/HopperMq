package gg.levely.system.hoppermq

import com.rabbitmq.client.BuiltinExchangeType

interface RabbitEQueue : RabbitQueue {

    fun getExchange(): String

    fun getType(): BuiltinExchangeType

}

class DefaultRabbitEQueue(
    private val queue: String,
    private val exchange: String,
    private val type: BuiltinExchangeType,
) : RabbitEQueue {

    override fun getQueue(): String {
        return queue
    }

    override fun getExchange(): String {
        return exchange
    }

    override fun getType(): BuiltinExchangeType {
        return type
    }
}