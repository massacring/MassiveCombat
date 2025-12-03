package me.massacring.massiveCombat.items.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.items.types.HealingItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static me.massacring.massiveCombat.utils.getNBT.itemNBT;

public class HealingItemsEvents implements Listener {
    private final MassiveCombat plugin;
    private final HashMap<Player, BukkitTask> taskHash = new HashMap<>();
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
            boolean instant = itemSection.getBoolean("instant");
            int useTime = itemSection.getInt("use_time");
            float useSlow = (float) itemSection.getDouble("use_slow");

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

            HealingItem item = new HealingItem(tag, healAmount, instant, useTime, useSlow, addEffects, removeEffects);
            itemHash.put(itemName, item);
        });
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        if (event.useInteractedBlock().equals(Event.Result.DENY) && event.useItemInHand().equals(Event.Result.DENY)) return;
        // Check if the event is a right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // Check if the action is done by the off-hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        // Check that an item is held
        ItemStack item = event.getItem();
        if (item == null) return;
        // Check if the player has permission
        if (!player.hasPermission("massivecombat.items.healing")) return;
        // Check that the item has the correct tags
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        if (!itemNBT.has(new NamespacedKey(this.plugin, "massive_healing_item")))
            return;
        // Get the healing item from the tag.
        String itemTag = itemNBT.get(new NamespacedKey(this.plugin, "massive_healing_item"), PersistentDataType.STRING);
        HealingItem healingItem = itemHash.get(itemTag);
        if (healingItem == null) return;

        cancelItemUse(player);
        useItem(player, healingItem);
    }

    // Prevents the healing item from being placed if it is a block.
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        PersistentDataContainer NBTHand = itemNBT(heldItem);
        if (NBTHand == null) return;
        if (!NBTHand.has(new NamespacedKey(this.plugin, "massive_healing_item"))) return;
        event.setCancelled(true);
    }

    // Cancels item use when you change held item.
    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        cancelItemUse(player);
    }

    private void useItem(Player player, HealingItem item) {
        if (item.instant()) {
            useItemEffects(player, item);
            return;
        }
        int[] counter = {0};
        final float[] oldSpeed = new float[1];
        BukkitTask healing = new BukkitRunnable() {
            @Override
            public void run() {
                if (counter[0] == 0) {
                    oldSpeed[0] = player.getWalkSpeed();
                    player.setWalkSpeed(oldSpeed[0] * item.useSlow());
                }
                counter[0]++;

                if (player.getOpenInventory().getType() != InventoryType.CRAFTING) {
                    cancelItemUse(player);
                }

                if (counter[0] >= item.useTime()) {
                    useItemEffects(player, item);
                    cancelItemUse(player);
                }
            }

            @Override
            public void cancel() {
                super.cancel();
                player.setWalkSpeed(oldSpeed[0]);
            }
        }.runTaskTimer(this.plugin, 0, 1);
        taskHash.put(player, healing);
    }

    private void cancelItemUse(Player player) {
        if (this.taskHash.containsKey(player)) {
            this.taskHash.get(player).cancel();
            this.taskHash.remove(player);
        }
    }

    private void useItemEffects(Player player, HealingItem item) {
        // Remove an item
        player.getInventory().setItemInMainHand(player.getInventory().getItemInMainHand().subtract());
        // Heal player
        player.heal(item.healAmount());
        // Remove effects
        item.removeEffects().forEach(player::removePotionEffect);
        // Add effects
        item.addEffects().forEach(player::addPotionEffect);
    }
}
