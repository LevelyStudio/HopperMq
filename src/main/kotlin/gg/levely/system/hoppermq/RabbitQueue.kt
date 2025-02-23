package gg.levely.system.hoppermq

import com.rabbitmq.client.BuiltinExchangeType

interface RabbitQueue {

    companion object {

        @JvmStatic
        fun of(queue: String): RabbitQueue = DefaultRabbitQueue(queue)

        @JvmStatic
        fun of(exchange: String, queue: String, type: BuiltinExchangeType): RabbitQueue = DefaultRabbitEQueue(queue, exchange, type)

    }

    fun getQueue(): String

}

class DefaultRabbitQueue(private val queue: String) : RabbitQueue {

    override fun getQueue(): String = queue

}