package project.kompass.illagerVZombies

import org.bukkit.plugin.java.JavaPlugin
import project.kompass.illagerVZombies.config.Config
import project.kompass.illagerVZombies.listener.FriendlyFireListener
import project.kompass.illagerVZombies.listener.IllusionerSpellListener
import project.kompass.illagerVZombies.listener.MobDeathXPListener
import project.kompass.illagerVZombies.listener.MobSpawnListener
import project.kompass.illagerVZombies.listener.SnowmanCreeperListener
import project.kompass.illagerVZombies.listener.WitchAttackListener
import project.kompass.illagerVZombies.task.GolemAttackTask

class IllagerVZombies : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        Config.load(config)

        val pluginManager = server.pluginManager

        pluginManager.registerEvents(MobSpawnListener(), this)
        pluginManager.registerEvents(WitchAttackListener(), this)
        pluginManager.registerEvents(SnowmanCreeperListener(this), this)
        pluginManager.registerEvents(FriendlyFireListener(), this)
        pluginManager.registerEvents(IllusionerSpellListener(this), this)
        pluginManager.registerEvents(MobDeathXPListener(this), this)

        GolemAttackTask().runTaskTimer(this, 0L, 10L)

        logger.info("Illager, Piglin and Zombie Behaviours are now modified!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}