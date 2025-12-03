package me.massacring.massiveCombat.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;

public class getNBT {
    public static PersistentDataContainer itemNBT(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer dataContainer = null;
        if (itemMeta != null)
            dataContainer = itemMeta.getPersistentDataContainer();

        return dataContainer;
    }
}