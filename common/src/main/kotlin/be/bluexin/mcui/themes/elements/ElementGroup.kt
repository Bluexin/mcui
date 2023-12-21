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

import be.bluexin.mcui.Constants
import be.bluexin.mcui.GLCore
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.platform.Services
import be.bluexin.mcui.themes.util.profile
import be.bluexin.mcui.util.Client
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.serialization.XmlElement

/**
 * Part of saoui by Bluexin.
 *
 * @author Bluexin
 */
@Serializable
sealed class ElementGroupParent : Element(), ElementParent {

    protected var children: Children = Children(emptyList())

    var elements: List<Element>
        get() = children
        internal set(value) {
            children = Children(value)
        }

    @Transient
    protected var rl: ResourceLocation? = null

    @XmlElement
    private var texture: String? = null

    // FIXME : an error in enabled will still crash
    override fun draw(ctx: IHudDrawContext, poseStack: PoseStack) {
        if (!enabled(ctx)) return

        prepareDraw(ctx, poseStack)
        drawChildren(ctx, poseStack)
        finishDraw(ctx, poseStack)
    }

    protected open fun prepareDraw(ctx: IHudDrawContext, poseStack: PoseStack) {
        GLCore.glBlend(true)
        GLCore.color(1f, 1f, 1f, 1f)

        if (this.rl != null) GLCore.glBindTexture(this.rl!!)

        poseStack.pushPose()
        poseStack.translate(x(ctx), y(ctx), z(ctx))
    }

    protected open fun finishDraw(ctx: IHudDrawContext, poseStack: PoseStack) {
        poseStack.popPose()
    }

    protected open fun drawChildren(ctx: IHudDrawContext, poseStack: PoseStack) {
        if (Services.PLATFORM.isDevelopmentEnvironment) {
            this.children = this.children.filter {
                ctx.profile(it.name) {
                    try {
                        it.draw(ctx, poseStack)
                        true
                    } catch (e: Throwable) {
                        Client.showError("Error rendering child ${it.hierarchyName()}, removing from group", e)
                        false
                    }
                }
            }.let(::Children)
        } else {
            this.children.forEach {
                ctx.profile(it.name) {
                    it.draw(ctx, poseStack)
                }
            }
        }
    }

    override fun setup(parent: ElementParent, fragments: Map<ResourceLocation, () -> Fragment>): Boolean {
        val res = super.setup(parent, fragments)
        this.rl = this.texture?.let(::ResourceLocation)
        var anonymous = 0
        this.children.forEach { if (it.name == DEFAULT_NAME) ++anonymous; it.setup(this, fragments) }
        if (anonymous > 0) Constants.LOG.info("Set up $anonymous anonymous elements in $name.")
        return res
    }

    fun add(element: Element) {
        this.elements += element
    }

    @Serializable
    @SerialName("children")
    protected class Children(
        private val elements: List<Element> = emptyList()
    ) : List<Element> by elements
}

@Serializable
@SerialName("elementGroup")
class ElementGroup : ElementGroupParent()
