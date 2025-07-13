package gg.levely.system.hoppermq.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class RabbitPacketProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("gg.levely.system.hoppermq.core.packet.RabbitPacketLabel", true)
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val file = codeGenerator.createNewFile(
            Dependencies(false),
            packageName = "gg.levely.system.hoppermq.generated",
            fileName = "GeneratedPacketRegistry"
        )

        file.bufferedWriter().use { writer ->
            writer.write("package gg.levely.system.hoppermq.generated\n\n")
            writer.write("import gg.levely.system.hoppermq.core.packet.PacketRegistry\n")

            symbols.forEach {
                val import = it.qualifiedName?.asString()
                if (import != null) writer.write("import $import\n")
            }

            writer.write("\n")

            writer.write("object GeneratedPacketRegistry {\n")
            writer.write("\n")
            writer.write("    fun registerAll(registry: PacketRegistry) {\n")

            symbols.forEach {
                val className = it.simpleName.asString()
                logger.warn("Registering RabbitPacket: $className")
                writer.write("        registry.register($className::class.java)\n")
            }

            writer.write("    }\n")

            writer.write("\n")

            writer.write("}\n")
        }

        return emptyList()
    }
}