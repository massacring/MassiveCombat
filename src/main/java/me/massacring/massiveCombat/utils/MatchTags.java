package me.massacring.massiveCombat.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;

public class MatchTags {
    public static boolean matchTags(ItemStack item, List<String> whitelistTags) {
        if (item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        boolean tagsMatch = false;
        for (String tag : whitelistTags) {
            NamespacedKey key = NamespacedKey.fromString(tag);
            if (key == null) continue;
            if (itemNBT.has(key)) {
                tagsMatch = true;
                break;
            }
        }
        return tagsMatch;
    }
}
