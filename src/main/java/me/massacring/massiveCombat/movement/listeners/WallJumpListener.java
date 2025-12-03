package me.massacring.massiveCombat.movement.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class WallJumpListener implements Listener {
    private final MassiveCombat plugin;
    private final double multiple;
    private final double setY;
    private final Sound sound;
    private final boolean useCooldown;
    private final int cooldownTicks;
    private final int num_of_jumps;

    public WallJumpListener(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.multiple = config.getDouble("wall_jump_multiple");
        this.setY = config.getDouble("wall_jump_setY");

        String soundStr = config.getString("wall_jump_sound");
        if (soundStr == null) soundStr = "";
        NamespacedKey soundKey = NamespacedKey.fromString(soundStr);
        if (soundKey != null)
            this.sound = Registry.SOUNDS.get(soundKey);
        else
            this.sound = Sound.ENTITY_BREEZE_DEFLECT;

        this.useCooldown = config.getBoolean("wall_jump_use_cooldown");
        this.cooldownTicks = config.getInt("wall_jump_cooldown");
        this.num_of_jumps = config.getInt("wall_jump_num_of_jumps");
    }

    @EventHandler
    public void onPlayerFlight(PlayerToggleFlightEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        if (!player.hasPermission("massivecombat.ability.starter.wall_jump")) return;

        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (!playerNBT.has(new NamespacedKey(this.plugin, "massivecombat.double_jump.cooldown"))) return;
        if (playerNBT.has(new NamespacedKey(this.plugin, "massivecombat.wall_jump.cooldown"))) return;

        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        Location location = player.getLocation();
        player.setVelocity(location.getDirection().multiply(this.multiple).setY(this.setY));
        player.getWorld().playSound(location, this.sound, SoundCategory.PLAYERS, 1, 1);
        player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getLocation(), 20, 0.5, 0, 0.5, 0);
        player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 10, 0.5, 0, 0.5, 0.03);

        // Update last coords
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.wall_jump.last_coords"), PersistentDataType.INTEGER_ARRAY, new int[] {location.getBlockX(), location.getBlockZ()});

        // Decrease jump count
        Integer jumps = playerNBT.get(new NamespacedKey(this.plugin, "massivecombat.wall_jump.jumps"), PersistentDataType.INTEGER);
        if (jumps == null) jumps = this.num_of_jumps;
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.wall_jump.jumps"), PersistentDataType.INTEGER, jumps-1);

        // set wall jump cooldown tag
        long cooldownTime = System.currentTimeMillis() + (this.useCooldown ? (this.cooldownTicks * 50L) : 0);
        playerNBT.set(new NamespacedKey(this.plugin, "massivecombat.wall_jump.cooldown"), PersistentDataType.LONG, cooldownTime);
    }
}