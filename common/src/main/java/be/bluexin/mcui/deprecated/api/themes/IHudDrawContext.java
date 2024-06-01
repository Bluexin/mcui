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

package be.bluexin.mcui.deprecated.api.themes;


import be.bluexin.mcui.effects.StatusEffects;
import be.bluexin.mcui.themes.miniscript.CValue;
import be.bluexin.mcui.util.HealthStep;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Getters to use in JEL (for access in xml themes).
 * <p>
 * These are accessible in the HUD xml.
 *
 * Everywhere "percent" or "scale of 1" is mentioned, it means the value will be a decimal ranging from 0.0 (included)
 * to 1.0 (included).
 *
 * @author Bluexin
 */
@SuppressWarnings("unused") // Used to get access in JEL
public interface IHudDrawContext {

    /**
     * @return the player's username
     */
    String username();

    /**
     * @return the width of the player's username (in pixel, with the current font)
     */
    double usernamewidth();

    /**
     * @return the current hp of the player, in percentage of 1
     */
    double hpPct();

    /**
     * @return the current hp of the player (1 heart = 2 HP)
     */
    float hp();

    /**
     * @return the current maximum hp of the player (1 heart = 2 HP)
     */
    float maxHp();

    /**
     * @return the health step the player is currently at
     */
    HealthStep healthStep();

    /**
     * @return the id of the hotbar slot the player is using (from 0 to 8)
     */
    int selectedslot();

    /**
     * @return screen width, scaled
     */
    int scaledwidth();

    /**
     * @return screen heigth, scaled
     */
    int scaledheight();

    /**
     * Used to know whether the specified offhand slot is empty.
     * Currently there is only 1 offhand slot.
     *
     * @param slot offhand slot to query
     * @return whether the specified offhand slot is empty
     */
    boolean offhandEmpty(int slot);

    /**
     * Used to get the width in pixels of the string with the current fontrenderer.
     *
     * @param s the string to query the width of
     * @return width in pixels of the provided string with current fontrenderer
     */
    int strWidth(String s);

    /**
     * Used to get the height in pixels of the font with the current fontrenderer.
     *
     * @return height in pixels of the current font with the fontrenderer
     */
    int strHeight();

    /**
     * @return the current absorption amount the player has
     */
    float absorption();


    /**
     * @return the current experience level of the player
     */
    int level();

    /**
     * @return the current experience percent of the player (scale of 1)
     */
    float experience();

    /**
     * @return z value. Shouldn't be needed in theme
     */
    float getZ();

    /**
     * @return current font renderer. Useless in themes for now
     */
    Font getFontRenderer();

    /**
     * @return current item renderer. Useless in themes for now
     */
    ItemRenderer getItemRenderer();

    /**
     * @return current player. Useless in themes for now
     */
    Player getPlayer();

    /**
     * @return partial ticks
     */
    float getPartialTicks();

    /**
     * @return horse jump value (on a scale of 1)
     */
    float horsejump();

    /**
     * Internal
     */
    void setI(int i);

    /**
     * @return index in repetition groups
     */
    int i();

    /**
     * @param index the index of the party member to check
     * @return username of the party member at given index
     */
    String ptName(int index);

    /**
     * @param index the index of the party member to check
     * @return hp of the party member at given index
     */
    float ptHp(int index);

    /**
     * @param index the index of the party member to check
     * @return max hp of the party member at given index
     */
    float ptMaxHp(int index);

    /**
     * @param index the index of the party member to check
     * @return hp percent of the party member at given index
     */
    float ptHpPct(int index);

    /**
     * @param index the index of the party member to check
     * @return health step of the party member at given index
     */
    HealthStep ptHealthStep(int index);

    /**
     * @return current party size
     */
    int ptSize();

    /**
     * @return player food level
     */
    float foodLevel();

    /**
     * @return player food max value
     */
    default float foodMax() {
        return 20.0f;
    }

    /**
     * @return player food percentage
     */
    default float foodPct() {
        return Math.min(foodLevel() / foodMax(), 1.0f);
    }

    /**
     * @return player saturation level
     */
    float saturationLevel();

    /**
     * @return player saturation max value
     */
    default float saturationMax() {
        return 20.0f;
    }

    /**
     * @return player saturation percentage
     */
    default float saturationPct() {
        return Math.min(saturationLevel() / saturationMax(), 1.0f);
    }

    /**
     * @return player's current status effects
     */
    List<StatusEffects> statusEffects();

    /**
     * @param i index to check
     * @return status effect at given index
     */
    default StatusEffects statusEffect(int i) {
        return statusEffects().get(i);
    }

    /**
     * @return whether the current player is riding an entity
     */
    boolean hasMount();

    /**
     * @return current mount hp (or 0 if none)
     */
    float mountHp();

    /**
     * @return mount max hp (or 1 if none)
     */
    float mountMaxHp();

    /**
     * @return mount hp percentage (or 1 if none)
     */
    default float mountHpPct() {
        return hasMount() ? Math.min(mountHp() / mountMaxHp(), 1.0f) : 1.0f;
    }

    /**
     * @return whether the player is under water
     */
    boolean inWater();

    /**
     * @return current air level
     */
    int air();

    /**
     * @return max air level
     */
    default int airMax() {
        return 300;
    }

    /**
     * @return air level percentage
     */
    default float airPct() {
        return Math.min(air() / (float) airMax(), 1.0f);
    }

    /**
     * @return armor value
     */
    int armor();

    // *** NEARBY ENTITIES ***

    /**
     * @return up to 5 nearby entities
     */
    List<LivingEntity> nearbyEntities();

    /**
     * @param i index to check
     * @return Entity at given index
     */
    default LivingEntity nearbyEntity(int i) {
        return nearbyEntities().get(i);
    }

    /**
     * @return nearby entity amount
     */
    default int nearbyEntitySize() { return nearbyEntities().size(); }

    /**
     * @param index the index of the entity to check
     * @return username of the entity at given index
     */
    String entityName(int index);

    /**
     * @param index the index of the entity to check
     * @return hp of the entity at given index
     */
    float entityHp(int index);

    /**
     * @param index the index of the entity to check
     * @return max hp of the entity at given index
     */
    float entityMaxHp(int index);

    /**
     * @param index the index of the entity to check
     * @return hp percent of the entity at given index
     */
    float entityHpPct(int index);

    /**
     * @param index the index of the entity to check
     * @return health step of the entity at given index
     */
    HealthStep entityHealthStep(int index);


    // *** TARGET ENTITY ***

    default boolean hasTargetEntity() {
        return targetEntity() != null;
    }

    LivingEntity targetEntity();
    /**
     * @return username of the target
     */
    String targetName();

    /**
     * @return hp of the target
     */
    float targetHp();

    /**
     * @return max hp of the target
     */
    float targetMaxHp();

    /**
     * @return hp percent of the target
     */
    float targetHpPct();

    /**
     * @return health step of the target
     */
    HealthStep targetHealthStep();

    ProfilerFiller getProfiler();

    /**
     * JEL access
     */
    String getStringProperty(@Nonnull final String name);
    double getDoubleProperty(@Nonnull final String name);
    int getIntProperty(@Nonnull final String name);
    boolean getBooleanProperty(@Nonnull final String name);
    void getUnitProperty(@Nonnull final String name);

    default void pushContext(@Nonnull Map<String, CValue<Object>> context) {
    }

    default void popContext() {}
}
