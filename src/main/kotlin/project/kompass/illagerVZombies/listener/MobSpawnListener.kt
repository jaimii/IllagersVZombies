package project.kompass.illagerVZombies.listener

import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RangedAttackGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.memory.MemoryModuleType
import net.minecraft.world.entity.monster.Giant as NmsGiant
import net.minecraft.world.entity.monster.Witch as NmsWitch
import net.minecraft.world.entity.monster.warden.Warden
import net.minecraft.world.entity.monster.zombie.Zombie as NmsZombie
import net.minecraft.world.entity.raid.Raider as NmsRaider
import net.minecraft.world.entity.monster.piglin.Piglin as NmsPiglin
import net.minecraft.world.entity.monster.piglin.PiglinBrute as NmsPiglinBrute
import net.minecraft.world.entity.animal.golem.IronGolem as NmsIronGolem

import org.bukkit.craftbukkit.entity.CraftCreeper
import org.bukkit.craftbukkit.entity.CraftGiant
import org.bukkit.craftbukkit.entity.CraftPiglin
import org.bukkit.craftbukkit.entity.CraftPiglinBrute
import org.bukkit.craftbukkit.entity.CraftRaider
import org.bukkit.craftbukkit.entity.CraftWitch
import org.bukkit.craftbukkit.entity.CraftZombie

import org.bukkit.entity.Creeper as BukkitCreeper
import org.bukkit.entity.Giant as BukkitGiant
import org.bukkit.entity.Piglin as BukkitPiglin
import org.bukkit.entity.PiglinBrute as BukkitPiglinBrute
import org.bukkit.entity.Raider as BukkitRaider
import org.bukkit.entity.Witch as BukkitWitch
import org.bukkit.entity.Zombie as BukkitZombie

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntitySpawnEvent

class MobSpawnListener : Listener {

    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent) {
        val entity = event.entity

        // 1. Handle ALL RAIDERS
        if (entity is BukkitRaider) {
            val nmsRaider = (entity as CraftRaider).handle

            // FEAR GOALS
            nmsRaider.goalSelector.addGoal(1, AvoidEntityGoal(nmsRaider, NmsGiant::class.java, 24.0f, 1.0, 1.2))
            nmsRaider.goalSelector.addGoal(1, AvoidEntityGoal(nmsRaider, Warden::class.java, 16.0f, 1.0, 1.2))

            // ATTACK GOALS
            nmsRaider.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsRaider, NmsZombie::class.java, true))
            nmsRaider.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsRaider, NmsPiglin::class.java, true))
            nmsRaider.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsRaider, NmsPiglinBrute::class.java, true))
        }

        // 2. Handle WITCHES
        else if (entity is BukkitWitch) {
            val nmsWitch = (entity as CraftWitch).handle

            // CLEAR ALL default AI
            nmsWitch.goalSelector.removeAllGoals { true }
            nmsWitch.targetSelector.removeAllGoals { true }

            // IDLE GOALS
            nmsWitch.goalSelector.addGoal(0, FloatGoal(nmsWitch))
            nmsWitch.goalSelector.addGoal(3, WaterAvoidingRandomStrollGoal(nmsWitch, 1.0))

            // CUSTOM RANGED ATTACK GOAL
            nmsWitch.goalSelector.addGoal(2, RangedAttackGoal(nmsWitch, 1.0, 15, 12.0f))

            // TARGET SELECTION
            nmsWitch.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsWitch, NmsZombie::class.java, true))
            nmsWitch.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsWitch, NmsPiglin::class.java, true))
        }

        // 3. Handle ZOMBIES
        else if (entity is BukkitZombie) {
            val nmsZombie = (entity as CraftZombie).handle

            // ATTACK GOALS
            nmsZombie.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsZombie, NmsRaider::class.java, true))
            nmsZombie.targetSelector.addGoal(3, NearestAttackableTargetGoal(nmsZombie, NmsWitch::class.java, true))
        }

        // 4. Handle GIANTS
        else if (entity is BukkitGiant) {
            val nmsGiant = (entity as CraftGiant).handle

            // ATTACK GOALS
            nmsGiant.goalSelector.addGoal(0, MeleeAttackGoal(nmsGiant, 1.0, true))
            nmsGiant.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsGiant, NmsRaider::class.java, true))
            nmsGiant.targetSelector.addGoal(2, NearestAttackableTargetGoal(nmsGiant, NmsWitch::class.java, true))
            nmsGiant.targetSelector.addGoal(3, NearestAttackableTargetGoal(nmsGiant, NmsPiglin::class.java, true))
            nmsGiant.targetSelector.addGoal(4, NearestAttackableTargetGoal(nmsGiant, NmsPiglinBrute::class.java, true))

            nmsGiant.getAttribute(Attributes.FOLLOW_RANGE)?.let { attribute ->
                attribute.baseValue = 48.0
            }
        }

        // 5. Handle PIGLINS
        else if (entity is BukkitPiglin) {
            entity.isImmuneToZombification = true
            val nmsPiglin = (entity as CraftPiglin).handle

            // CLEAR BRAIN MEMORIES
            nmsPiglin.brain.eraseMemory(MemoryModuleType.ATTACK_TARGET)

            // FEAR GOALS
            nmsPiglin.goalSelector.addGoal(1, AvoidEntityGoal(nmsPiglin, NmsGiant::class.java, 24.0f, 1.0, 1.2))
            nmsPiglin.goalSelector.addGoal(1, AvoidEntityGoal(nmsPiglin, Warden::class.java, 16.0f, 1.0, 1.2))
            nmsPiglin.goalSelector.addGoal(1, AvoidEntityGoal(nmsPiglin, NmsZombie::class.java, 16.0f, 1.0, 1.2))

            // ATTACK GOALS
            nmsPiglin.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsPiglin, NmsRaider::class.java, true))
            nmsPiglin.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsPiglin, NmsWitch::class.java, true))

            nmsPiglin.goalSelector.addGoal(2, MeleeAttackGoal(nmsPiglin, 1.0, true))
        }

        // 6. Handle PIGLIN BRUTES
        else if (entity is BukkitPiglinBrute) {
            entity.isImmuneToZombification = true
            val nmsBrute = (entity as CraftPiglinBrute).handle

            // FEAR GOALS
            nmsBrute.goalSelector.addGoal(1, AvoidEntityGoal(nmsBrute, NmsGiant::class.java, 24.0f, 1.0, 1.2))
            nmsBrute.goalSelector.addGoal(1, AvoidEntityGoal(nmsBrute, Warden::class.java, 16.0f, 1.0, 1.2))
            nmsBrute.goalSelector.addGoal(1, AvoidEntityGoal(nmsBrute, NmsZombie::class.java, 16.0f, 1.0, 1.2))

            // ATTACK GOALS
            nmsBrute.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsBrute, NmsRaider::class.java, true))
            nmsBrute.targetSelector.addGoal(1, NearestAttackableTargetGoal(nmsBrute, NmsWitch::class.java, true))

            nmsBrute.goalSelector.addGoal(2, MeleeAttackGoal(nmsBrute, 1.0, true))
        }

        // 7. Handle CREEPERS
        else if (entity is BukkitCreeper) {
            val nmsCreeper = (entity as CraftCreeper).handle

            // Instruct creepers to flee from Iron Golems
            nmsCreeper.goalSelector.addGoal(1, AvoidEntityGoal(
                nmsCreeper,
                NmsIronGolem::class.java,
                16.0f,
                1.0,
                1.2
            ))
        }
    }
}