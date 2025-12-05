package me.massacring.massiveCombat.combat.listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.List;

public class DeflectionEvents implements Listener {
    private final MassiveCombat plugin;
    private final List<String> whitelistTags;
    private final double minimumAngle;
    private final int power;
    private final Sound sound;
    private final boolean useCooldown;
    private final int cooldownTicks;

    public DeflectionEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.whitelistTags = config.getStringList("deflection_whitelist_tags");
        this.minimumAngle = config.getDouble("deflection_minimum_angle");
        this.power = config.getInt("deflection_power");

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

    private boolean canDeflect(ItemStack item) {
        if (item == null) return false;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return false;
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        boolean tagsMatch = false;
        for (String tag : this.whitelistTags) {
            NamespacedKey key = NamespacedKey.fromString(tag);
            if (key == null) continue;
            if (itemNBT.has(key)) {
                tagsMatch = true;
                break;
            }
        }
        return tagsMatch;
    }

    @EventHandler
    public void deflectArrow(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasPermission("massivecombat.ability.starter.deflect")) return;
        if (player.isBlocking()) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;

        // Check if the item in either hand can deflect.
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!canDeflect(item)) {
            item = player.getInventory().getItemInOffHand();
            if (!canDeflect(item)) return;
        }

        // Check if the player is on cooldown
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"))) {
            Long cooldownTime = playerNBT.get(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG);
            if (cooldownTime != null && System.currentTimeMillis() >= cooldownTime)
                playerNBT.remove(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"));
            else return;
        }

        // Check if the player is angled correctly
        Vector playerDirection = player.getEyeLocation().getDirection().normalize();
        Vector arrowDirection = arrow.getLocation().getDirection().normalize();
        double dotProduct = Math.abs(playerDirection.dot(arrowDirection));
        double minDot = Math.cos(Math.toRadians(this.minimumAngle));
        if (dotProduct < minDot) {
            // Cancel Shield Blocking
            if (player.isBlocking() && event.getFinalDamage() == 0) {
                player.damage(event.getDamage());
                arrow.remove();
            }
            return;
        }

        // Get and play Sound
        player.getWorld().playSound(player.getLocation(), this.sound, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Deflect particle
        Vector playerVector = player.getLocation().getDirection().normalize();
        Location sweepLocation = player.getEyeLocation().add(playerVector).subtract(new Vector(0, 0.3, 0));
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, sweepLocation, 1);

        // Reverse the arrow's direction.
        new BukkitRunnable() {
            @Override
            public void run() {
                arrow.setVelocity(player.getEyeLocation().getDirection().multiply(power));
            }
        }.runTaskLater(this.plugin, 1);

        event.setCancelled(true);

        // set deflection cooldown tag
        long cooldownTime = System.currentTimeMillis() + (this.useCooldown ? (this.cooldownTicks * 50L) : 0);
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG, cooldownTime);
    }
}