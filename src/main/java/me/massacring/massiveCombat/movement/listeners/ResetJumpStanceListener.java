package me.massacring.massiveCombat.movement.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import net.kyori.adventure.util.TriState;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ResetJumpStanceListener implements Listener {
    private final MassiveCombat plugin;
    private final int minDistance;

    public ResetJumpStanceListener(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.minDistance = config.getInt("wall_jump_min_distance");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (!playerNBT.has(new NamespacedKey(plugin, "massivecombat.double_jump.ready"))) player.setAllowFlight(false);

        if (player.getAllowFlight()) return;

        boolean onGround = isPlayerOnGround(player);
        if (onGround) {
            doubleJumpCheck(player);
            playerNBT.remove(new NamespacedKey(plugin, "massivecombat.wall_jump.last_coords"));
            playerNBT.remove(new NamespacedKey(plugin, "massivecombat.wall_jump.jumps"));
            return;
        } else playerNBT.remove(new NamespacedKey(plugin, "massivecombat.double_jump.ready"));
        Block playerBlock = player.getLocation().getBlock();
        if (playerBlock.getType() != Material.AIR ||
                playerBlock.getRelative(BlockFace.NORTH).getType() != Material.AIR ||
                playerBlock.getRelative(BlockFace.EAST).getType() != Material.AIR ||
                playerBlock.getRelative(BlockFace.SOUTH).getType() != Material.AIR ||
                playerBlock.getRelative(BlockFace.WEST).getType() != Material.AIR) {
            wallJumpCheck(player);
        }
    }

    private void doubleJumpCheck(Player player) {
        if (!player.hasPermission("massivecombat.ability.starter.double_jump")) return;

        // remove double jump cooldown tag
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        Long cooldownTime = playerNBT.get(new NamespacedKey(plugin, "massivecombat.double_jump.cooldown"), PersistentDataType.LONG);
        if (cooldownTime != null && System.currentTimeMillis() >= cooldownTime)
            playerNBT.remove(new NamespacedKey(plugin, "massivecombat.double_jump.cooldown"));

        // return if player has the double jump cooldown tag
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.double_jump.cooldown"))) return;

        player.setAllowFlight(true);
        player.setFlyingFallDamage(TriState.TRUE);
        playerNBT.set(new NamespacedKey(plugin, "massivecombat.double_jump.ready"), PersistentDataType.BOOLEAN, true);
    }

    private void wallJumpCheck(Player player) {
        if (!player.hasPermission("massivecombat.ability.starter.wall_jump")) return;

        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        Integer jumps = playerNBT.get(new NamespacedKey(plugin, "massivecombat.wall_jump.jumps"), PersistentDataType.INTEGER);
        if (jumps != null && jumps <= 0) return;

        int[] lastCoords = playerNBT.get(new NamespacedKey(plugin, "massivecombat.wall_jump.last_coords"), PersistentDataType.INTEGER_ARRAY);
        if (lastCoords != null) {
            int differenceX = Math.abs(lastCoords[0] - player.getLocation().getBlockX());
            int differenceZ = Math.abs(lastCoords[1] - player.getLocation().getBlockZ());
            int difference = differenceX + differenceZ;
            if (difference < this.minDistance) return;
        }

        // remove wall jump cooldown tag
        Long cooldownTime = playerNBT.get(new NamespacedKey(plugin, "massivecombat.wall_jump.cooldown"), PersistentDataType.LONG);
        if (cooldownTime != null && System.currentTimeMillis() >= cooldownTime)
            playerNBT.remove(new NamespacedKey(plugin, "massivecombat.wall_jump.cooldown"));

        // return if player has the wall jump cooldown tag
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.wall_jump.cooldown"))) return;

        player.setAllowFlight(true);
        player.setFlyingFallDamage(TriState.TRUE);
    }

    private boolean isPlayerOnGround(Player player) {
        if (player.isFlying()) return false;
        Location startLocation = player.getLocation();
        startLocation.setPitch(90);
        final double gravity = -0.0784000015258789D;
        double fallVelocity = player.getVelocity().getY();
        return (fallVelocity == gravity);
    }
}