package project.kompass.illagerVZombies.listener

import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.IronGolem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import project.kompass.illagerVZombies.IllagerVZombies
import project.kompass.illagerVZombies.config.Config
import java.util.logging.Level

class MobDeathXPListener(private val plugin: IllagerVZombies) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        if (entity is Player) return
        if (event.droppedExp > 0) return

        val damageEvent = entity.lastDamageCause ?: return

        val world = entity.world
        if (Config.disabledWorlds.contains(world.name)) return

        val damageCause = damageEvent.cause

        val match = when (damageCause) {
            DamageCause.WITHER -> Config.enabledWitherRose
            DamageCause.HOT_FLOOR -> Config.enabledHotFloor
            DamageCause.CRAMMING -> Config.enabledCramming
            DamageCause.LAVA -> Config.enabledLava
            DamageCause.FALL -> Config.enabledFall
            DamageCause.ENTITY_ATTACK -> {
                Config.enabledIronGolem && isIronGolemKill(damageEvent)
            }
            DamageCause.CONTACT -> {
                (Config.enabledPointedDripstone && isPointedDripstone(damageEvent)) ||
                        (Config.enabledStonecutter && isStonecutter(damageEvent))
            }
            DamageCause.FALLING_BLOCK -> {
                Config.enabledPointedDripstone && isPointedDripstone(damageEvent)
            }
            else -> false
        }

        if (match) {
            try {
                val nmsEntity = (entity as CraftLivingEntity).handle
                val nmsServerLevel = (world as CraftWorld).handle

                val exp = nmsEntity.getExperienceReward(nmsServerLevel, null)

                if (Config.debug) {
                    plugin.logger.log(Level.INFO, "Entity ${entity.type} dropped $exp experience on death due to ${damageCause.name}")
                }

                event.droppedExp = exp
            } catch (e: Exception) {
                if (Config.debug) {
                    plugin.logger.log(Level.WARNING, "Failed to get NMS experience reward: ${e.message}")
                }
            }
        }
    }

    private fun isIronGolemKill(event: EntityDamageEvent): Boolean {
        return event is EntityDamageByEntityEvent && event.damager is IronGolem
    }

    private fun isPointedDripstone(event: EntityDamageEvent): Boolean {
        return if (event is EntityDamageByBlockEvent) {
            event.damager?.type == Material.POINTED_DRIPSTONE
        } else if (event is EntityDamageByEntityEvent) {
            val damager = event.damager
            damager is FallingBlock && damager.blockData.material == Material.POINTED_DRIPSTONE
        } else {
            false
        }
    }

    private fun isStonecutter(event: EntityDamageEvent): Boolean {
        return event is EntityDamageByBlockEvent && event.damager?.type == Material.STONECUTTER
    }
}