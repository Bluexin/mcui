/*
 * Copyright (C) 2016-2019 Arnaud 'Bluexin' Solé
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

import be.bluexin.mcui.api.themes.IHudDrawContext
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.profiling.ProfilerFiller
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlNamespaceDeclSpec
import nl.adaptivity.xmlutil.serialization.XmlSerialName

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
@OptIn(ExperimentalXmlUtilApi::class)
@Serializable
@XmlSerialName(
    value = "bl:hud"
)
@XmlNamespaceDeclSpec("bl=https://www.bluexin.be/com/saomc/saoui/hud-schema")
class Hud(
    @XmlSerialName("parts")
    private val parts: Parts,
    override val name: String = "MenuDefs",
    @XmlElement
    val version: String = "1.0"
) : ElementParent {

    @Transient
    private val indexedParts = parts.parts.associate { (k, v) -> k to v }

    override fun getX(ctx: IHudDrawContext) = 0.0

    override fun getY(ctx: IHudDrawContext) = 0.0

    override fun getZ(ctx: IHudDrawContext) = 0.0

    operator fun get(key: HudPartType) = indexedParts[key]

    fun setup(fragments: Map<ResourceLocation, () -> Fragment>) =
        this.indexedParts.values.forEach { it.setup(this, fragments) }

    fun draw(key: HudPartType, ctx: IHudDrawContext, poseStack: PoseStack) {
        this[key]?.draw(ctx, poseStack)
    }

    fun drawAll(ctx: IHudDrawContext, poseStack: PoseStack, profiler: ProfilerFiller) {
        profiler.push(javaClass.simpleName)
        parts.parts.forEach { (key, part) ->
            profiler.push(key.name)
            part.draw(ctx, poseStack)
            profiler.pop()
        }
        profiler.pop()
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
