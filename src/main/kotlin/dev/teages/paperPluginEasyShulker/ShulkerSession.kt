package dev.teages.paperPluginEasyShulker

import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.inventory.*
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MenuType
import org.bukkit.inventory.meta.BlockStateMeta

/**
 * Represents a session for a player interacting with a shulker box.
 * Manages the virtual inventory interface and handles inventory interactions.
 */
class ShulkerSession(
    private val player: Player,
    private val shulkerBox: ItemStack,
) {
    
    // Getters for the player and shulker box associated with this session
    fun getPlayer(): Player = player
    
    fun getShulkerBox(): ItemStack = shulkerBox

    /**
     * Opens a virtual shulker box inventory for the player.
     * Creates a custom inventory view that displays the contents of the shulker box.
     */
    fun open() {
        // Get the block state metadata from the shulker box item
        val meta = shulkerBox.itemMeta as? BlockStateMeta ?: return
        // Get the actual shulker box block state
        val shulker = meta.blockState as? ShulkerBox ?: return

        // Create a shulker box menu with the display name from the item
        val view = MenuType.SHULKER_BOX.builder()
            .title(meta.displayName())
            .build(player)
        
        // Copy the contents from the shulker box to the virtual inventory
        view.topInventory.contents = shulker.inventory.contents
        // Open the inventory view for the player
        view.open()
    }

    /**
     * Saves the current state of the inventory back to the shulker box item.
     * This is called when the inventory is closed or when changes need to be persisted.
     */
    fun save(inventory: Inventory) {
        // Get the block state metadata from the shulker box item
        val meta = shulkerBox.itemMeta as? BlockStateMeta ?: return
        // Get the actual shulker box block state
        val shulker = meta.blockState as? ShulkerBox ?: return
        
        // Copy the inventory contents back to the shulker box
        shulker.inventory.contents = inventory.contents
        // Update the block state in the metadata
        meta.blockState = shulker
        // Apply the updated metadata to the item
        shulkerBox.itemMeta = meta
    }

    /**
     * Handles inventory click events within the shulker box interface.
     * Prevents invalid operations like placing shulker boxes inside other shulker boxes.
     */
    fun handleInventoryClick(event: InventoryClickEvent) {
        // Ensure the click is from the correct player and inventory
        if (event.whoClicked != player) return
        val view = event.view
        val top = view.topInventory
        if (top.type != InventoryType.SHULKER_BOX) return

        val action = event.action
        val clickedInv = event.clickedInventory

        // Prevent moving shulker boxes from player inventory to shulker box
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY && clickedInv != null && clickedInv != top) {
            if (ShulkerUtils.isShulkerBox(event.currentItem)) {
                event.isCancelled = true
                return
            }
        }

        // Prevent placing shulker boxes from cursor into shulker box
        if (clickedInv == top && action in setOf(
                InventoryAction.PLACE_ALL,
                InventoryAction.PLACE_ONE,
                InventoryAction.PLACE_SOME,
                InventoryAction.SWAP_WITH_CURSOR
            )
        ) {
            if (ShulkerUtils.isShulkerBox(event.cursor)) {
                event.isCancelled = true
                return
            }
        }

        // Prevent hotbar swap with shulker boxes
        if (clickedInv == top && action in setOf(
                InventoryAction.HOTBAR_SWAP,
                InventoryAction.HOTBAR_MOVE_AND_READD
            )
        ) {
            val hotbarIndex = event.hotbarButton
            if (hotbarIndex >= 0 && ShulkerUtils.isShulkerBox(player.inventory.getItem(hotbarIndex))) {
                event.isCancelled = true
                return
            }
        }
    }

    /**
     * Handles inventory drag events within the shulker box interface.
     * Prevents dragging shulker boxes into the shulker box inventory.
     */
    fun handleInventoryDrag(event: InventoryDragEvent) {
        // Ensure the drag is from the correct player and inventory
        if (event.whoClicked != player) return
        val view = event.view
        val top = view.topInventory
        if (top.type != InventoryType.SHULKER_BOX) return

        // Check if the drag affects the shulker box inventory (top inventory)
        val affectsTop = event.rawSlots.any { it < top.size }
        if (!affectsTop) return

        // Prevent dragging shulker boxes into the shulker box
        if (event.newItems.values.any { ShulkerUtils.isShulkerBox(it) }) {
            event.isCancelled = true
            return
        }
    }
}
