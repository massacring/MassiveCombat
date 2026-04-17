package me.massacring.massiveCombat;

import me.massacring.massiveCombat.combat.events.CallPlayerChangeHealthEvent;
import me.massacring.massiveCombat.combat.listeners.BackstabEvents;
import me.massacring.massiveCombat.combat.listeners.VanillaDeflectionEvents;
import me.massacring.massiveCombat.addons.listeners.MMOCoreWMEvents;
import me.massacring.massiveCombat.commands.CommandManager;
import me.massacring.massiveCombat.commands.CustomTabCompleter;
import me.massacring.massiveCombat.items.listeners.HealingItemsEvents;
import me.massacring.massiveCombat.listeners.InvincibilityFramesEvents;
import me.massacring.massiveCombat.listeners.OnPlayerJoin;
import me.massacring.massiveCombat.movement.listeners.DoubleJumpListener;
import me.massacring.massiveCombat.movement.listeners.ResetJumpStanceListener;
import me.massacring.massiveCombat.movement.listeners.SlidingListener;
import me.massacring.massiveCombat.movement.listeners.WallJumpListener;
import me.massacring.massiveCombat.addons.listeners.WMDeflectionEvents;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.swing.*;
import java.util.stream.Stream;

public final class MassiveCombat extends JavaPlugin {
    private static MassiveCombat INSTANCE;

    private FileConfiguration config;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        this.config = this.getConfig();
        registerListeners();
        registerCommands();
        if (!getServer().getPluginManager().isPluginEnabled("WeaponMechanics")) {
            getLogger().warning("WeaponMechanics not found! Some features will be disabled.");
        }
        if (!getServer().getPluginManager().isPluginEnabled("MMOCore")) {
            getLogger().warning("MMOCore not found! Some features will be disabled.");
        }
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().warning("PlaceholderAPI not found! Some features will be disabled.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static MassiveCombat getInstance() {
        return INSTANCE;
    }

    private void registerListeners() {
        // Vanilla listeners
        Stream<Listener> vanillaStream;
        vanillaStream = Stream.of(
                new OnPlayerJoin(),
                new InvincibilityFramesEvents(),
                new ResetJumpStanceListener(),
                new DoubleJumpListener(),
                new WallJumpListener(),
                new SlidingListener(),
                new CallPlayerChangeHealthEvent(),
                //new HealthEffectsEvents(),
                new BackstabEvents(),
                //new TargetDummyListener(),
                new HealingItemsEvents()
        );

        if (config.getBoolean("deflection_vanilla")) {
            vanillaStream = Stream.concat(vanillaStream, Stream.of(new VanillaDeflectionEvents()));
        }

        vanillaStream.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        // Addon Listeners
        Stream<Listener> addonStream = Stream.empty();
        if (getServer().getPluginManager().isPluginEnabled("WeaponMechanics")) {

            if (getServer().getPluginManager().isPluginEnabled("MMOCore") &&
                    getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                addonStream = Stream.concat(addonStream, Stream.of(new MMOCoreWMEvents()));
            }

            if (config.getBoolean("deflection_weapon_mechanics")) {
                addonStream = Stream.concat(addonStream, Stream.of(new WMDeflectionEvents()));
            }

            addonStream.forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
        }
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager();
        Stream.of(
                "combat"
        ).forEach(label -> {
            PluginCommand command = getCommand(label);
            if (command != null) {
                command.setExecutor(commandManager);
                command.setTabCompleter(new CustomTabCompleter());
            }
            else getLogger().warning(String.format("Failed to register command '%s', check plugin.yml", label));
        });
    }

    public FileConfiguration getConfiguration() {
        return config;
    }
}
