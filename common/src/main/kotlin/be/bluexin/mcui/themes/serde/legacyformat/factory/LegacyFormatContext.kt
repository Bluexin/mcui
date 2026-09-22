package be.bluexin.mcui.themes.serde.legacyformat.factory

import be.bluexin.mcui.themes.elements.Fragment
import be.bluexin.mcui.themes.loader.AbstractThemeLoader
import be.bluexin.mcui.themes.serde.Factory
import be.bluexin.mcui.themes.serde.legacyformat.xml.FragmentXml
import net.minecraft.resources.ResourceLocation

/**
 * [Factory.Context] implementation used when building the modern element tree from legacy XML.
 *
 * `nested`/`pop` maintain a path stack for error messages, [error] funnels into the global
 * [AbstractThemeLoader.Reporter] (surfaced by `/saoui debug print_errors`) and [loadFragment]
 * lazily resolves fragments (by their theme fragment id) through [fragmentFactory], caching results.
 */
internal class LegacyFormatContext(
    private val fragmentFactory: FragmentFactory,
    private val fragmentLoader: (ResourceLocation) -> FragmentXml?,
) : Factory.Context {

    private val path = ArrayDeque<String>()
    private val fragments = mutableMapOf<ResourceLocation, FragmentXml?>()

    override fun nested(path: String) {
        this.path.addLast(path)
    }

    override fun pop() {
        check(path.isNotEmpty()) { "Factory.Context stack underflow" }
        path.removeLast()
    }

    override fun error(message: String) {
        AbstractThemeLoader.Reporter += "${path.joinToString(" > ")} : $message"
    }

    override fun loadFragment(id: ResourceLocation): Fragment? = fragments.getOrPut(id) {
        fragmentLoader(id)
    }?.let { fragmentFactory.create(it, this).getOrNull() }
}