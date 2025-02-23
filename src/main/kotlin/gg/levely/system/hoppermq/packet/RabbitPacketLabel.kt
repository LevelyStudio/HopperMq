package gg.levely.system.hoppermq.packet

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RabbitPacketLabel(val value: String = "")

