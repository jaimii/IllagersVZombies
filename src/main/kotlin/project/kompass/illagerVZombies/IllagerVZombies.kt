package project.kompass.illagerVZombies

import org.bukkit.plugin.java.JavaPlugin

class IllagerVZombies : JavaPlugin() {

    override fun onEnable() {
        val pluginManager = server.pluginManager

        // Register isolated event listeners
        pluginManager.registerEvents(MobSpawnListener(), this)
        pluginManager.registerEvents(WitchAttackListener(), this)
        pluginManager.registerEvents(SnowmanCreeperListener(this), this)
        pluginManager.registerEvents(FriendlyFireListener(), this)
        pluginManager.registerEvents(IllusionerSpellListener(this), this)

        // Run the Golem attack task every 10 ticks (0.5 seconds)
        GolemAttackTask().runTaskTimer(this, 0L, 10L)

        logger.info("Illager, Piglin and Zombie Behaviours are now modified!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}