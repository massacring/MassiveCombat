package me.massacring.massiveCombat.items.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.items.types.HealingItem;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HealingItemsEvents implements Listener {
    private final MassiveCombat plugin;
    private final HashMap<String, HealingItem> itemHash = new HashMap<>();

    public HealingItemsEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();

        ConfigurationSection healingItemsSection = config.getConfigurationSection("healing_items");
        if (healingItemsSection == null) return;

        Set<String> itemNames = healingItemsSection.getKeys(false);
        itemNames.forEach(itemName -> {
            ConfigurationSection itemSection = healingItemsSection.getConfigurationSection(itemName);
            if (itemSection == null) return;

            String tag = itemSection.getString("tag");
            if (tag == null) tag = "";
            double healAmount = itemSection.getInt("heal_amount");

            List<PotionEffect> addEffects = new ArrayList<>();
            List<PotionEffectType> removeEffects = new ArrayList<>();

            ConfigurationSection addEffectsSection = itemSection.getConfigurationSection("add_effects");
            if (addEffectsSection != null) {
                Set<String> effectNames = addEffectsSection.getKeys(false);
                effectNames.forEach(effectName -> {
                    NamespacedKey effectKey = NamespacedKey.fromString(effectName.toLowerCase());
                    if (effectKey == null) return;
                    PotionEffectType effectType = Registry.MOB_EFFECT.get(effectKey);
                    if (effectType == null) return;
                    ConfigurationSection effectSection = addEffectsSection.getConfigurationSection(effectName);
                    if (effectSection == null) return;
                    int amplifier = effectSection.getInt("amplifier");
                    int duration = effectSection.getInt("duration");
                    PotionEffect effect = new PotionEffect(effectType, duration, amplifier);
                    addEffects.add(effect);
                });
            }

            ConfigurationSection removeEffectsSection = itemSection.getConfigurationSection("remove_effects");
            if (removeEffectsSection != null) {
                Set<String> effectNames = removeEffectsSection.getKeys(false);
                effectNames.forEach(effectName -> {
                    NamespacedKey effectKey = NamespacedKey.fromString(effectName.toLowerCase());
                    if (effectKey == null) return;
                    PotionEffectType effectType = Registry.MOB_EFFECT.get(effectKey);
                    if (effectType == null) return;
                    removeEffects.add(effectType);
                });
            }

            HealingItem item = new HealingItem(tag, healAmount, addEffects, removeEffects);
            itemHash.put(itemName, item);
        });
    }

    @EventHandler
    public void onItemEaten(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) return;
        // Check if the action is done by the off-hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        // Check if the player has permission
        Player player = event.getPlayer();
        if (!player.hasPermission("massivecombat.items.healing")) return;
        // Check that the item has the correct tags
        ItemStack item = event.getItem();
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        if (!itemNBT.has(new NamespacedKey(this.plugin, "massive_healing_item")))
            return;
        // Get the healing item from the tag.
        String itemTag = itemNBT.get(new NamespacedKey(this.plugin, "massive_healing_item"), PersistentDataType.STRING);
        HealingItem healingItem = itemHash.get(itemTag);
        if (healingItem == null) return;

        useItemEffects(player, healingItem);
    }

    private void useItemEffects(Player player, HealingItem item) {
        // Heal player
        player.heal(item.healAmount());
        // Remove effects
        item.removeEffects().forEach(player::removePotionEffect);
        // Add effects
        item.addEffects().forEach(player::addPotionEffect);
    }
}
