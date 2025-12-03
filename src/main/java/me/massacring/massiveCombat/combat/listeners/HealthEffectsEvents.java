package me.massacring.massiveCombat.combat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.combat.events.PlayerChangeHealthEvent;
import me.massacring.massiveCombat.combat.runnables.LowHealthEffectTask;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class HealthEffectsEvents implements Listener {
    private final MassiveCombat plugin;
    private final HashMap<Integer, HashMap<String, ImmutablePair<Integer, Integer>>> lowHealthEffects = new HashMap<>();
    private final HashMap<UUID, LowHealthEffectTask> lowHealthEffectTasks = new HashMap<>();
    private final FileConfiguration config;
    private final int LHE_duration;
    private final int ticksPerDamage;

    public HealthEffectsEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
        LHE_duration = this.config.getInt("LHE_duration");
        ticksPerDamage = this.config.getInt("slowness_ticks_per_block_fallen");
        loadDebuffs();
    }

    @EventHandler
    public void onPlayerChangeHealth(PlayerChangeHealthEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (!player.hasPermission("massivecombat.health_effects")) return;

        UUID uuid = player.getUniqueId();
        if (lowHealthEffectTasks.containsKey(uuid))
            lowHealthEffectTasks.get(uuid).cancel();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        int newPlayerHealth = Math.abs((int)(player.getHealth() + event.getAmount()));
        if (!lowHealthEffects.containsKey(newPlayerHealth)) return;

        LowHealthEffectTask task = new LowHealthEffectTask(lowHealthEffects.get(newPlayerHealth), player.getUniqueId(), plugin);
        task.runTaskTimer(plugin, 0L, LHE_duration/2);
        lowHealthEffectTasks.put(uuid, task);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (lowHealthEffectTasks.containsKey(uuid))
            lowHealthEffectTasks.get(uuid).cancel();
    }

    private void loadDebuffs() {
        ConfigurationSection lowHealthEffectsSection = this.config.getConfigurationSection("low_health_effects");
        if (lowHealthEffectsSection == null) return;

        Set<String> keys = lowHealthEffectsSection.getKeys(false);
        keys.forEach(key -> {
            ConfigurationSection section = Objects.requireNonNull(lowHealthEffectsSection.getConfigurationSection(key));
            if (section.getKeys(false).contains("copy")) return;

            int keyNumber;
            try {
                keyNumber = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                System.err.println("Error in config file: Invalid section(s) under low_health_effects");
                return;
            }

            ConfigurationSection toAdd = section.getConfigurationSection("adds");
            if (toAdd == null) {
                System.err.println("Error in config file: Invalid section(s) under low_health_effects. " + key + " is missing a section \"adds\"");
                return;
            }

            Set<String> adds = toAdd.getKeys(false);
            HashMap<String, ImmutablePair<Integer, Integer>> effects = new HashMap<>();

            adds.forEach(name -> {
                NamespacedKey namespacedKey = NamespacedKey.minecraft(name.toLowerCase());
                PotionEffectType type = Registry.EFFECT.get(namespacedKey);
                if (type == null) return;
                ImmutablePair<Integer, Integer> dataPair = new ImmutablePair<>(LHE_duration, toAdd.getInt(name));
                effects.put(name, dataPair);
            });
            lowHealthEffects.put(keyNumber, effects);
        });
        keys.forEach(key -> {
            ConfigurationSection section = Objects.requireNonNull(lowHealthEffectsSection.getConfigurationSection(key));
            if (!section.getKeys(false).contains("copy")) return;

            int keyNumber;
            try {
                keyNumber = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                System.err.println("Error in config file: Invalid section(s) under low_health_effects");
                return;
            }

            int copyKey = section.getInt("copy");

            lowHealthEffects.put(keyNumber, lowHealthEffects.get(copyKey));
        });
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasPermission("massivecombat.safer_fall")) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        double damage = event.getDamage();

        PotionEffect slowness = new PotionEffect(
                PotionEffectType.SLOWNESS,
                (int) (damage * ticksPerDamage), (int) Math.abs((damage / 3)-1)
        );

        player.addPotionEffect(slowness);
    }
}