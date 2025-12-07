package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Element
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.legacyformat.dto.ElementXml
import org.koin.core.annotation.Single

@Single
internal class LegacyFactoryRegistry(
    factories: List<LegacyFactory<out ElementXml, out Element>>
) {
    private val factoryMap = factories.associateBy { it.xmlType }

    fun createFromXml(xml: ElementXml, context: Factory.Context): Result<Element> {
        val factory = factoryMap[xml::class] ?: return Result.failure(
            IllegalArgumentException("No factory registered for ${xml::class.simpleName}")
        )

        @Suppress("UNCHECKED_CAST")
        return (factory as LegacyFactory<ElementXml, Element>).create(xml, context)
    }
}
