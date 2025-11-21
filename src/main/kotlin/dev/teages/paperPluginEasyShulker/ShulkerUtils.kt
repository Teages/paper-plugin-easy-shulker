package dev.teages.paperPluginEasyShulker

import org.bukkit.inventory.ItemStack

/**
 * Utility object containing helper methods for working with shulker boxes.
 * Provides methods to identify and compare shulker box items.
 */
object ShulkerUtils {
    
    /**
     * Checks if the given item stack is a shulker box.
     * @param item The item stack to check (can be null)
     * @return true if the item is a shulker box, false otherwise
     */
    fun isShulkerBox(item: ItemStack?): Boolean {
        if (item == null) return false
        // Check if the item type name ends with "SHULKER_BOX"
        return item.type.name.endsWith("SHULKER_BOX")
    }
    
    /**
     * Checks if two item stacks are similar shulker boxes.
     * Two items are considered similar if they are the same type, have the same metadata,
     * and both are shulker boxes.
     * @param item The first item to compare (can be null)
     * @param shulkerBox The second item to compare (can be null)
     * @return true if both items are similar shulker boxes, false otherwise
     */
    fun isSimilarShulkerBox(item: ItemStack?, shulkerBox: ItemStack?): Boolean {
        if (item == null || shulkerBox == null) return false
        // Check if items are similar and both are shulker boxes
        return item.isSimilar(shulkerBox) && isShulkerBox(item)
    }
}
