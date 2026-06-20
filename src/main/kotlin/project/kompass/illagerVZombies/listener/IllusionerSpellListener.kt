package project.kompass.illagerVZombies.listener

import org.bukkit.Bukkit
import org.bukkit.entity.Illusioner
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.plugin.Plugin

class IllusionerSpellListener(private val plugin: Plugin) : Listener {

    @EventHandler
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        val player = event.entity
        if (player is Player) {
            if (event.modifiedType == PotionEffectType.BLINDNESS) {
                if (event.action == EntityPotionEffectEvent.Action.ADDED ||
                    event.action == EntityPotionEffectEvent.Action.CHANGED) {

                    val newEffect = event.newEffect
                    if (newEffect != null && newEffect.duration == 400 &&
                        event.cause == EntityPotionEffectEvent.Cause.ATTACK) {

                        val nearbyIllusioner = player.getNearbyEntities(30.0, 30.0, 30.0).stream()
                            .anyMatch { entity -> entity is Illusioner }

                        if (nearbyIllusioner) {
                            event.isCancelled = true

                            player.addPotionEffect(
                                PotionEffect(
                                    PotionEffectType.BLINDNESS,
                                    60,
                                    0,
                                    false,
                                    true,
                                    true
                                )
                            )

                            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                                if (player.isOnline && !player.isDead) {
                                    player.addPotionEffect(
                                        PotionEffect(
                                            PotionEffectType.DARKNESS,
                                            200,
                                            0,
                                            false,
                                            true,
                                            true
                                        )
                                    )
                                }
                            }, 60L)
                        }
                    }
                }
            }
        }
    }
}