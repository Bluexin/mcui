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

package be.bluexin.mcui

import be.bluexin.mcui.themes.ThemeManager
import be.bluexin.mcui.util.Client
import be.bluexin.mcui.util.ColorUtil
import be.bluexin.mcui.util.append
import be.bluexin.mcui.util.math.Vec2d
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import org.joml.Vector2i
import org.joml.Vector3dc
import org.lwjgl.opengl.GL11
import kotlin.math.max
import kotlin.math.min

object GLCore {

    private val mc = Client.mc
    val glFont: Font get() = mc.font
    private val glTextureManager get() = mc.textureManager
    val tessellator: Tesselator
        get() = Tesselator.getInstance()
    val bufferBuilder: BufferBuilder
        get() = tessellator.builder

    @JvmOverloads
    fun color(red: Float, green: Float, blue: Float, alpha: Float = 1f) {
        RenderSystem.clearColor(red, green, blue, alpha)
    }

    fun color(color: ColorUtil) {
        color(color.rgba)
    }

    fun color(rgba: Int) {
        val red = (rgba shr 24 and 0xFF).toFloat() / 0xFF
        val green = (rgba shr 16 and 0xFF).toFloat() / 0xFF
        val blue = (rgba shr 8 and 0xFF).toFloat() / 0xFF
        val alpha = (rgba and 0xFF).toFloat() / 0xFF

        color(red, green, blue, alpha)
    }

    fun color(rgba: Int, lightLevel: Float) {
        val red = (rgba shr 24 and 0xFF).toFloat() / 0xFF
        val green = (rgba shr 16 and 0xFF).toFloat() / 0xFF
        val blue = (rgba shr 8 and 0xFF).toFloat() / 0xFF
        val alpha = (rgba and 0xFF).toFloat() / 0xFF
        val light = max(lightLevel, 0.15f)

        color(red * light, green * light, blue * light, alpha)
    }

    private fun glFontColor(rgba: Int): Int {
        val alpha = rgba and 0xFF
        val red = rgba shr 24 and 0xFF
        val blue = rgba shr 8 and 0xFF
        val green = rgba shr 16 and 0xFF

        return alpha shl 24 or (red shl 16) or (blue shl 8) or green
    }

    @JvmOverloads
    fun glString(
        font: Font?,
        string: String,
        x: Int,
        y: Int,
        argb: Int,
        shadow: Boolean = false,
        centered: Boolean = false,
        poseStack: PoseStack
    ) {
        if (shadow) font?.drawShadow(
            poseStack,
            string,
            x.toFloat(),
            y.toFloat() - if (centered) font.lineHeight / 2f else 0f,
            glFontColor(argb)
        ) else font?.draw(
            poseStack,
            string,
            x.toFloat(),
            y.toFloat() - if (centered) font.lineHeight / 2f else 0f,
            glFontColor(argb)
        )
    }

    @JvmOverloads
    fun glString(
        string: String,
        x: Int,
        y: Int,
        argb: Int,
        shadow: Boolean = false,
        centered: Boolean = false,
        poseStack: PoseStack
    ) {
        glString(glFont, string, x, y, argb, shadow, centered, poseStack)
    }

    @JvmOverloads
    fun glString(string: String, pos: Vec2d, argb: Int, shadow: Boolean = false, centered: Boolean = false,
                 poseStack: PoseStack
    ) {
        glString(string, pos.xi, pos.yi, argb, shadow, centered, poseStack)
    }

    fun setFont(mc: Minecraft, custom: Boolean) {

        val fontLocation: ResourceLocation =
            if (custom) {
                takeTextureIfExists(
                    ThemeManager.currentTheme.texturesRoot.append("ascii.png")
                ) ?: takeTextureIfExists(
                    ResourceLocation(
                        ThemeManager.currentTheme.themeRoot.namespace,
                        "textures/ascii.png"
                    )
                ) ?: ResourceLocation(Constants.MOD_ID, "textures/ascii.png")
            } else {
                ResourceLocation("textures/font/ascii.png")
            }
        /*mc.fontRenderer = FontRenderer(mc.gameSettings, fontLocation, mc.textureManager, false)
        if (mc.gameSettings.language != null) {
            mc.fontRenderer.unicodeFlag = mc.isUnicode
            mc.fontRenderer.bidiFlag = mc.languageManager.isCurrentLanguageBidirectional
        }
        (mc.resourceManager as IReloadableResourceManager).registerReloadListener(mc.fontRenderer)*/
    }

    @JvmOverloads
    fun glStringWidth(string: String, font: Font? = glFont): Int {
        return font?.width(string) ?: 0
    }

    @JvmOverloads
    fun glStringHeight(font: Font? = glFont): Int {
        return font?.lineHeight ?: 0
    }

    fun glBindTexture(location: ResourceLocation) {
        RenderSystem.setShaderTexture(0, location)
    }

