package be.bluexin.mcui.themes

import be.bluexin.mcui.themes.elements.ElementModule
import be.bluexin.mcui.themes.loader.ThemeLoaderModule
import be.bluexin.mcui.themes.loader.ThemeLoaderRegistry
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import be.bluexin.mcui.themes.meta.ThemeMetaModule
import be.bluexin.mcui.themes.miniscript.MiniscriptModule
import be.bluexin.mcui.themes.scripting.ScriptingModule
import be.bluexin.mcui.themes.serde.legacyformat.LegacyFormatModule
import be.bluexin.mcui.themes.serde.legacyformat.factory.LegacyFactoryRegistry
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.ksp.generated.module
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Starts the real Koin graph (same modules as MCUICore, from the KSP-generated definitions) and
 * resolves the theme-loader factory clique on a plain JVM.
 */
class KoinDiGraphTest {

    @Test
    fun `theme loader and legacy factory clique resolves`() {
        try {
            startKoin {
                modules(
                    ThemeMetaModule().module,
                    ScriptingModule().module,
                    ThemeLoaderModule().module,
                    LegacyFormatModule().module,
                    MiniscriptModule().module,
                    ElementModule().module,
                )
            }.koin.also { koin ->
                assertNotNull(koin.get<ThemeLoaderRegistry>())
                assertNotNull(koin.get<XmlThemeLoader>())
                assertNotNull(koin.get<LegacyFactoryRegistry>())
            }
        } finally {
            stopKoin()
        }
    }
}