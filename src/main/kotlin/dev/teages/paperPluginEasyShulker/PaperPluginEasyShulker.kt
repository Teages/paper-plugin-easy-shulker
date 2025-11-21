package dev.teages.paperPluginEasyShulker

import org.bukkit.plugin.java.JavaPlugin

class PaperPluginEasyShulker : JavaPlugin() {

    private lateinit var shulkerManager: ShulkerManager

    override fun onEnable() {
        // Plugin startup logic
        shulkerManager = ShulkerManager(this)
        server.pluginManager.registerEvents(shulkerManager, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
