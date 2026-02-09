package me.massacring.massiveCombat.combat;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.List;

public class Deflection {
    private static boolean deflectFail(ItemStack item, List<String> whitelistTags) {
        if (item == null) return true;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return true;
        PersistentDataContainer itemNBT = itemMeta.getPersistentDataContainer();
        boolean tagsMatch = true;
        for (String tag : whitelistTags) {
            NamespacedKey key = NamespacedKey.fromString(tag);
            if (key == null) continue;
            if (itemNBT.has(key)) {
                tagsMatch = false;
                break;
            }
        }
        return tagsMatch;
    }

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
        if (deflectFail(item, whitelistTags)) {
            item = player.getInventory().getItemInOffHand();
            if (deflectFail(item, whitelistTags)) return false;
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
