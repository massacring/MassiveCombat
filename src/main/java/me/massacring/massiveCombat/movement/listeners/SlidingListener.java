package me.massacring.massiveCombat.movement.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class SlidingListener implements Listener {
    private final MassiveCombat plugin;
    private final double multiple;
    private final String sound;
    private final boolean useCooldown;
    private final int cooldownTicks;
    private final int crawlDuration;

    public SlidingListener(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.multiple = config.getDouble("slide_multiple");
        this.sound = config.getString("slide_sound");
        this.useCooldown = config.getBoolean("slide_use_cooldown");
        this.cooldownTicks = config.getInt("slide_cooldown");
        this.crawlDuration = config.getInt("slide_craw_duration");
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!player.hasPermission("massivecombat.ability.starter.slide")) return;
        if (!event.isSneaking()) return;
        if (!player.isSprinting()) return;
        boolean onGround = (!player.isFlying() && player.getLocation().subtract(0, 0.1, 0).getBlock().getType() != Material.AIR);
        if (!onGround) return;
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.slide.busy"))) return;

        // return if sliding is on cooldown
        Long cooldown = playerNBT.get(new NamespacedKey(plugin, "massivecombat.slide.cooldown"), PersistentDataType.LONG);
        if (cooldown != null && System.currentTimeMillis() < cooldown) return;

        slide(player);

        // set wall jump cooldown tag
        long cooldownTime = System.currentTimeMillis() + (useCooldown ? (cooldownTicks * 50L) : 0);
        playerNBT.set(new NamespacedKey(plugin, "massivecombat.slide.cooldown"), PersistentDataType.LONG, cooldownTime);
    }

    @EventHandler
    public void disableSneak(PlayerToggleSneakEvent event) {
        if (event.isCancelled()) return;
        if (!event.isSneaking()) return;
        Player player = event.getPlayer();
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.slide.busy"))) event.setCancelled(true);
    }

    @EventHandler
    public void disableGliding(EntityToggleGlideEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.slide.busy"))) event.setCancelled(true);
    }

    @EventHandler
    public void disableJumping(PlayerJumpEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.slide.busy")))
            playerNBT.remove(new NamespacedKey(plugin, "massivecombat.slide.busy"));
    }

    private void slide(Player player) {
        player.setGliding(true);
        float originalFlySpeed = player.getFlySpeed();
        player.setFlySpeed(-1);
        player.setJumping(false);
        player.setSneaking(false);

        player.setVelocity(player.getLocation().getDirection().multiply(multiple).setY(0));
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        playerNBT.set(new NamespacedKey(plugin, "massivecombat.slide.busy"), PersistentDataType.BOOLEAN, true);
        player.getWorld().playSound(player.getLocation(), Sound.valueOf(sound), SoundCategory.PLAYERS, 1, 1);

        new BukkitRunnable() {

            @Override
            public void run() {
                player.setFlySpeed(originalFlySpeed);
                playerNBT.remove(new NamespacedKey(plugin, "massivecombat.slide.busy"));
            }
        }.runTaskLater(plugin, crawlDuration);
    }
}