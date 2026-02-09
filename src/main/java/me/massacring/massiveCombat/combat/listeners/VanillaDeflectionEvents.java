package me.massacring.massiveCombat.combat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static me.massacring.massiveCombat.combat.Deflection.*;

public class VanillaDeflectionEvents implements Listener {
    private final MassiveCombat plugin;
    private final List<String> whitelistTags;
    private final double minimumAngle;
    private final int power;
    private final Sound sound;
    private final boolean useCooldown;
    private final int cooldownTicks;
    private final boolean requiresBlocking;

    public VanillaDeflectionEvents(MassiveCombat plugin) {
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
        this.requiresBlocking = config.getBoolean("deflection_requires_blocking");
    }

    @EventHandler
    public void deflectVanillaProjectile(ProjectileHitEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasPermission("massivecombat.ability.starter.deflect")) return;
        if (player.isBlocking()) return;
        Projectile projectile = event.getEntity();

        if (!deflectCheck(this.plugin, player, projectile.getLocation().getDirection().normalize(), this.whitelistTags, this.minimumAngle, this.requiresBlocking)) return;

        deflectAesthetics(player, this.sound);

        // Reverse the arrow's direction.
        new BukkitRunnable() {
            @Override
            public void run() {
                projectile.setVelocity(player.getEyeLocation().getDirection().multiply(power));
            }
        }.runTaskLater(this.plugin, 1);

        event.setCancelled(true);

        // set deflection cooldown tag
        long cooldownTime = System.currentTimeMillis() + (this.useCooldown ? (this.cooldownTicks * 50L) : 0);
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG, cooldownTime);
    }
}