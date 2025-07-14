package gg.levely.system.hoppermq.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class RabbitPacketProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        environment.logger.warn("RabbitPacketProcessorProvider is being created!")

        return RabbitPacketProcessor(environment.codeGenerator, environment.options, environment.logger)
    }

}