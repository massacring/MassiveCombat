package me.massacring.massiveCombat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record OnPlayerJoin(MassiveCombat plugin) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AttributeInstance safeFallDistanceAttribute = player.getAttribute(Attribute.SAFE_FALL_DISTANCE);
        if (safeFallDistanceAttribute == null) return;
        if (!player.hasPermission("massivecombat.safer_fall")) {
            safeFallDistanceAttribute.setBaseValue(safeFallDistanceAttribute.getDefaultValue());
            return;
        }
        double safeFallDistance = this.plugin.getConfig().getDouble("safe_fall_distance");
        safeFallDistanceAttribute.setBaseValue(safeFallDistance);
    }
}