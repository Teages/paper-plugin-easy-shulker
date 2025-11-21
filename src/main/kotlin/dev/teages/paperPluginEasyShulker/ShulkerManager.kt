package dev.teages.paperPluginEasyShulker

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

/**
 * Manager class that handles all shulker box related events and manages active sessions.
 * This class is responsible for detecting when players interact with shulker boxes
 * and opening virtual inventories for them.
 */
class ShulkerManager(private val plugin: Plugin) : Listener {
    
    // Map to track active shulker box sessions for each player
    private val activeSessions = mutableMapOf<Player, ShulkerSession>()

    /**
     * Handles player interaction events to detect right-click on shulker boxes.
     * Opens a virtual shulker box inventory when a player right-clicks a shulker box
     * in the air while not sneaking.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun handlePlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action
        val item = event.item ?: return
        
        // Check if player right-clicked air with a shulker box while not sneaking
        if (
            action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
            !player.isSneaking &&
            ShulkerUtils.isShulkerBox(item)
        ) {
            // Cancel the default behavior and open our custom shulker interface
            event.isCancelled = true
            openShulkerBox(player, item)
        }
    }
    
    /**
     * Handles inventory click events within shulker box sessions.
     * Prevents certain invalid interactions and saves changes to the shulker box.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = activeSessions[player] ?: return
        val shulkerItem = session.getShulkerBox()

        // Check if this interaction should be cancelled (e.g., trying to place shulker boxes inside shulker boxes)
        if (shouldCancelShulkerInteraction(event, shulkerItem, player)) {
            event.isCancelled = true
            return
        }

        // Let the session handle the click event
        session.handleInventoryClick(event)
        // If the event wasn't cancelled, save the inventory state asynchronously
        if (!event.isCancelled) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                session.save(event.view.topInventory)
            })
        }
    }

    /**
     * Determines if a shulker interaction should be cancelled.
     * Prevents players from placing shulker boxes inside other shulker boxes
     * or performing other invalid operations.
     */
    private fun shouldCancelShulkerInteraction(event: InventoryClickEvent, shulkerItem: ItemStack, player: Player): Boolean {
        val currentItem = event.currentItem
        val cursor = event.cursor

        // Check number key press (hotbar swap)
        if (event.click == ClickType.NUMBER_KEY) {
            val hotIndex = event.hotbarButton
            if (hotIndex >= 0) {
                val hotItem = player.inventory.getItem(hotIndex)
                if (ShulkerUtils.isSimilarShulkerBox(hotItem, shulkerItem) ||
                    ShulkerUtils.isSimilarShulkerBox(currentItem, shulkerItem)) {
                    return true
                }
            }
        }

        // Check offhand swap
        if (event.click == ClickType.SWAP_OFFHAND) {
            if (ShulkerUtils.isSimilarShulkerBox(player.inventory.itemInOffHand, shulkerItem) ||
                ShulkerUtils.isSimilarShulkerBox(currentItem, shulkerItem)) {
                return true
            }
        }

        // Check collect to cursor action
        if (event.action == InventoryAction.COLLECT_TO_CURSOR) {
            if (ShulkerUtils.isSimilarShulkerBox(cursor, shulkerItem)) {
                return true
            }
        }

        // General check for shulker box interactions
        if (ShulkerUtils.isSimilarShulkerBox(currentItem, shulkerItem) ||
            ShulkerUtils.isSimilarShulkerBox(cursor, shulkerItem)) {
            return true
        }

        // Check drop actions
        if (event.click == ClickType.DROP || event.click == ClickType.CONTROL_DROP) {
            if (ShulkerUtils.isSimilarShulkerBox(currentItem, shulkerItem) ||
                ShulkerUtils.isSimilarShulkerBox(cursor, shulkerItem)) {
                return true
            }
        }

        return false
    }

    /**
     * Handles inventory drag events within shulker box sessions.
     * Processes drag operations and saves changes to the shulker box.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun handleInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = activeSessions[player] ?: return
        
        // Let the session handle the drag event
        session.handleInventoryDrag(event)
        // If the event wasn't cancelled, save the inventory state asynchronously
        if (!event.isCancelled) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                session.save(event.view.topInventory)
            })
        }
    }

    /**
     * Handles player drop item events.
     * Prevents players from dropping the shulker box they are currently viewing.
     */
    @EventHandler(priority = EventPriority.HIGH)
    fun handlePlayerDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val session = activeSessions[player] ?: return
        val dropped = event.itemDrop.itemStack
        val shulker = session.getShulkerBox()
        
        // Cancel if trying to drop the shulker box being viewed
        if (ShulkerUtils.isSimilarShulkerBox(dropped, shulker)) {
            event.isCancelled = true
        }
    }
    
    /**
     * Handles inventory close events.
     * Saves the shulker box state and removes the session when a player closes the inventory.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun handleInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val session = activeSessions.remove(player) ?: return
        // Save the final state of the shulker box
        session.save(event.inventory)
    }
    
    /**
     * Opens a virtual shulker box inventory for the specified player.
     * Creates a new session and opens the shulker box interface.
     */
    fun openShulkerBox(player: Player, shulkerBox: ItemStack) {
        val session = ShulkerSession(player, shulkerBox)
        activeSessions[player] = session
        session.open()
    }

    /**
     * Handles player quit events.
     * Saves the shulker box state and removes the session when a player leaves the server.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun handlePlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val session = activeSessions.remove(player) ?: return
        // Save the current state before the player disconnects
        session.save(player.openInventory.topInventory)
    }

    /**
     * Handles player death events.
     * Saves the shulker box state and removes the session when a player dies.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    fun handlePlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val session = activeSessions.remove(player) ?: return
        // Save the current state before the player respawns
        session.save(player.openInventory.topInventory)
    }
    

}