    /**
     * Checks to make sure the texture is valid, returning false means the texture is invalid.
     */
    fun checkTexture(location: ResourceLocation): Boolean {
        return try {
            Client.mc.resourceManager.getResource(location)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun takeTextureIfExists(location: ResourceLocation): ResourceLocation? =
        if (checkTexture(location)) location else null

    @JvmOverloads
    fun glTexturedRectV2(
        pos: Vector3dc,
        size: Vec2d,
        srcPos: Vec2d = Vec2d.ZERO,
        srcSize: Vec2d = size,
        textureSize: Vector2i = Vector2i(256)
    ) {
        glTexturedRectV2(
            x = pos.x(), y = pos.y(), z = pos.z(),
            width = size.x, height = size.y,
            srcX = srcPos.x, srcY = srcPos.y,
            srcWidth = srcSize.x, srcHeight = srcSize.y,
            textureW = textureSize.x(), textureH = textureSize.y()
        )
    }

    @JvmOverloads
    fun glTexturedRectV2(
        x: Double,
        y: Double,
        z: Double = 0.0,
        width: Double,
        height: Double,
        srcX: Double = 0.0,
        srcY: Double = 0.0,
        srcWidth: Double = width,
        srcHeight: Double = height,
        textureW: Int = 256,
        textureH: Int = 256
    ) {
        val f = 1f / textureW
        val f1 = 1f / textureH
        /*begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        bufferBuilder.pos(x, y + height, z)
            .tex((srcX.toFloat() * f).toDouble(), ((srcY + srcHeight).toFloat() * f1).toDouble()).endVertex()
        bufferBuilder.pos(x + width, y + height, z)
            .tex(((srcX + srcWidth).toFloat() * f).toDouble(), ((srcY + srcHeight).toFloat() * f1).toDouble())
            .endVertex()
        bufferBuilder.pos(x + width, y, z)
            .tex(((srcX + srcWidth).toFloat() * f).toDouble(), (srcY.toFloat() * f1).toDouble()).endVertex()
        bufferBuilder.pos(x, y, z).tex((srcX.toFloat() * f).toDouble(), (srcY.toFloat() * f1).toDouble()).endVertex()
        draw()*/
        // TODO: replace with blit
    }

    fun glAlphaTest(flag: Boolean) {
        if (flag) {
//            RenderSystem.enableAlpha()
        } else {
//            RenderSystem.disableAlpha()
        }
    }

    fun alphaFunc(src: Int, dst: Int) {
//        RenderSystem.alphaFunc(src, dst.toFloat())
    }

    fun glBlend(flag: Boolean) {
        if (flag) {
            RenderSystem.enableBlend()
        } else {
            RenderSystem.disableBlend()
        }
    }

    fun blendFunc(src: Int, dst: Int) {
        RenderSystem.blendFunc(src, dst)
    }

    fun tryBlendFuncSeparate(a: Int, b: Int, c: Int, d: Int) {
        RenderSystem.blendFuncSeparate(a, b, c, d)
    }

    fun depthMask(flag: Boolean) {
        RenderSystem.depthMask(flag)
    }

    fun depth(flag: Boolean) {
        if (flag) {
            RenderSystem.enableDepthTest()
        } else {
            RenderSystem.disableDepthTest()
        }
    }

    fun glDepthFunc(flag: Int) {
        GL11.glDepthFunc(flag)
    }

    fun glRescaleNormal(flag: Boolean) {
        if (flag) {
//            RenderSystem.enableRescaleNormal()
        } else {
//            RenderSystem.disableRescaleNormal()
        }
    }

    fun glTexture2D(flag: Boolean) {
        if (flag) {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
        } else {
            GL11.glDisable(GL11.GL_TEXTURE_2D)
        }
    }

    fun glCullFace(flag: Boolean) {
        if (flag) {
            RenderSystem.enableCull()
        } else {
            RenderSystem.disableCull()
        }
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.translate(x, y, z)"), DeprecationLevel.ERROR)
    fun glTranslatef(x: Float, y: Float, z: Float) {
//        RenderSystem.translate(x, y, z)
    }

    fun glNormal3f(x: Float, y: Float, z: Float) {
        GL11.glNormal3f(x, y, z)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.rotateAround(angle, x, y, z)"), DeprecationLevel.ERROR)
    fun glRotatef(angle: Float, x: Float, y: Float, z: Float) {
//        RenderSystem.rotate(angle, x, y, z)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.scale(x, y, z)"), DeprecationLevel.ERROR)
    fun glScalef(x: Float, y: Float, z: Float) {
//        RenderSystem.scale(x, y, z)
    }

    fun lighting(flag: Boolean) {
        if (flag) {
//            RenderSystem.enableLighting()
        } else {
//            RenderSystem.disableLighting()
        }
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.pushPose()"), DeprecationLevel.ERROR)
    fun pushMatrix() {
//        RenderSystem.pushMatrix()
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.popPose()"), DeprecationLevel.ERROR)
    fun popMatrix() {
//        RenderSystem.popMatrix()
    }

    /**
     * returns an AABB with corners x1, y1, z1 and x2, y2, z2
     */
    fun fromBounds(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): AABB {
        val d0 = min(x1, x2)
        val d1 = min(y1, y2)
        val d2 = min(z1, z2)
        val d3 = max(x1, x2)
        val d4 = max(y1, y2)
        val d5 = max(z1, z2)
        return AABB(d0, d1, d2, d3, d4, d5)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.translate(x, y, z)"), DeprecationLevel.ERROR)
    fun translate(x: Double, y: Double, z: Double) {
//        RenderSystem.translate(x, y, z)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.translate(x, y, z)"), DeprecationLevel.ERROR)
    fun translate(x: Float, y: Float, z: Float) {
//        RenderSystem.translate(x, y, z)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.scale(x, y, z)"), DeprecationLevel.ERROR)
    fun scale(x: Double, y: Double, z: Double) {
//        RenderSystem.scale(x, y, z)
    }

    @Deprecated("Use PoseStack", ReplaceWith("poseStack.scale(x, y, z)"), DeprecationLevel.ERROR)
    fun scale(x: Float, y: Float, z: Float) {
//        RenderSystem.scale(x, y, z)
    }
}
