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

package be.bluexin.mcui.effects

import be.bluexin.mcui.GLCore
import be.bluexin.mcui.resources.StringNames
import net.minecraft.Client.mc
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@SideOnly(Side.CLIENT)
class DeathParticles private constructor(world: World, xCoord: Double, yCoord: Double, zCoord: Double, redValue: Float, greenValue: Float, blueVale: Float, scale: Float) : Particle(world, xCoord, yCoord, zCoord, 0.0, 0.0, 0.0) {

    private var time: Float = 0.toFloat()
    private var particleX: Float = 0.toFloat()
    private var particleZ: Float = 0.toFloat()
    private var yz: Float = 0.toFloat()
    private var xy: Float = 0.toFloat()
    private var xz: Float = 0.toFloat()
    private var rotationY: Float = 0.toFloat() // Rotation around Y axis
    private val speedRotationY: Float // Rotation speed around Y axis

    constructor(world: World, xCoord: Double, yCoord: Double, zCoord: Double, redValue: Float, greenValue: Float, blueValue: Float) : this(world, xCoord, yCoord, zCoord, redValue, greenValue, blueValue, 1.0f)

    init {
        this.motionX = ((Math.random() * 2.0 - 1.0).toFloat() * 0.05f).toDouble()
        this.motionY = ((Math.random() * 2.0 - 1.0).toFloat() * 0.05f).toDouble()
        this.motionZ = ((Math.random() * 2.0 - 1.0).toFloat() * 0.05f).toDouble()
        this.rotationY = rand.nextFloat() * 2
        this.speedRotationY = (rand.nextFloat() + 2.0f) / if (rand.nextBoolean()) 16.0f else -16.0f
        this.particleRed = redValue
        this.particleGreen = greenValue
        this.particleBlue = blueVale
        this.particleScale *= scale
        this.particleMaxAge = (8.0 / (Math.random() * 0.8 + 0.2)).toInt()
        this.particleMaxAge = (this.particleMaxAge.toFloat() * scale).toInt()
    }

    override fun renderParticle(worldrender: BufferBuilder, entity: Entity?, time: Float, x: Float, z: Float, yz: Float, xy: Float, xz: Float) {
        this.time = time
        this.particleX = x
        this.particleZ = z
        this.yz = yz
        this.xy = xy
        this.xz = xz

        queuedRenders.add(this)
    }

    private fun renderQueued() {
        var particle = (this.particleAge.toFloat() + time) / this.particleMaxAge.toFloat() * 32.0f

        if (particle < 0.0f) particle = 0.0f

        if (particle > 1.0f) particle = 1.0f

        val scale = 0.1f * this.particleScale * particle
        val xPos = (this.prevPosX + (this.posX - this.prevPosX) * time.toDouble() - interpPosX).toFloat()
        val yPos = (this.prevPosY + (this.posY - this.prevPosY) * time.toDouble() - interpPosY).toFloat()
        val zPos = (this.prevPosZ + (this.posZ - this.prevPosZ) * time.toDouble() - interpPosZ).toFloat()
        val colorIntensity = 1.0f

        val x1: Double = (-(this.particleX + xy) * scale).toDouble()
        val y1: Double = (-this.particleZ * scale).toDouble()
        val z1: Double = (-(this.yz + xz) * scale).toDouble()
        val x2: Double = ((xy - this.particleX) * scale).toDouble()
        val y2: Double = (this.particleZ * scale).toDouble()
        val z2: Double = ((xz - this.yz) * scale).toDouble()
        val x3: Double = ((this.particleX + xy) * scale).toDouble()
        val y3: Double = (this.particleZ * scale).toDouble()
        val z3: Double = ((this.yz + xz) * scale).toDouble()
        val x4: Double = ((this.particleX - xy) * scale).toDouble()
        val y4: Double = (-this.particleZ * scale).toDouble()
        val z4: Double = ((this.yz - xz) * scale).toDouble()
        val e = Minecraft.getMinecraft().player.horizontalFacing
        val q = e == EnumFacing.NORTH || e == EnumFacing.SOUTH
        val a = (if (q) if (rotationY < 1.5f && rotationY > 0.5f) rotationY - 1.0f else rotationY + 1.0f else rotationY) * Math.PI
        val cos = cos(a)
        val sin = sin(a)

        if (a < Math.PI) {
            GLCore.addVertex(xPos + x1 * cos, yPos + y1, zPos + z1 * sin, 0.0, 1.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos + x2 * cos, yPos + y2, zPos + z2 * sin, 1.0, 1.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos + x3 * cos, yPos + y3, zPos + z3 * sin, 1.0, 0.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos + x4 * cos, yPos + y4, zPos + z4 * sin, 0.0, 0.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
        } else {
            GLCore.addVertex(xPos - x1 * cos, yPos + y1, zPos - z1 * sin, 0.0, 1.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos - x2 * cos, yPos + y2, zPos - z2 * sin, 1.0, 1.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos - x3 * cos, yPos + y3, zPos - z3 * sin, 1.0, 0.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
            GLCore.addVertex(xPos - x4 * cos, yPos + y4, zPos - z4 * sin, 0.0, 0.0, this.particleRed * colorIntensity, this.particleGreen * colorIntensity, this.particleBlue * colorIntensity, 1f)
        }
    }

    /**
     * Called to update the entity's position/logic.
     */

    override fun onUpdate() {
        this.prevPosX = this.posX
        this.prevPosY = this.posY
        this.prevPosZ = this.posZ

        if (this.particleAge++ >= this.particleMaxAge) this.isExpired = true

        this.motionY += 0.004
        this.move(this.motionX, this.motionY, this.motionZ)
        this.motionX *= 0.8999999761581421
        this.motionY *= 0.8999999761581421
        this.motionZ *= 0.8999999761581421
        this.rotationY += this.speedRotationY
        this.rotationY = this.rotationY % 2.0f

        if (this.onGround) {
            this.motionX *= 0.699999988079071
            this.motionZ *= 0.699999988079071
        }
    }

    companion object {

        var queuedRenders: Queue<DeathParticles> = ArrayDeque()

        internal fun dispatchQueuedRenders() {
            Minecraft.getMinecraft().renderEngine.bindTexture(StringNames.particleLarge)

            GLCore.glAlphaTest(true)
            // GLCore.glBlend(true)
            queuedRenders.forEach { p ->
                p.renderQueued()
            }
            // GLCore.glBlend(false)

            queuedRenders.clear()
        }
    }
}
