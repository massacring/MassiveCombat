package me.massacring.massiveCombat.combat;

import me.massacring.massiveCombat.utils.MatchTags;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;

public class Deflection {
    public static void deflectAesthetics(Player player, Sound sound) {
        // Get and play Sound
        player.getWorld().playSound(player.getLocation(), sound, SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Deflect particle
        Vector playerVector = player.getLocation().getDirection().normalize();
        Location sweepLocation = player.getEyeLocation().add(playerVector).subtract(new Vector(0, 0.3, 0));
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, sweepLocation, 1);
    }

    public static boolean deflectCheck(JavaPlugin plugin, Player player, Vector projDirection, List<String> whitelistTags, double minimumAngle, boolean requiresBlocking) {
        // Check if the item in either hand can deflect.
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!MatchTags.matchTags(item, whitelistTags)) {
            item = player.getInventory().getItemInOffHand();
            if (!MatchTags.matchTags(item, whitelistTags)) return false;
        }

        if (requiresBlocking && !player.isBlocking()) return false;

        // Check if the player is on cooldown
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        if (playerNBT.has(new NamespacedKey(plugin, "massivecombat.deflect.cooldown"))) {
            Long cooldownTime = playerNBT.get(new NamespacedKey(plugin, "massivecombat.deflect.cooldown"), PersistentDataType.LONG);
            if (cooldownTime != null && System.currentTimeMillis() >= cooldownTime)
                playerNBT.remove(new NamespacedKey(plugin, "massivecombat.deflect.cooldown"));
            else return false;
        }

        // Check if the player is angled correctly
        Vector playerDirection = player.getEyeLocation().getDirection().normalize();
        double dotProduct = Math.abs(playerDirection.dot(projDirection));
        double minDot = Math.cos(Math.toRadians(minimumAngle));
        return !(dotProduct < minDot);
    }
}
