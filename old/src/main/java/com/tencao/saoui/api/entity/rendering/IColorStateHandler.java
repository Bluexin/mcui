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

package com.tencao.saoui.api.entity.rendering;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Part of saoui
 * <p>
 * Handles the changing of color states for an entity.
 *
 * @author Bluexin
 */
@FunctionalInterface
public interface IColorStateHandler {

    /**
     * @return the color state the entity should be showing.
     */
    ColorState getColorState();

    /**
     * Called every render tick.
     * Use this to toggle crystals.
     */
    default boolean shouldDrawCrystal() {
        return true;
    }

    /**
     * Called every render tick.
     * Use this to toggle health bars.
     */
    default boolean shouldDrawHealth() {
        return true;
    }

    /**
     * Called every tick.
     * Use this to handle anything special.
     */
    default void tick() {
    }

    /**
     * Save any data to NBT format.
     * The implementation has to create his own sub-tag and to behave properly.
     * See {@link PlayerColorStateHandler#save(NBTTagCompound)} for an example.
     * This will be used for sync between client and server, and saving on world shutting down.
     *
     * @param tag the NBT tag to save to
     */
    default void save(NBTTagCompound tag) {
    }

    /**
     * Load any data from NBT format.
     * The implementation has to retrieve his own sub-tag and to behave properly.
     * See {@link PlayerColorStateHandler#load(NBTTagCompound)} for an example.
     * This will be used for sync between client and server, and loading on world starting up.
     *
     * @param tag the NBT tag to save to
     */
    default void load(NBTTagCompound tag) {
    }
}
