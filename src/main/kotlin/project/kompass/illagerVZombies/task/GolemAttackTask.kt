package project.kompass.illagerVZombies.task

import org.bukkit.Bukkit
import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.IronGolem
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Pillager
import org.bukkit.entity.Raider
import org.bukkit.entity.Snowman
import org.bukkit.entity.Zombie
import org.bukkit.scheduler.BukkitRunnable

class GolemAttackTask : BukkitRunnable() {

    override fun run() {
        for (world in Bukkit.getWorlds()) {
            // 1. Handle Iron Golems
            val ironGolems = world.getEntitiesByClass(IronGolem::class.java)
            for (golem in ironGolems) {
                val currentTarget = golem.target

                // If already targeting a high-priority threat (Zombie/Pillager/Raider), let it finish
                if (currentTarget != null && isHighPriority(currentTarget)) {
                    continue
                }

                // Query nearby entities exactly once per Golem
                val nearbyEntities = golem.getNearbyEntities(10.0, 10.0, 10.0)

                // Zombies, Pillagers, and Raiders must always take absolute priority.
                val hasHighPriorityThreat = nearbyEntities.any { isHighPriority(it) }
                if (hasHighPriorityThreat) {
                    // If currently targeted on a Creeper, clear it so vanilla target selectors can hit the high-priority threat
                    if (currentTarget is Creeper) {
                        golem.target = null
                    }
                    continue
                }

                // Performance Optimization: Since hasHighPriorityThreat is FALSE, we are guaranteed
                // that NONE of the entities in nearbyEntities are Zombies, Pillagers, or Raiders.
                // We can bypass redundant priority checks. Any 'Monster' is a standard threat.
                var closestStandardThreat: LivingEntity? = null
                var closestDistanceSq = Double.MAX_VALUE

                for (nearby in nearbyEntities) {
                    if (nearby is Monster) {
                        val distSq = golem.location.distanceSquared(nearby.location)
                        if (distSq < closestDistanceSq) {
                            closestDistanceSq = distSq
                            closestStandardThreat = nearby
                        }
                    }
                }

                // Equal Opportunity targeting
                if (closestStandardThreat != null) {
                    if (closestStandardThreat is Creeper) {
                        // Force target the Creeper since Golems do not naturally target them
                        golem.target = closestStandardThreat
                    } else {
                        // If currently focused on a Creeper but a Skeleton is now closer, switch targets
                        if (currentTarget is Creeper) {
                            golem.target = closestStandardThreat
                        }
                    }
                }
            }

            // 2. Handle Snow Golems (Snowmen)
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

    private fun isHighPriority(entity: Entity): Boolean {
        return entity is Zombie || entity is Pillager || entity is Raider
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