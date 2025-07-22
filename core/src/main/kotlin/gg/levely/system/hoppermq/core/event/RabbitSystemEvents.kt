package gg.levely.system.hoppermq.core.event

import com.rabbitmq.client.ShutdownSignalException
import gg.levely.system.hoppermq.core.RabbitQueue

class RabbitConsumerReadyEvent(val consumerTag: RabbitQueue) : RabbitEvent

class RabbitShutdownSignalEvent(val consumerTag: String, val exception: ShutdownSignalException) : RabbitEvent
