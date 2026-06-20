package project.kompass.illagerVZombies.listener

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Creeper
import org.bukkit.entity.IronGolem
import org.bukkit.entity.Snowball
import org.bukkit.entity.Snowman
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import java.util.UUID

class SnowmanCreeperListener(plugin: Plugin) : Listener {

    private val snowmanHits = HashMap<UUID, Int>()
    private val creeperTargets = HashMap<UUID, UUID>()
    private val snowmanShooterKey = NamespacedKey(plugin, "snowman_shooter")

    @EventHandler(priority = EventPriority.LOWEST)
    fun onSnowballLaunch(event: ProjectileLaunchEvent) {
        val snowball = event.entity
        if (snowball is Snowball && snowball.shooter is Snowman) {
            val snowman = snowball.shooter as Snowman
            snowball.persistentDataContainer.set(
                snowmanShooterKey,
                PersistentDataType.STRING,
                snowman.uniqueId.toString()
            )
        }
    }

    @EventHandler
    fun onCreeperHitBySnowball(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity
        val snowball = event.entity
        if (hitEntity is Creeper && snowball is Snowball) {
            val snowman = findAttackingSnowman(hitEntity, snowball)

            if (snowman != null) {
                val creeperId = hitEntity.uniqueId
                val hits = snowmanHits.getOrDefault(creeperId, 0) + 1

                if (hits >= 1) {
                    snowmanHits.remove(creeperId)
                    creeperTargets[creeperId] = snowman.uniqueId
                    hitEntity.target = snowman
                } else {
                    snowmanHits[creeperId] = hits
                }
            }
        }
    }

    private fun findAttackingSnowman(creeper: Creeper, snowball: Snowball): Snowman? {
        val container = snowball.persistentDataContainer
        val uuidStr = container.get(snowmanShooterKey, PersistentDataType.STRING)
        if (uuidStr != null) {
            try {
                val uuid = UUID.fromString(uuidStr)
                val entity = Bukkit.getEntity(uuid)
                if (entity is Snowman && entity.isValid) {
                    return entity
                }
            } catch (ignored: IllegalArgumentException) {}
        }

        val shooter = snowball.shooter
        if (shooter is Snowman && shooter.isValid) {
            return shooter
        }

        // Fallback: Max targeting range of Snowman is 16 blocks; scanning 30 blocks is highly inefficient
        var closestDistSq = Double.MAX_VALUE
        var closestSnowman: Snowman? = null
        for (nearby in creeper.getNearbyEntities(16.0, 16.0, 16.0)) {
            if (nearby is Snowman && nearby.isValid) {
                if (nearby.target?.uniqueId == creeper.uniqueId) {
                    return nearby
                }
                val distSq = creeper.location.distanceSquared(nearby.location)
                if (distSq < closestDistSq) {
                    closestDistSq = distSq
                    closestSnowman = nearby
                }
            }
        }
        return closestSnowman
    }

    @EventHandler
    fun onCreeperTarget(event: EntityTargetEvent) {
        val entity = event.entity
        if (entity !is Creeper) return // Fast early exit

        if (event.target is IronGolem) {
            event.isCancelled = true
            return
        }

        val creeperId = entity.uniqueId
        val snowmanId = creeperTargets[creeperId] ?: return

        val snowman = Bukkit.getEntity(snowmanId)
        if (snowman != null && snowman.isValid && !snowman.isDead) {
            if (entity.location.distanceSquared(snowman.location) <= 1024.0) {
                if (event.target?.uniqueId != snowmanId) {
                    event.target = snowman
                }
            } else {
                creeperTargets.remove(creeperId)
            }
        } else {
            creeperTargets.remove(creeperId)
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val uuid = event.entity.uniqueId
        snowmanHits.remove(uuid)
        creeperTargets.remove(uuid)
        creeperTargets.values.removeIf { targetId -> targetId == uuid }
    }
}