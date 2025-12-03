package me.massacring.massiveCombat.combat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DeflectionEvents implements Listener {
    private final MassiveCombat plugin;
    private final HashMap<UUID, BukkitRunnable> rightClickingTasks = new HashMap<>();
    private final long useHoldFrequency;
    private final List<String> whitelistTags;
    private final double minimumAngle;
    private final Sound sound;
    private final boolean useCooldown;
    private final int cooldownTicks;

    public DeflectionEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.useHoldFrequency = config.getLong("deflection_use_hold_frequency");
        this.whitelistTags = config.getStringList("deflection_whitelist_tags");
        this.minimumAngle = config.getDouble("deflection_minimum_angle");

        String soundStr = config.getString("deflection_sound");
        if (soundStr == null) soundStr = "";
        NamespacedKey soundKey = NamespacedKey.fromString(soundStr);
        if (soundKey != null)
            this.sound = Registry.SOUNDS.get(soundKey);
        else
            this.sound = Sound.ENTITY_BREEZE_DEFLECT;

        this.useCooldown = config.getBoolean("deflection_use_cooldown");
        this.cooldownTicks = config.getInt("deflection_cooldown");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the event is a right-click
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // Check if the action is done by the off-hand
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;
        Player player = event.getPlayer();
        // Check that an item is held
        ItemStack item = event.getItem();
        if (item == null) return;
        // Check if the player has permission
        if (!player.hasPermission("massivecombat.ability.starter.deflect")) return;
        // Check if the player is on cooldown
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"))) {
            Long cooldownTime = playerNBT.get(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG);
            if (cooldownTime != null && System.currentTimeMillis() >= cooldownTime)
                playerNBT.remove(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"));
            else return;
        }
        // Check that the item has the correct tags
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        boolean tagsMatch = false;
        for (String tag : this.whitelistTags) {
            if (itemNBT.has(new NamespacedKey(this.plugin, tag))) {
                tagsMatch = true;
                break;
            }
        }
        if (!tagsMatch) return;

        UUID playerId = player.getUniqueId();

        // Cancel any existing task
        if (this.rightClickingTasks.containsKey(playerId)) {
            this.rightClickingTasks.get(playerId).cancel();
        }
        // Add the player to the HashMap
        addPlayerToRightClickingSet(player);
    }

    @EventHandler
    public void deflectArrow(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        // Check if the damaged entity is a player
        if (!(event.getEntity() instanceof Player player)) return;
        // Check if the player was right-clicking
        if (!this.rightClickingTasks.containsKey(player.getUniqueId())) return;
        // Check if the damage was caused by an arrow
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        // Check if the player is angled correctly
        double combinedYaw = Math.abs(player.getYaw()) + Math.abs(arrow.getYaw());
        double angleInDegrees = Math.abs(180 - combinedYaw);
        if (angleInDegrees > this.minimumAngle) return;

        // Get and play Sound
        player.getWorld().playSound(player.getLocation(), this.sound, SoundCategory.PLAYERS, 1.0f, 1.0f);


        // Deflect arrow
        Vector playerVector = player.getLocation().getDirection().normalize();
        Location sweepLocation = player.getEyeLocation().add(playerVector).subtract(new Vector(0, 0.3, 0));
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, sweepLocation, 1);

        Vector arrowVelocity = arrow.getVelocity();
        // Reverse the arrow's direction.
        Vector deflectedVelocity = arrowVelocity.multiply(-1);
        // Applies some random variation to make the deflection more natural
        deflectedVelocity = deflectedVelocity.multiply(new Vector(
                (Math.random() - 0.5) * 0.2,
                (Math.random() - 0.5) * 0.2,
                (Math.random() - 0.5) * 0.2
        ));
        arrow.setVelocity(deflectedVelocity);
        event.setCancelled(true);

        // set deflection cooldown tag
        long cooldownTime = System.currentTimeMillis() + (this.useCooldown ? (this.cooldownTicks * 50L) : 0);
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG, cooldownTime);
    }

    private void addPlayerToRightClickingSet(Player player) {
        UUID playerId = player.getUniqueId();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                removePlayerFromRightClickingSet(player);
            }
        };

        task.runTaskLater(this.plugin, this.useHoldFrequency);

        this.rightClickingTasks.put(playerId, task);
    }

    private void removePlayerFromRightClickingSet(Player player) {
        UUID playerId = player.getUniqueId();
        this.rightClickingTasks.remove(playerId);
    }
}