package dev.teages.paperPluginEasyShulker

import org.bukkit.inventory.ItemStack
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

/**
 * Utility object containing helper methods for working with shulker boxes.
 * Provides methods to identify and compare shulker box items.
 */
object ShulkerUtils {
    
    // NBT key for tracking opened shulker box instances
    private const val SESSION_UUID_KEY = "easy_shulker_session_uuid"
    
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
    
    /**
     * Checks if two item stacks are the exact same shulker box instance.
     * This uses a session UUID to identify the specific shulker box being viewed,
     * allowing differentiation between identical copies (e.g., from creative mode).
     * @param item The first item to compare (can be null)
     * @param shulkerBox The second item to compare (must have a session UUID)
     * @return true if both items reference the same shulker box instance, false otherwise
     */
    fun isSameShulkerInstance(item: ItemStack?, shulkerBox: ItemStack?): Boolean {
        if (item == null || shulkerBox == null) return false
        if (!isShulkerBox(item) || !isShulkerBox(shulkerBox)) return false
        
        // Get session UUID from the opened shulker box
        val sessionUuid = getSessionUuid(shulkerBox) ?: return false
        // Get UUID from the item being checked
        val itemUuid = getSessionUuid(item) ?: return false
        
        // Compare UUIDs to determine if they're the same instance
        return sessionUuid == itemUuid
    }
    
    /**
     * Adds a unique session UUID to a shulker box item.
     * This marks the shulker box as currently being viewed.
     * @param shulkerBox The shulker box to mark
     * @param namespace The plugin's namespace for the NBT key
     */
    fun addSessionUuid(shulkerBox: ItemStack, namespace: NamespacedKey) {
        val meta = shulkerBox.itemMeta ?: return
        val container = meta.persistentDataContainer
        
        // Generate and store a new UUID
        val uuid = UUID.randomUUID().toString()
        container.set(namespace, PersistentDataType.STRING, uuid)
        
        shulkerBox.itemMeta = meta
    }
    
    /**
     * Removes the session UUID from a shulker box item.
     * This should be called when the shulker box is closed.
     * @param shulkerBox The shulker box to clean
     * @param namespace The plugin's namespace for the NBT key
     */
    fun removeSessionUuid(shulkerBox: ItemStack, namespace: NamespacedKey) {
        val meta = shulkerBox.itemMeta ?: return
        val container = meta.persistentDataContainer
        
        // Remove the UUID tag
        container.remove(namespace)
        
        shulkerBox.itemMeta = meta
    }
    
    /**
     * Gets the session UUID from a shulker box item, if present.
     * @param shulkerBox The shulker box to check
     * @return The UUID string, or null if not present
     */
    private fun getSessionUuid(shulkerBox: ItemStack): String? {
        val meta = shulkerBox.itemMeta ?: return null
        val container = meta.persistentDataContainer
        
        // Try to get the UUID from all possible namespaced keys
        // We need to check if the key exists by trying to retrieve it
        return try {
            // This is a simplified version - in practice we'd need the actual namespace
            // For now, we'll just check the standard location
            container.keys.firstOrNull { it.key == SESSION_UUID_KEY }?.let { key ->
                container.get(key, PersistentDataType.STRING)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Creates a namespaced key for the session UUID.
     * @param plugin The plugin instance
     * @return The namespaced key
     */
    fun createSessionKey(plugin: org.bukkit.plugin.Plugin): NamespacedKey {
        return NamespacedKey(plugin, SESSION_UUID_KEY)
    }
}
