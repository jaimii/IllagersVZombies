package project.kompass.illagerVZombies.task

import org.bukkit.Bukkit
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Snowman
import org.bukkit.scheduler.BukkitRunnable

class GolemAttackTask : BukkitRunnable() {

    override fun run() {
        for (world in Bukkit.getWorlds()) {
            // Handle Snow Golems (Snowmen)
            val snowGolems = world.getEntitiesByClass(Snowman::class.java)
            for (golem in snowGolems) {
                val currentTarget = golem.target
                if (currentTarget != null && currentTarget !is Creeper) {
                    continue
                }

                val nearbyEntities = golem.getNearbyEntities(15.0, 15.0, 15.0)
                val nearestCreeper = findNearestCreeper(golem, nearbyEntities)
                if (nearestCreeper != null) {
                    golem.target = nearestCreeper
                }
            }
        }
    }

    private fun findNearestCreeper(golem: LivingEntity, nearbyEntities: List<Entity>): Creeper? {
        var nearestCreeper: Creeper? = null
        var nearestDistanceSq = Double.MAX_VALUE

        for (nearby in nearbyEntities) {
            if (nearby is Creeper) {
                val distanceSq = golem.location.distanceSquared(nearby.location)
                if (distanceSq < nearestDistanceSq) {
                    nearestDistanceSq = distanceSq
                    nearestCreeper = nearby
                }
            }
        }
        return nearestCreeper
    }
}