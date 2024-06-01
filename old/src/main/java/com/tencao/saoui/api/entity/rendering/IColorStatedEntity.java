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

/**
 * Part of saoui
 * <p>
 * Implementing this marks the object as having an instance of {@link IColorStateHandler} to supply.
 * It (currently) only has an effect on subclasses of {@link net.minecraft.entity.EntityLivingBase}
 *
 * @author Bluexin
 */
@FunctionalInterface
public interface IColorStatedEntity {

    /**
     * Gets the instance of {@link ICustomizationProvider} the capability has to use.
     *
     * @return instance to use
     */
    IColorStateHandler getColorState();
}
