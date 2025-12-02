package me.massacring.massiveCombat.combat.runnables;

import me.massacring.massiveCombat.MassiveCombat;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class LowHealthEffectTask extends BukkitRunnable {
    private final HashMap<String, ImmutablePair<Integer, Integer>> effects; // Name, (Duration, Amplifier)
    private final UUID uuid;
    private final MassiveCombat plugin;

    public LowHealthEffectTask(HashMap<String, ImmutablePair<Integer, Integer>> effects, UUID uuid, MassiveCombat plugin) {
        this.effects = effects;
        this.uuid = uuid;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;
        if (effects == null) return;
        this.effects.forEach((name, dataPair) -> {
            NamespacedKey namespacedKey = NamespacedKey.minecraft(name.toLowerCase());
            PotionEffectType type = Registry.EFFECT.get(namespacedKey);
            if (type == null) return;
            int duration = dataPair.getLeft();
            int amplifier = dataPair.getRight();
            player.addPotionEffect(new PotionEffect(type, duration, amplifier));
        });
    }
}