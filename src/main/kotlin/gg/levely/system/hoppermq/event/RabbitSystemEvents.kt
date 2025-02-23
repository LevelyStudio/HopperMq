package gg.levely.system.hoppermq.event

import com.rabbitmq.client.ShutdownSignalException

class RabbitConsumerReadyEvent(val consumerTag: String) : RabbitEvent

class RabbitShutdownSignalEvent(val consumerTag: String, val exception: ShutdownSignalException) : RabbitEvent
