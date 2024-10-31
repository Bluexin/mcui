package be.bluexin.mcui.themes.miniscript.api

import net.minecraft.world.entity.LivingEntity
import java.lang.ref.WeakReference
import kotlin.math.min

interface MiniscriptLivingEntity {

    /**
     * @return the entity's display name (username in case of players)
     */
    fun displayName(): String

    /**
     * @return the current hp of the entity on a scale from 0.0 to 1.0
     */
    fun healthPercent(): Double = min(health() / maxHealth().toDouble(), 1.0)

    /**
     * @return the current hp of the entity (1 heart = 2 HP)
     */
    fun health(): Float

    /**
     * @return the current maximum hp of the entity (1 heart = 2 HP)
     */
    fun maxHealth(): Float

    /**
     * @return the armor value of the entity
     */
    fun armor(): Int

    /**
     * @return whether the entity is underwater
     */
    fun isInWater(): Boolean

    /**
     * @return the current air level of the entity
     */
    fun currentAir(): Int

    /**
     * @return the max air level of the entity
     */
    fun maxAir(): Int = 300

    /**
     * @return the current air level of the entity on a scale from 0.0 to 1.0
     */
    fun airPercent(): Float = min((currentAir() / maxAir().toFloat()), 1.0F)

    /**
     * @return whether this entity is riding another living entity
     */
    fun hasMount(): Boolean

    /**
     * @return the current living mount of the player
     */
    fun mount(): MiniscriptLivingEntity?
}

internal class MiniscriptLivingEntityImpl(
    entity: LivingEntity
) : MiniscriptLivingEntity {

    private val entityRef = WeakReference(entity)
    private val entity = requireNotNull(entityRef.get()) { "Player reference was cleared" }

    override fun displayName(): String = entity.displayName.string

    override fun health() = entity.health
    override fun maxHealth() = entity.maxHealth
    override fun armor() = entity.armorValue

    override fun isInWater() = entity.isInWater
    override fun currentAir() = entity.airSupply

    private var mountCache: MiniscriptLivingEntityImpl? = null
    override fun hasMount(): Boolean = entity.vehicle is LivingEntity
    override fun mount(): MiniscriptLivingEntity? {
        val v = entity.vehicle
        if (v == null && mountCache != null) {
            mountCache = null
        } else if (mountCache?.entity !== v) {
            mountCache = (entity.vehicle as? LivingEntity)
                ?.let { MiniscriptLivingEntityImpl(it) }
        }
        return mountCache
    }
}