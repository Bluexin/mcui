/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.mcui.themes.elements

import be.bluexin.mcui.deprecated.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.loader.ThemeLoaderModule
import be.bluexin.mcui.themes.loader.XmlThemeLoader
import be.bluexin.mcui.themes.meta.ThemeDefinition
import be.bluexin.mcui.themes.meta.ThemeMetaModule
import be.bluexin.mcui.themes.meta.ThemeMetadata
import be.bluexin.mcui.themes.miniscript.MiniscriptModule
import be.bluexin.mcui.themes.miniscript.api.DrawContext
import be.bluexin.mcui.themes.miniscript.profile
import be.bluexin.mcui.themes.scripting.ScriptingModule
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlStreaming
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import org.koin.logger.slf4jLogger
import java.io.FileReader
import kotlin.jvm.Transient as JvmTransient

fun main() {
    val ka = startKoin {
        slf4jLogger()
        modules(
            ThemeMetaModule().module,
            ScriptingModule().module,
            ThemeLoaderModule().module,
            MiniscriptModule().module,
        )
    }

    val xml = ka.koin.get<XmlThemeLoader>().xml

    val hud = /*Hud(
        name = "nice_hud", version = "123", parts = mapOf(
            HudPartType.ENTITY_HEALTH_HUD to ElementGroup(),
            HudPartType.AIR to ElementGroup(),
        )
    )*/
        xml.decodeFromReader<Hud>(XmlStreaming.newReader(FileReader("common/src/main/resources/assets/mcui/themes/hex2/hud.xml")))

    println(xml.encodeToString(hud))

    println(
        Json {
            prettyPrint = true
        }.encodeToString(hud)
    )

    ka.close()
}

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@SerialName("bl:hud")
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/be/bluexin/mcui/hud-schema")
class Hud(
    override val name: String = "HUD",
    @XmlElement
    val version: String = ThemeMetadata.UNKNOWN_VERSION,
    @XmlSerialName("parts")
    private val parts: Parts,
) : ElementParent, KoinComponent {

    override val rootElement: ElementParent
        get() = this

    @Transient
    @JvmTransient
    private val indexedParts = parts.parts.associate { (k, v) -> k to v }

    private val drawContext by inject<DrawContext>()

    operator fun get(key: HudPartType) = indexedParts[key]

    fun setup(fragments: Map<ResourceLocation, () -> Fragment>, themeDefinition: ThemeDefinition) =
        this.indexedParts.values.forEach { it.setup(this, fragments, themeDefinition) }

    override val elements: Iterable<Element> = emptyList() // this could be populated if we open up HUD to scripting

    fun draw(key: HudPartType, ctx: IHudDrawContext, poseStack: PoseStack) {
        this[key]?.draw(ctx, poseStack, 0.0, 0.0)
    }

    fun drawAll(ctx: IHudDrawContext, poseStack: PoseStack) {
        ctx.profile(javaClass.simpleName) {
            parts.parts.forEach { (key, part) ->
                // Compatibility for old themes
                if (key != HudPartType.JUMP_BAR || drawContext.player().hasMount()) ctx.profile(key.name) {
                    part.draw(ctx, poseStack, 0.0, 0.0)
                }
            }
        }
    }

    @Serializable
    class Parts(
        @XmlSerialName("entry")
        val parts: List<Entry<HudPartType, ElementGroup>>
    ) {
        companion object {
            operator fun invoke(parts: List<Pair<HudPartType, ElementGroup>>) = Parts(
                parts.map { (k, v) -> Entry(k, v) }
            )
        }

        @Serializable
        data class Entry<K, V>(
            @XmlSerialName("key")
            val key: K,
            @XmlSerialName("value")
            val value: V
        )

    }
}
