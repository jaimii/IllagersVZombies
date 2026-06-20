package project.kompass.illagerVZombies.config

import org.bukkit.configuration.file.FileConfiguration

object Config {
    var enabledWitherRose = true
    var enabledHotFloor = true
    var enabledCramming = true
    var enabledLava = true
    var enabledFall = true
    var enabledPointedDripstone = true
    var enabledStonecutter = true
    var debug = false
    var disabledWorlds = HashSet<String>()

    fun load(config: FileConfiguration) {
        enabledWitherRose = config.getBoolean("enabled_wither_rose", true)
        enabledHotFloor = config.getBoolean("enabled_hot_floor", true)
        enabledCramming = config.getBoolean("enabled_cramming", true)
        enabledLava = config.getBoolean("enabled_lava", true)
        enabledFall = config.getBoolean("enabled_fall", true)
        enabledPointedDripstone = config.getBoolean("enabled_pointed_dripstone", true)
        enabledStonecutter = config.getBoolean("enabled_stonecutter", true)
        debug = config.getBoolean("debug", false)
        disabledWorlds = HashSet(config.getStringList("disabled_worlds"))
    }
}