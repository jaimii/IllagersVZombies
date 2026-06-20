package project.kompass.illagerVZombies

import org.bukkit.Bukkit
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.IronGolem
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Snowman
import org.bukkit.scheduler.BukkitRunnable

class GolemAttackTask : BukkitRunnable() {

    override fun run() {
        for (world in Bukkit.getWorlds()) {
            // 1. Handle Iron Golems
            val ironGolems = world.getEntitiesByClass(IronGolem::class.java)
            for (golem in ironGolems) {
                val currentTarget = golem.target
                if (currentTarget != null && currentTarget !is Creeper) {
                    continue
                }

                // Query nearby entities exactly once to conserve server performance
                val nearbyEntities = golem.getNearbyEntities(10.0, 10.0, 10.0)

                // If other threat types (Zombies, Skeletons, Raiders, etc.) are nearby,
                // do NOT force the Golem to target a Creeper. Let vanilla defense goals take priority.
                val hasOtherThreats = nearbyEntities.any { it is Monster && it !is Creeper }
                if (hasOtherThreats) {
                    continue
                }

                // If no other threat types are present, find and target the nearest Creeper
                val nearestCreeper = findNearestCreeper(golem, nearbyEntities)
                if (nearestCreeper != null) {
                    golem.target = nearestCreeper
                }
            }

            // 2. Handle Snow Golems (Snowmen)
            val snowGolems = world.getEntitiesByClass(Snowman::class.java)
            for (golem in snowGolems) {
                // Keep target if it is already fighting a non-Creeper
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