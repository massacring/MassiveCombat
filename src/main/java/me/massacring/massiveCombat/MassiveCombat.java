package me.massacring.massiveCombat;

import me.massacring.massiveCombat.combat.events.CallPlayerChangeHealthEvent;
import me.massacring.massiveCombat.combat.listeners.BackstabEvents;
import me.massacring.massiveCombat.combat.listeners.DeflectionEvents;
import me.massacring.massiveCombat.combat.listeners.HealthEffectsEvents;
import me.massacring.massiveCombat.combat.listeners.WeaponMechanicsEvents;
import me.massacring.massiveCombat.commands.CommandManager;
import me.massacring.massiveCombat.items.listeners.HealingItemsEvents;
import me.massacring.massiveCombat.listeners.InvincibilityFramesEvents;
import me.massacring.massiveCombat.listeners.OnPlayerJoin;
import me.massacring.massiveCombat.listeners.TargetDummyListener;
import me.massacring.massiveCombat.movement.listeners.DoubleJumpListener;
import me.massacring.massiveCombat.movement.listeners.ResetJumpStanceListener;
import me.massacring.massiveCombat.movement.listeners.SlidingListener;
import me.massacring.massiveCombat.movement.listeners.WallJumpListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Stream;

public final class MassiveCombat extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
        //registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListeners() {
        Stream.of(
                new OnPlayerJoin(this),
                new InvincibilityFramesEvents(this),
                new ResetJumpStanceListener(this),
                new DoubleJumpListener(this),
                new WallJumpListener(this),
                new SlidingListener(this),
                new CallPlayerChangeHealthEvent(this),
                //new HealthEffectsEvents(this),
                new DeflectionEvents(this),
                new BackstabEvents(this),
                new HealingItemsEvents(this),
                //new TargetDummyListener(),
                new WeaponMechanicsEvents()
        ).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager();
        Stream.of(
                "combat"
        ).forEach(label -> {
            PluginCommand command = getCommand(label);
            if (command != null) command.setExecutor(commandManager);
            else getLogger().warning(String.format("Failed to register command '%s', check plugin.yml", label));
        });
    }
}
