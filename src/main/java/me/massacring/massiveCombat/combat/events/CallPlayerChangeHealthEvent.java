package me.massacring.massiveCombat.combat.events;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class CallPlayerChangeHealthEvent implements Listener {
    private final MassiveCombat plugin;

    public CallPlayerChangeHealthEvent(MassiveCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerChangeHealthEvent customEvent = new PlayerChangeHealthEvent(player, -(event.getFinalDamage()));
        plugin.getServer().getPluginManager().callEvent(customEvent);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerHealed(EntityRegainHealthEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerChangeHealthEvent customEvent = new PlayerChangeHealthEvent(player, event.getAmount());
        plugin.getServer().getPluginManager().callEvent(customEvent);
    }
}