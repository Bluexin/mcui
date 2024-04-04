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

import be.bluexin.luajksp.annotations.LuajExpose
import be.bluexin.mcui.Constants
import be.bluexin.mcui.api.themes.IHudDrawContext
import be.bluexin.mcui.themes.util.CBoolean
import be.bluexin.mcui.themes.util.CDouble
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import java.lang.ref.WeakReference
import javax.annotation.OverridingMethodsMustInvokeSuper

/**
 * Used to map a xml element with x, y, z coordinates and an "enabled" toggle.
 *
 * @author Bluexin
 */
@LuajExpose(includeType = LuajExpose.IncludeType.OPT_IN)
@Serializable
sealed class Element {

    companion object {
        const val DEFAULT_NAME = "anonymous"
    }

    /**
     * Friendly name for this element. Mostly used for debug purposes.
     */
    @LuajExpose
    var name: String = DEFAULT_NAME

    /**
     * X position.
     */
    @SerialName("x")
    @XmlSerialName("x")
    @LuajExpose
    var x: CDouble = CDouble.ZERO

    /**
     * Y position.
     */
    @SerialName("y")
    @XmlSerialName("y")
    @LuajExpose
    var y: CDouble = CDouble.ZERO

    /**
     * Z position.
     */
    @SerialName("z")
    @XmlSerialName("z")
    @LuajExpose
    var z: CDouble = CDouble.ZERO

    /**
     * Whether this element should be enabled.
     */
    @SerialName("enabled")
    @XmlSerialName("enabled")
    @LuajExpose
    var enabled: CBoolean = CBoolean.TRUE

    /**
     * Global scale for this element
     */
    @SerialName("scale")
    @XmlSerialName("scale")
    @LuajExpose
    var scale: CDouble? = null

    /**
     * Parent element for this element.
     */
    @Transient
    protected lateinit var parent: WeakReference<ElementParent>

    protected val parentOrZero get() = parent.get() ?: ElementParent.ZERO

    @LuajExpose
    val hasParent: Boolean get() = ::parent.isInitialized && parent.get().let { it != null && it !is Hud }

    /**
     * Draw this element on the screen.
     * This method should handle all the GL calls.
     *
     * @param ctx additional info about the draws
     */
    abstract fun draw(ctx: IHudDrawContext, poseStack: PoseStack)

    /**
     * Called during setup, used to initialize anything extra (after it has finished loading)
     * and returns whether this is an anonymous element.
     *
     * @param parent the parent to this element
     * @return whether this is an anonymous element
     */
    @OverridingMethodsMustInvokeSuper
    open fun setup(parent: ElementParent, fragments: Map<ResourceLocation, () -> Fragment>): Boolean {
        this.parent = WeakReference(parent)
        return if (name != DEFAULT_NAME) {
            Constants.LOG.debug("Set up {} in {}", this, parent.name)
            false
        } else true
    }

    fun hierarchyName(sb: StringBuilder = StringBuilder()): StringBuilder {
        parent.get()?.let {
            if (it is Element) {
                it.hierarchyName(sb)
                sb.append(" > ")
            }
        }
        if (name == DEFAULT_NAME) {
            sb.append("anonymous ")
                .append(this::class.simpleName)
        } else sb.append(name)
        return sb
    }

    fun nameOrParent(): String {
        if (name == DEFAULT_NAME) {
            parent.get()?.let {
                return it.name
            }
        }
        return name
    }

    override fun toString() = "$name (${javaClass.simpleName})"
}
