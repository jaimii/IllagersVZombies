package project.kompass.illagerVZombies.listener

import org.bukkit.entity.AbstractSkeleton
import org.bukkit.entity.Entity
import org.bukkit.entity.Illusioner
import org.bukkit.entity.Pillager
import org.bukkit.entity.Projectile
import org.bukkit.entity.Raider
import org.bukkit.entity.Witch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.ProjectileHitEvent

class FriendlyFireListener : Listener {

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        var damager: Entity = event.damager
        val damaged: Entity = event.entity

        if (damager is Projectile) {
            val shooter = damager.shooter
            if (shooter is Entity) {
                damager = shooter
            }
        }

        if (damager is AbstractSkeleton && damaged is AbstractSkeleton) {
            event.isCancelled = true
            return
        }

        if (isIllusionerOrPillager(damager) && isIllusionerOrPillager(damaged)) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onEntityTargetFriendly(event: EntityTargetEvent) {
        val target = event.target ?: return
        val entity = event.entity

        if (entity is AbstractSkeleton && target is AbstractSkeleton) {
            event.isCancelled = true
            return
        }

        if (isIllusionerOrPillager(entity) && isIllusionerOrPillager(target)) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onProjectileHitFriendly(event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity ?: return
        val projectile = event.entity
        val shooter = projectile.shooter

        if (shooter !is Entity) return

        if (shooter is AbstractSkeleton && hitEntity is AbstractSkeleton) {
            event.isCancelled = true
            return
        }

        if (isIllusionerOrPillager(shooter) && isRaiderOrWitch(hitEntity)) {
            event.isCancelled = true
            return
        }
    }

    private fun isIllusionerOrPillager(entity: Entity): Boolean {
        return entity is Illusioner || entity is Pillager
    }

    private fun isRaiderOrWitch(entity: Entity): Boolean {
        return entity is Raider || entity is Witch
    }
}