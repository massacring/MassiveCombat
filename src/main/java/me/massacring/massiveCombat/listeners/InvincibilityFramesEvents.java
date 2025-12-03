package me.massacring.massiveCombat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class InvincibilityFramesEvents implements Listener {
    private final boolean disableIFrames;

    public InvincibilityFramesEvents(MassiveCombat plugin) {
        FileConfiguration config = plugin.getConfig();
        this.disableIFrames = config.getBoolean("disable_invulnerability_frames");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void disableIFrames(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!disableIFrames) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // Immediately reset the no-damage ticks to 0, disabling invincibility frames
        entity.setMaximumNoDamageTicks(0);
        entity.setNoDamageTicks(0);
    }
}