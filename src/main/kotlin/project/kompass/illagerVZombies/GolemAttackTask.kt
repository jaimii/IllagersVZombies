package project.kompass.illagerVZombies

import org.bukkit.Bukkit
import org.bukkit.entity.Creeper
import org.bukkit.entity.IronGolem
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Snowman
import org.bukkit.scheduler.BukkitRunnable

class GolemAttackTask : BukkitRunnable() {

    override fun run() {
        for (world in Bukkit.getWorlds()) {
            // 1. Handle Iron Golems
            val ironGolems = world.getEntitiesByClass(IronGolem::class.java)
            for (golem in ironGolems) {
                val nearestCreeper = findNearestCreeper(golem, 10.0)
                if (nearestCreeper != null) {
                    golem.target = nearestCreeper
                }
            }

            // 2. Handle Snow Golems (Snowmen)
            val snowGolems = world.getEntitiesByClass(Snowman::class.java)
            for (golem in snowGolems) {
                // Snowmen have a longer default target distance of 15 blocks
                val nearestCreeper = findNearestCreeper(golem, 15.0)
                if (nearestCreeper != null) {
                    golem.target = nearestCreeper
                }
            }
        }
    }

    private fun findNearestCreeper(golem: LivingEntity, radius: Double): Creeper? {
        var nearestCreeper: Creeper? = null
        var nearestDistanceSq = Double.MAX_VALUE

        // Scan nearby entities
        val nearbyEntities = golem.getNearbyEntities(radius, radius, radius)
        for (nearby in nearbyEntities) {
            if (nearby is Creeper) {
                // Optimally uses distanceSquared to skip costly square root math operations
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