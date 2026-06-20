package project.kompass.illagerVZombies.listener

import org.bukkit.Material
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftLivingEntity
import org.bukkit.entity.FallingBlock
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

        // Fast early exit 1: Players do not trigger custom environmental mob XP
        if (entity is Player) return

        // Fast early exit 2: If the mob already drops XP (e.g. standard player/wolf kill), bypass immediately
        if (event.droppedExp > 0) return

        // Fast early exit 3: Ensure there is actually a registered damage cause
        val damageEvent = entity.lastDamageCause ?: return

        // Fast early exit 4: Verify the world isn't blacklisted (HashSet check is deferred here to save CPU)
        val world = entity.world
        if (Config.disabledWorlds.contains(world.name)) return

        val damageCause = damageEvent.cause

        // Constant-time O(1) JVM lookupswitch on enum.
        // Bypasses evaluation of config flags entirely for unmonitored death causes (e.g., ENTITY_ATTACK).
        val match = when (damageCause) {
            DamageCause.WITHER -> Config.enabledWitherRose
            DamageCause.HOT_FLOOR -> Config.enabledHotFloor
            DamageCause.CRAMMING -> Config.enabledCramming
            DamageCause.LAVA -> Config.enabledLava
            DamageCause.FALL -> Config.enabledFall
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

                // Fetch default experience reward using 1.21.11 NMS
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

    private fun isPointedDripstone(event: EntityDamageEvent): Boolean {
        // Event type is already contextually narrowed down by the 'when' block
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
        // Event type is already contextually narrowed down by the 'when' block
        return event is EntityDamageByBlockEvent && event.damager?.type == Material.STONECUTTER
    }
}