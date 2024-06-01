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

package be.bluexin.mcui.deprecated.screens

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.config.OptionCore
import be.bluexin.mcui.themes.elements.HudPartType
import be.bluexin.mcui.themes.meta.ThemeManager
import be.bluexin.mcui.themes.miniscript.HudDrawContext
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySelector
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.entity.EntityTypeTest
import net.minecraft.world.phys.AABB
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lwjgl.opengl.GL11

class IngameGUI(private val mc: Minecraft) : Gui(mc, mc.itemRenderer), KoinComponent {

    private val themeManager by inject<ThemeManager>()

    //    private var eventParent: RenderGameOverlayEvent? = null
    private var offsetUsername: Int = 0
//    private val debugOverlay: GuiOverlayDebugForge = GuiOverlayDebugForge(mc)

    private lateinit var context: HudDrawContext

    override fun render(poseStack: PoseStack, partialTicks: Float) {
        mc.profiler.push("setup")
        if (!::context.isInitialized) {
            this.context = HudDrawContext()
        }
        val username = mc.player?.displayName
        val maxNameWidth = if (username == null) 0 else font.width(username)
        val usernameBoxes = 1 + (maxNameWidth + 4) / 5
        offsetUsername = 18 + usernameBoxes * 5
//        val res = ScaledResolution(mc)
//        eventParent = RenderGameOverlayEvent(partialTicks, res)
//        val width = res.scaledWidth
//        val height = res.scaledHeight
        context.setTime(partialTicks)
        context.setScaledResolution(mc.window)
        context.z = 0f//zLevel
        GLCore.glBlend(true)
        mc.profiler.pop()

//        super.renderGameOverlay(partialTicks)

//        if (OptionCore.ALWAYS_SHOW.isEnabled && !this.mc.player.shouldDrawHUD() && this.mc.renderViewEntity is Player) {
            /*if (renderHealth)*/ renderHealth(poseStack)
            // TODO add option//
            /*if (renderArmor)*/ renderArmor(poseStack)
            /*if (renderFood)*/ renderFood(poseStack)
            /*if (renderHealthMount)*/ renderHealthMount(poseStack)
            /*if (renderAir)*/ renderAir(poseStack)
//            mc.entityRenderer.setupOverlayRendering()
//        } // Basically adding what super doesn't render by default
    }

    private fun renderCrosshairs(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderCrosshairs(partialTicks)
        } else {
            themeManager.HUD.draw(HudPartType.CROSS_HAIR, context, poseStack)
            this.renderAttackIndicator(poseStack)
//            if (OptionCore.RENDER_CROSSHAIRS.isEnabled) initiateEvents(CROSSHAIRS)
        }
    }

    private fun renderArmor(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderArmor(width, height)
        } else {
            themeManager.HUD.draw(HudPartType.ARMOR, context, poseStack)
//            if (OptionCore.RENDER_ARMOR.isEnabled) initiateEvents(ARMOR)
        }
    }

    private fun renderHotbar(poseStack: PoseStack) {
        if (OptionCore.DEFAULT_HOTBAR.isEnabled || OptionCore.VANILLA_UI.isEnabled) ;/*super.renderHotbar(
            res,
            partialTicks
        )*/
        else {
            if (mc.player?.isSpectator == true) {
                this.spectatorGui.renderTooltip(poseStack)
            } else {
                mc.profiler.push("hotbar")
                themeManager.HUD.draw(HudPartType.HOTBAR, context, poseStack)
                mc.profiler.pop()
            }
//            if (OptionCore.RENDER_HOTBAR.isEnabled) initiateEvents(HOTBAR)
        }
    }

    private fun renderAir(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderAir(width, height)
        } else {
            mc.profiler.push("air")
            themeManager.HUD.draw(HudPartType.AIR, context, poseStack)
            mc.profiler.pop()
//            if (OptionCore.RENDER_AIR.isEnabled) initiateEvents(AIR)
        }
    }

    private fun renderAttackIndicator(poseStack: PoseStack) {
        /*if (trueOptionCore.VANILLA_UI.isEnabled) {
            GLCore.glBindTexture(Gui.ICONS)
//            super.renderAttackIndicator(p_184045_1_, p_184045_2_)
        }*/
        // todo: implement
    }

    private fun renderPotionEffects(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) ;//super.renderPotionEffects(resolution)
        else {
            themeManager.HUD.draw(HudPartType.EFFECTS, context, poseStack)
            // mc.profile("potionEffects") {
            //
            // }
        }
    }

    private fun renderPotionIcons(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) ;//super.renderPotionIcons(resolution)
        else {
            this.renderPotionEffects(poseStack)
//            if (OptionCore.RENDER_POTION_ICONS.isEnabled) initiateEvents(POTION_ICONS)
        }
    }

    private fun renderHealth(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderHealth(width, height)
        } else {
            themeManager.HUD.draw(HudPartType.HEALTH_BOX, context, poseStack)
//            if (OptionCore.RENDER_HEALTH.isEnabled) initiateEvents(HEALTH)
        }
