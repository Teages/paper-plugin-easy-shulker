package dev.teages.paperPluginEasyShulker

import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.meta.BlockStateMeta

class ShulkerSession(
    private val player: Player,
    private val shulkerBox: ItemStack,
) {
    
    fun getPlayer(): Player = player
    
    fun getShulkerBox(): ItemStack = shulkerBox

    fun open() {
        val meta = shulkerBox.itemMeta as? BlockStateMeta ?: return
        val shulker = meta.blockState as? ShulkerBox ?: return

        val view = MenuType.SHULKER_BOX.builder()
            .title(meta.displayName())
            .build(player)
        
        view.topInventory.contents = shulker.inventory.contents
        view.open()
    }

    fun save(inventory: Inventory) {
        val meta = shulkerBox.itemMeta as? BlockStateMeta ?: return
        val shulker = meta.blockState as? ShulkerBox ?: return
        
        shulker.inventory.contents = inventory.contents
        meta.blockState = shulker
        shulkerBox.itemMeta = meta
    }

    fun handleInventoryClick(event: InventoryClickEvent) {
        if (event.whoClicked != player) return
        val view = event.view
        val top = view.topInventory
        if (top.type != InventoryType.SHULKER_BOX) return

        val action = event.action
        val clickedInv = event.clickedInventory

        if (action == org.bukkit.event.inventory.InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInv != null && clickedInv != top) {
            val current = event.currentItem
            if (isShulkerBox(current)) {
                event.isCancelled = true
                return
            }
        }

        if (clickedInv == top && when (action) {
                org.bukkit.event.inventory.InventoryAction.PLACE_ALL,
                org.bukkit.event.inventory.InventoryAction.PLACE_ONE,
                org.bukkit.event.inventory.InventoryAction.PLACE_SOME,
                org.bukkit.event.inventory.InventoryAction.SWAP_WITH_CURSOR -> true
                else -> false
            }
        ) {
            if (isShulkerBox(event.cursor)) {
                event.isCancelled = true
                return
            }
        }

        if (clickedInv == top && (action == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP || action == org.bukkit.event.inventory.InventoryAction.HOTBAR_MOVE_AND_READD)) {
            val hotbarIndex = event.hotbarButton
            if (hotbarIndex >= 0) {
                val hotItem = player.inventory.getItem(hotbarIndex)
                if (isShulkerBox(hotItem)) {
                    event.isCancelled = true
                    return
                }
            }
        }
    }

    fun handleInventoryDrag(event: InventoryDragEvent) {
        if (event.whoClicked != player) return
        val view = event.view
        val top = view.topInventory
        if (top.type != InventoryType.SHULKER_BOX) return

        val affectsTop = event.rawSlots.any { it < top.size }
        if (!affectsTop) return

        if (event.newItems.values.any { isShulkerBox(it) }) {
            event.isCancelled = true
            return
        }
    }

    private fun isShulkerBox(item: ItemStack?): Boolean {
        if (item == null) return false
        val material = item.type
        return material.name.endsWith("SHULKER_BOX")
    }
}
