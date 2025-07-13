package gg.levely.system.hoppermq.core.packet

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RabbitPacketLabel(val value: String = "")