//        renderParty()
        if (OptionCore.ENEMY_ONSCREEN_HEALTH.isEnabled) renderEnemyHealth(poseStack)
    }

    /*private fun renderParty() {
        val pt = mc.player.getPartyCapability().partyData
        if ((pt == null || !pt.isParty) && ConfigHandler.debugFakePT == 0) return

        mc.profiler.push("party")

        GlStateManager.disableLighting()
        GLCore.glAlphaTest(true)
        GLCore.glBlend(true)

        var members: MutableList<PlayerInfo> = mutableListOf()
        if (pt?.isParty == true) {
            members = pt.getMembers().filter { !it.equals(mc.player) }.toMutableList()
            if (OptionCore.HIDE_OFFLINE_PARTY.isEnabled) members.removeIf { it.player == null }
        } else {
            for (i in 0 until ConfigHandler.debugFakePT) members.add(PlayerInfo(mc.player))
        }

        context.setPt(members)
        themeManager.HUD.draw(HudPartType.PARTY, context)

        GlStateManager.disableLighting()
        mc.profiler.pop()
    }*/

    private fun renderEnemyHealth(poseStack: PoseStack) {
        mc.profiler.push("enemy health")
        val entities: MutableList<LivingEntity> = mutableListOf()
        val trackedEntity = getMouseOver(mc.frameTime)
        if (trackedEntity is LivingEntity /*&& trackedEntity.getRenderData()?.colorStateHandler?.shouldDrawHealth() == true*/) context.setTargetEntity(
            trackedEntity
        )
        else context.setTargetEntity(null)
        entities.addAll(
            mc.level!!.getEntities(
                mc.player!!,
                AABB.ofSize(mc.player!!.position(), 20.0, 10.0, 20.0)
            ) {
                it is LivingEntity /*&& it.getRenderData()?.isAggressive == true && it.getRenderData()?.colorStateHandler?.shouldDrawHealth() == true*/ && !entities.contains(it)
            }.map { it as LivingEntity }.sortedBy { LivingEntity -> LivingEntity.distanceToSqr(mc.player!!) }.take(5)
        )
        entities.sortBy { it.health / it.maxHealth }
        context.setNearbyEntities(entities)
        /*val baseY = 35
        val h = 15.0
        val offset = width - 20.0*/
        GLCore.glCullFace(false)
        GLCore.glBlend(true)
        themeManager.HUD.draw(HudPartType.ENTITY_HEALTH_HUD, context, poseStack)
        /*

        entities.sortedBy { it.health / it.maxHealth }.forEachIndexed { index, entity ->
            GLCore.glBindTexture(StringNames.entities)
            val health = entity.health / entity.maxHealth
            if (entity.getRenderData() != null)
                GLCore.color(entity.getRenderData()!!.getColorStateHandler().colorState.rgba)
            else
                HealthStep.VERY_LOW.glColor()
            GLCore.glTexturedRectV2(offset - 1, baseY + 1.5 + index * h, zLevel.toDouble(), -79.0 * health, 14.0, 1.0, 0.0, srcWidth = 255.0, srcHeight = 30.0)
            GLCore.color(1.0F, 1.0F, 1.0F, 1.0F)
            GLCore.glTexturedRectV2(offset, baseY + index * h, zLevel.toDouble(), -80.0, 15.0, 1.0, 30.0, srcWidth = 255.0, srcHeight = 30.0)
            var name = entity.displayName.formattedText
            if (name.length > 10) name = name.substring(0, 10)
            GLCore.glString(name, Vec2d(offset - GLCore.glStringWidth(name) - 5, baseY + 1 + index * h + (13 - fontRenderer.FONT_HEIGHT) / 2), ColorUtil.DEFAULT_COLOR.rgba)
        }
        GLCore.glCullFace(true)

         */

        mc.profiler.pop()
    }

    /**
     * Gets the block or object that is being moused over.
     */
    private fun getMouseOver(partialTicks: Float): Entity? {
        mc.profiler.push("track enemy")
        val entity = mc.cameraEntity
        var pointedEntity: Entity? = null
        if (entity != null) {
            if (mc.level != null) {
                mc.crosshairPickEntity = null
                val distance = 32.0
                val vec3d = entity.getEyePosition(partialTicks)
                val vec3d1 = entity.lookAngle
                val vec3d1TimesDistance = vec3d1.multiply(distance, distance, distance)
                val vec3d2 = vec3d.add(vec3d1TimesDistance)
                val list = mc.level!!.getEntities(
                    EntityTypeTest.forClass(LivingEntity::class.java),
                    entity.boundingBox.expandTowards(vec3d1TimesDistance).inflate(1.0, 1.0, 1.0),
                    EntitySelector.NO_SPECTATORS
                        .and { it !== entity }
                        .and { it != null && it.canBeCollidedWith() }
                )
                var d2 = distance * distance
                for (j in list.indices) {
                    val entity1 = list[j]
                    val axisalignedbb = entity1.boundingBox//.inflate(entity1.collisionBorderSize.toDouble())
                    val raytraceresult = axisalignedbb.clip(vec3d, vec3d2)
                    if (axisalignedbb.contains(vec3d)) {
                        if (d2 >= 0.0) {
                            pointedEntity = entity1
                            d2 = 0.0
                        }
                    } else raytraceresult.ifPresent { 
                        val d3 = vec3d.distanceToSqr(it)
                        if (d3 < d2 || d2 == 0.0) {
                            if (false /*entity1.lowestRidingEntity === entity.lowestRidingEntity && !entity1.canRiderInteract()*/) {
                                if (d2 == 0.0) {
                                    pointedEntity = entity1
                                }
                            } else {
                                pointedEntity = entity1
                                d2 = d3
                            }
                        }
                    }
                }
            }
        }
        mc.profiler.pop()
        return pointedEntity
    }

    private fun renderFood(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            GLCore.glBindTexture(Gui.ICONS)
//            super.renderFood(width, height)
        } else {
            mc.profiler.push("food")
            themeManager.HUD.draw(HudPartType.FOOD, context, poseStack)
            mc.profiler.pop()
//            if (OptionCore.RENDER_FOOD.isEnabled) initiateEvents(FOOD)
        }
    }

    private fun renderExperience(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderExperience(width, height)
        } else {
//            if (!OptionCore.ALWAYS_SHOW.isEnabled && !this.mc.player.shouldDrawHUD()) return
            mc.profiler.push("expLevel")

            themeManager.HUD.draw(HudPartType.EXPERIENCE, context, poseStack)

            mc.profiler.pop()
//            if (OptionCore.RENDER_EXPERIENCE.isEnabled) initiateEvents(EXPERIENCE)
        }
    }

    private fun renderJumpBar(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderJumpBar(width, height)
        } else {
            // These are here because vanilla hides them when showing jump bar
            renderExperience(poseStack)
            renderFood(poseStack)

            mc.profiler.push("jumpBar")
            themeManager.HUD.draw(HudPartType.JUMP_BAR, context, poseStack)
            mc.profiler.pop()

//            if (OptionCore.RENDER_JUMPBAR.isEnabled) initiateEvents(JUMPBAR)
        }
    }

    private fun renderHealthMount(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled) {
//            super.renderHealthMount(width, height)
        } else {
            themeManager.HUD.draw(HudPartType.MOUNT_HEALTH, context, poseStack)
//            if (OptionCore.RENDER_HEALTHMOUNT.isEnabled) initiateEvents(HEALTHMOUNT)
        }
    }

    private fun renderHUDText(poseStack: PoseStack) {
        if (OptionCore.VANILLA_UI.isEnabled || OptionCore.DEFAULT_DEBUG.isEnabled) {
//            super.renderHUDText(width, height)
        } else {
            mc.profiler.push("forgeHudText")
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA/*, 1, 0*/)
            val listL = ArrayList<String>()
            val listR = ArrayList<String>()

            /*if (mc.isDemo) {
                val time = mc.world.totalWorldTime
                if (time >= 120500L) {
                    listR.add(I18n.get("demo.demoExpired"))
                } else {
                    listR.add(I18n.get("demo.remainingTime", StringUtils.ticksToElapsedTime((120500L - time).toInt())))
                }
            }*/

            if (this.mc.options.renderDebug /*&& !pre(DEBUG)*/) {
//                listL.addAll(debugOverlay.left)
//                listR.addAll(debugOverlay.right)
//                post(DEBUG)
            }

            /*val event = RenderGameOverlayEvent.Text(eventParent!!, listL, listR)
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                var top = 20
                for (msg in listL) {
                    drawRect(1, top - 1, 2 + font.width(msg) + 1, top + fontRenderer.FONT_HEIGHT - 1, -1873784752)
                    fontRenderer.drawString(msg, 2, top, 14737632)
                    top += fontRenderer.FONT_HEIGHT
                }

                top = 2
                for (msg in listR) {
                    val w = font.width(msg)

                    val slotsY = (height - 9 * 22) / 2
                    //                        (res.getScaledHeight() - (slotCount * 22)) / 2;

                    *//*for (int i = 0; i < slotCount; i++) {
                    GLCore.color(i == inv.currentItem ? 0xFFBA66AA : 0xCDCDCDAA);
                    GLCore.glTexturedRect(res.getScaledWidth() - 24, slotsY + (22 * i), zLevel, 0, 25, 20, 20);
                }*//*

                    val left =
                        width - (if (OptionCore.HOR_HOTBAR.isEnabled || top < slotsY - fontRenderer.FONT_HEIGHT - 2) 2 else 26) - w
                    drawRect(left - 1, top - 1, left + w + 1, top + fontRenderer.FONT_HEIGHT - 1, -1873784752)
                    fontRenderer.drawString(msg, left, top, 14737632)
                    top += fontRenderer.FONT_HEIGHT
                }
            }*/

            mc.profiler.pop()
//            post(TEXT)
        }
    }

    /*fun initiateEvents(type: ElementType) {
        if (!pre(type)) post(type)
    }*/

    // c/p from GuiIngameForge
    // TODO : port to GuiOverlayManager.getOverlays()
    /**
     * Returns true if cancelled.
     */
    /*private fun pre(type: ElementType): Boolean {
        return MinecraftForge.EVENT_BUS.post(RenderGameOverlayEvent.Pre(eventParent!!, type))
    }

    private fun post(type: ElementType) {
        MinecraftForge.EVENT_BUS.post(RenderGameOverlayEvent.Post(eventParent!!, type))
    }*/

    /*private inner class GuiOverlayDebugForge constructor(mc: Minecraft) : GuiOverlayDebug(mc) {

        override fun renderDebugInfoLeft() {}

        override fun renderDebugInfoRight(res: ScaledResolution) {}

        val left: List<String>
            get() = this.call()

        val right: MutableList<String>
            get() = this.getDebugInfoRight<Comparable<Any>>()
    }*/
}
