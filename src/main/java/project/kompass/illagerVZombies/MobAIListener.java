package project.kompass.illagerVZombies;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.craftbukkit.entity.CraftPiglin;
import org.bukkit.craftbukkit.entity.CraftPiglinBrute;
import org.bukkit.craftbukkit.entity.CraftGiant;
import org.bukkit.craftbukkit.entity.CraftRaider;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.craftbukkit.entity.CraftWitch;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.craftbukkit.entity.CraftCreeper;

import org.bukkit.potion.PotionEffectType;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;

import org.bukkit.entity.IronGolem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobAIListener implements Listener {

    private final java.util.Map<java.util.UUID, Integer> snowmanHits = new java.util.HashMap<>();
    private final java.util.Map<java.util.UUID, java.util.UUID> creeperTargets = new java.util.HashMap<>();

    private final org.bukkit.plugin.Plugin plugin;
    private final NamespacedKey snowmanShooterKey;

    public MobAIListener() {
        this.plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(MobAIListener.class);
        this.snowmanShooterKey = new NamespacedKey(this.plugin, "snowman_shooter");
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        // 1. Handle ALL RAIDERS
        if (event.getEntity() instanceof org.bukkit.entity.Raider bRaider) {
            Raider nmsRaider = ((CraftRaider) bRaider).getHandle();

            // FEAR GOALS
            nmsRaider.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsRaider, Giant.class, 24.0F, 1.0D, 1.2D));
            nmsRaider.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsRaider, Warden.class, 16.0F, 1.0D, 1.2D));

            // ATTACK GOALS
            nmsRaider.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsRaider, Zombie.class, true));
            nmsRaider.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsRaider, Piglin.class, true));
            nmsRaider.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsRaider, PiglinBrute.class, true));
        }

        // 2. Handle WITCHES
        else if (event.getEntity() instanceof org.bukkit.entity.Witch bWitch) {
            Witch nmsWitch = ((CraftWitch) bWitch).getHandle();

            // CLEAR ALL default AI
            nmsWitch.goalSelector.removeAllGoals(goal -> true);
            nmsWitch.targetSelector.removeAllGoals(goal -> true);

            // IDLE GOALS
            nmsWitch.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(nmsWitch));
            nmsWitch.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(nmsWitch, 1.0D));

            // CUSTOM RANGED ATTACK GOAL
            nmsWitch.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.RangedAttackGoal(nmsWitch, 1.0D, 15, 12.0F));

            // TARGET SELECTION
            nmsWitch.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(nmsWitch, net.minecraft.world.entity.monster.zombie.Zombie.class, true));
            nmsWitch.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(nmsWitch, net.minecraft.world.entity.monster.piglin.Piglin.class, true));
        }

        // 3. Handle ZOMBIES
        else if (event.getEntity() instanceof org.bukkit.entity.Zombie bZombie) {
            Zombie nmsZombie = ((CraftZombie) bZombie).getHandle();

            // ATTACK GOALS
            nmsZombie.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsZombie, Raider.class, true));
            nmsZombie.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(nmsZombie, Witch.class, true));
        }

        // 4. Handle GIANTS
        else if (event.getEntity() instanceof org.bukkit.entity.Giant bGiant) {
            Giant nmsGiant = ((CraftGiant) bGiant).getHandle();

            // ATTACK GOALS
            nmsGiant.goalSelector.addGoal(0, new MeleeAttackGoal(nmsGiant, 1.0D, true));
            nmsGiant.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsGiant, Raider.class, true));
            nmsGiant.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(nmsGiant, Witch.class, true));
            nmsGiant.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(nmsGiant, Piglin.class, true));
            nmsGiant.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(nmsGiant, PiglinBrute.class, true));

            if (nmsGiant.getAttribute(Attributes.FOLLOW_RANGE) != null) {
                nmsGiant.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(48.0);
            }
        }
        // 5. Handle PIGLINS
        else if (event.getEntity() instanceof org.bukkit.entity.Piglin bPiglin) {
            bPiglin.setImmuneToZombification(true);
            Piglin nmsPiglin = ((CraftPiglin) bPiglin).getHandle();

            // CLEAR BRAIN MEMORIES
            nmsPiglin.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

            // FEAR GOALS
            nmsPiglin.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsPiglin, Giant.class, 24.0F, 1.0D, 1.2D));
            nmsPiglin.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsPiglin, Warden.class, 16.0F, 1.0D, 1.2D));
            nmsPiglin.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsPiglin, Zombie.class, 16.0F, 1.0D, 1.2D));

            // ATTACK GOALS
            nmsPiglin.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsPiglin, Raider.class, true));
            nmsPiglin.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsPiglin, Witch.class, true));

            nmsPiglin.goalSelector.addGoal(2, new MeleeAttackGoal(nmsPiglin, 1.0D, true));
        }

        // 6. Handle PIGLIN BRUTES
        else if (event.getEntity() instanceof org.bukkit.entity.PiglinBrute bBrute) {
            bBrute.setImmuneToZombification(true);
            PiglinBrute nmsBrute = ((CraftPiglinBrute) bBrute).getHandle();

            // FEAR GOALS
            nmsBrute.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsBrute, Giant.class, 24.0F, 1.0D, 1.2D));
            nmsBrute.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsBrute, Warden.class, 16.0F, 1.0D, 1.2D));
            nmsBrute.goalSelector.addGoal(1, new AvoidEntityGoal<>(nmsBrute, Zombie.class, 16.0F, 1.0D, 1.2D));

            // ATTACK GOALS
            nmsBrute.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsBrute, Raider.class, true));
            nmsBrute.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nmsBrute, Witch.class, true));

            nmsBrute.goalSelector.addGoal(2, new MeleeAttackGoal(nmsBrute, 1.0D, true));
        }

        // 7. Handle IRON GOLEMS
        else if (event.getEntity() instanceof org.bukkit.entity.IronGolem bGolem) {
            net.minecraft.world.entity.animal.golem.IronGolem nmsGolem = ((CraftIronGolem) bGolem).getHandle();

            // Register targeting Creeper directly to the targetSelector.
            nmsGolem.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(
                    nmsGolem,
                    net.minecraft.world.entity.monster.Creeper.class,
                    true
            ));
        }

        // 8. Handle CREEPERS
        else if (event.getEntity() instanceof org.bukkit.entity.Creeper bCreeper) {
            net.minecraft.world.entity.monster.Creeper nmsCreeper = ((CraftCreeper) bCreeper).getHandle();

            // Instruct creepers to flee from Iron Golems
            nmsCreeper.goalSelector.addGoal(1, new AvoidEntityGoal<>(
                    nmsCreeper,
                    net.minecraft.world.entity.animal.golem.IronGolem.class,
                    16.0F,
                    1.0D,
                    1.2D
            ));
        }
    }

    // 9. Special WITCH attack On Zombies
    @EventHandler
    public void onWitchThrow(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.ThrownPotion potion &&
                event.getEntity().getShooter() instanceof org.bukkit.entity.Witch witch) {

            org.bukkit.entity.LivingEntity target = witch.getTarget();

            if (target instanceof org.bukkit.entity.Zombie) {
                org.bukkit.inventory.ItemStack healthPotion = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SPLASH_POTION);
                org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) healthPotion.getItemMeta();

                meta.setBasePotionType(org.bukkit.potion.PotionType.STRONG_HEALING);
                meta.addCustomEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 1), true);

                healthPotion.setItemMeta(meta);
                potion.setItem(healthPotion);
            }
        }
    }

    // 10. Snowman snowball launch
    @EventHandler(priority = EventPriority.LOWEST)
    public void onSnowballLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Snowball snowball && snowball.getShooter() instanceof Snowman snowman) {
            snowball.getPersistentDataContainer().set(
                    snowmanShooterKey,
                    PersistentDataType.STRING,
                    snowman.getUniqueId().toString()
            );
        }
    }

    // 11. Creeper hit by snowball
    @EventHandler
    public void onCreeperHitBySnowball(ProjectileHitEvent event) {
        if (event.getHitEntity() instanceof Creeper creeper && event.getEntity() instanceof Snowball snowball) {
            Snowman snowman = findAttackingSnowman(creeper, snowball);

            if (snowman != null) {
                java.util.UUID creeperId = creeper.getUniqueId();
                int hits = snowmanHits.getOrDefault(creeperId, 0) + 1;

                if (hits >= 3) {
                    snowmanHits.remove(creeperId);
                    creeperTargets.put(creeperId, snowman.getUniqueId());
                    creeper.setTarget(snowman);
                } else {
                    snowmanHits.put(creeperId, hits);
                }
            }
        }
    }

    private Snowman findAttackingSnowman(Creeper creeper, Snowball snowball) {
        if (snowball.getPersistentDataContainer().has(snowmanShooterKey, PersistentDataType.STRING)) {
            String uuidStr = snowball.getPersistentDataContainer().get(snowmanShooterKey, PersistentDataType.STRING);
            if (uuidStr != null) {
                try {
                    java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
                    org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(uuid);
                    if (entity instanceof Snowman snowman && snowman.isValid()) {
                        return snowman;
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (snowball.getShooter() instanceof Snowman snowman && snowman.isValid()) {
            return snowman;
        }

        double closestDistSq = Double.MAX_VALUE;
        Snowman closestSnowman = null;
        for (org.bukkit.entity.Entity nearby : creeper.getNearbyEntities(30.0, 30.0, 30.0)) {
            if (nearby instanceof Snowman snowman && snowman.isValid()) {
                if (snowman.getTarget() != null && snowman.getTarget().getUniqueId().equals(creeper.getUniqueId())) {
                    return snowman;
                }
                double distSq = creeper.getLocation().distanceSquared(snowman.getLocation());
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestSnowman = snowman;
                }
            }
        }
        return closestSnowman;
    }

    // 12. Creeper target validation
    @EventHandler
    public void onCreeperTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Creeper creeper) {
            if (event.getTarget() instanceof IronGolem) {
                event.setCancelled(true);
                return;
            }

            java.util.UUID creeperId = creeper.getUniqueId();
            if (creeperTargets.containsKey(creeperId)) {
                java.util.UUID snowmanId = creeperTargets.get(creeperId);
                org.bukkit.entity.Entity snowman = org.bukkit.Bukkit.getEntity(snowmanId);

                if (snowman != null && snowman.isValid() && !snowman.isDead()) {
                    if (creeper.getLocation().distanceSquared(snowman.getLocation()) <= 1024.0) {
                        if (event.getTarget() == null || !event.getTarget().getUniqueId().equals(snowmanId)) {
                            event.setTarget(snowman);
                        }
                    } else {
                        creeperTargets.remove(creeperId);
                    }
                } else {
                    creeperTargets.remove(creeperId);
                }
            }
        }
    }

    // --- IMPLEMENTATION OF SKELETON & ILLAGER FRIENDLY-FIRE / PASS-THROUGH ---

    // 13. Prevent friendly fire damage (melee or projectiles) between Skeletons, and between Illusioners and Pillagers
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        org.bukkit.entity.Entity damager = event.getDamager();
        org.bukkit.entity.Entity damaged = event.getEntity();

        // Trace projectiles back to their source
        if (damager instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof org.bukkit.entity.Entity shooter) {
                damager = shooter;
            }
        }

        // Skeletons should never damage each other (includes Bogged, Wither Skeletons, Stray, Parched)
        if (damager instanceof org.bukkit.entity.AbstractSkeleton && damaged instanceof org.bukkit.entity.AbstractSkeleton) {
            event.setCancelled(true);
            return;
        }

        // Illusioners and Pillagers should never damage each other
        if (isIllusionerOrPillager(damager) && isIllusionerOrPillager(damaged)) {
            event.setCancelled(true);
            return;
        }
    }

    // 14. Block intentional target selection (preventing initial aggro or revenge tracking)
    @EventHandler
    public void onEntityTargetFriendly(EntityTargetEvent event) {
        if (event.getTarget() == null) return;

        org.bukkit.entity.Entity entity = event.getEntity();
        org.bukkit.entity.Entity target = event.getTarget();

        // Skeletons should never target each other
        if (entity instanceof org.bukkit.entity.AbstractSkeleton && target instanceof org.bukkit.entity.AbstractSkeleton) {
            event.setCancelled(true);
            return;
        }

        // Illusioners and Pillagers should never target each other
        if (isIllusionerOrPillager(entity) && isIllusionerOrPillager(target)) {
            event.setCancelled(true);
            return;
        }
    }

    // 15. Handle Projectile pass-through on collision
    @EventHandler
    public void onProjectileHitFriendly(ProjectileHitEvent event) {
        if (event.getHitEntity() == null) return;

        org.bukkit.entity.Projectile projectile = event.getEntity();
        org.bukkit.projectiles.ProjectileSource shooter = projectile.getShooter();

        if (!(shooter instanceof org.bukkit.entity.Entity shooterEntity)) return;
        org.bukkit.entity.Entity hitEntity = event.getHitEntity();

        // Skeletons' projectiles pass-through other Skeletons
        if (shooterEntity instanceof org.bukkit.entity.AbstractSkeleton && hitEntity instanceof org.bukkit.entity.AbstractSkeleton) {
            event.setCancelled(true);
            return;
        }

        // Illusioner and Pillager projectiles pass-through all raiders (including Witches)
        if (isIllusionerOrPillager(shooterEntity) && isRaiderOrWitch(hitEntity)) {
            event.setCancelled(true);
            return;
        }
    }

    // Helper to identify Illusioners and Pillagers
    private boolean isIllusionerOrPillager(org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.Illusioner || entity instanceof org.bukkit.entity.Pillager;
    }

    // Helper to identify Raiders and Witches
    private boolean isRaiderOrWitch(org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.Raider || entity instanceof org.bukkit.entity.Witch;
    }

    // DEATH CLEANUP
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        java.util.UUID uuid = event.getEntity().getUniqueId();
        snowmanHits.remove(uuid);
        creeperTargets.remove(uuid);
        creeperTargets.values().removeIf(targetId -> targetId.equals(uuid));
    }
}