package dev.teages.paperPluginEasyShulker

import org.bukkit.plugin.java.JavaPlugin

/**
 * Main plugin class for Easy Shulker functionality.
 * This plugin allows players to open shulker boxes directly from their hand
 * without placing them on the ground.
 */
class PaperPluginEasyShulker : JavaPlugin() {

    // Manager class that handles all shulker box related events and sessions
    private lateinit var shulkerManager: ShulkerManager

    /**
     * Called when the plugin is enabled.
     * Initializes the shulker manager and registers event handlers.
     */
    override fun onEnable() {
        // Initialize the shulker manager to handle shulker box interactions
        shulkerManager = ShulkerManager(this)
        // Register the manager as an event listener to handle player interactions
        server.pluginManager.registerEvents(shulkerManager, this)
    }

    /**
     * Called when the plugin is disabled.
     * Clean up any resources if needed.
     */
    override fun onDisable() {
        // Plugin shutdown logic - currently no cleanup needed
    }
}
