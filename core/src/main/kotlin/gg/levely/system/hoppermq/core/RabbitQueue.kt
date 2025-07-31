package gg.levely.system.hoppermq.core

interface RabbitQueue {

    fun getQueue(): String

    fun isDurable(): Boolean = false

    fun isAutoDelete(): Boolean = true

}

@JvmOverloads
fun queueBuilder(queue: String, property: DefaultRabbitQueue.() -> Unit = {}): RabbitQueue {
    return DefaultRabbitQueue(queue).apply(property)
}

data class DefaultRabbitQueue(
    private val queue: String,
    private var durable: Boolean = false,
    private var autoDelete: Boolean = true,
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