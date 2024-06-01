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

import be.bluexin.mcui.util.Client
import be.bluexin.mcui.GLCore
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11

/**
 * This code was original created by <Vazkii> and has been modified to our needs
 * All credit goes to him
</Vazkii> */
@SideOnly(Side.CLIENT)
object RenderDispatcher {

    fun dispatch() {
        GLCore.begin()
        GLCore.glBlend(true)
        GLCore.blendFunc(GL11.GL_ONE, GL11.GL_ONE)
        GLCore.lighting(false)
        Client.mc.mcProfiler.startSection("death particle")
        DeathParticles.dispatchQueuedRenders()
        Client.mc.mcProfiler.endSection()
        GLCore.blendFunc(GL11.GL_ONE, GL11.GL_ZERO)
        GLCore.glBlend(false)
        GLCore.draw()
    }
}
