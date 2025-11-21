package dev.teages.paperPluginEasyShulker

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.plugin.Plugin
import org.bukkit.inventory.InventoryView

class ShulkerManager(private val plugin: Plugin) : Listener {
    
    private val activeSessions = mutableMapOf<Player, ShulkerSession>()

    @EventHandler(priority = EventPriority.HIGH)
    fun handlePlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val action = event.action
        val item = event.item ?: return
        
        if (
            action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
            !player.isSneaking &&
            isShulkerBox(item)
        ) {
            event.isCancelled = true
            openShulkerBox(player, item)
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    fun handleInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedInventory = event.clickedInventory ?: return
        val currentItem = event.currentItem
        
        val session = activeSessions[player] ?: return
        val shulkerItem = session.getShulkerBox()

        if (event.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY) {
            val hotIndex = event.hotbarButton
            if (hotIndex >= 0) {
                val hotItem = player.inventory.getItem(hotIndex)
                if (hotItem != null && hotItem.isSimilar(shulkerItem)) {
                    event.isCancelled = true
                    return
                }
                if (currentItem != null && currentItem.isSimilar(shulkerItem)) {
                    event.isCancelled = true
                    return
                }
            }
        }

        if (event.click == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND) {
            if (player.inventory.itemInOffHand.isSimilar(shulkerItem)) {
                event.isCancelled = true
                return
            }
            if (currentItem != null && currentItem.isSimilar(shulkerItem)) {
                event.isCancelled = true
                return
            }
        }

        if (event.action == org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR) {
            if (event.cursor.isSimilar(shulkerItem)) {
                event.isCancelled = true
                return
            }
        }

        if (currentItem != null && currentItem.isSimilar(shulkerItem)) {
            event.isCancelled = true
            return
        }
        if (event.cursor.isSimilar(shulkerItem)) {
            event.isCancelled = true
            return
        }

        val clickType = event.click
        if ((clickType == org.bukkit.event.inventory.ClickType.DROP || clickType == org.bukkit.event.inventory.ClickType.CONTROL_DROP)) {
            if (currentItem != null && currentItem.isSimilar(shulkerItem)) {
                event.isCancelled = true
                return
            }
            val cursor = event.cursor
            if (cursor.isSimilar(shulkerItem)) {
                event.isCancelled = true
                return
            }
        }

        session.handleInventoryClick(event)
        if (!event.isCancelled) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                session.save(event.view.topInventory)
            })
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun handleInventoryDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val session = activeSessions[player] ?: return
        
        session.handleInventoryDrag(event)
        if (!event.isCancelled) {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                session.save(event.view.topInventory)
            })
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun handlePlayerDrop(event: PlayerDropItemEvent) {
        val player = event.player
        val session = activeSessions[player] ?: return
        val dropped = event.itemDrop.itemStack
        val shulker = session.getShulkerBox()
        if (dropped.isSimilar(shulker)) {
            event.isCancelled = true
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    fun handleInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val session = activeSessions.remove(player) ?: return
        session.save(event.inventory)
    }
    
    fun openShulkerBox(player: Player, shulkerBox: ItemStack) {
        val session = ShulkerSession(player, shulkerBox)
        activeSessions[player] = session
        session.open()
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handlePlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        val session = activeSessions.remove(player) ?: return
        session.save(player.openInventory.topInventory)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun handlePlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val session = activeSessions.remove(player) ?: return
        session.save(player.openInventory.topInventory)
    }
    
    private fun isShulkerBox(item: ItemStack): Boolean {
        val material = item.type
        return material.name.endsWith("SHULKER_BOX")
    }

}
