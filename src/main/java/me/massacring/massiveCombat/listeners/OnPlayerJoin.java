package me.massacring.massiveCombat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OnPlayerJoin implements Listener {
    private final MassiveCombat plugin;

    public OnPlayerJoin(MassiveCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        AttributeInstance safeFallDistanceAttribute = player.getAttribute(Attribute.SAFE_FALL_DISTANCE);
        double safeFallDistance = this.plugin.getConfig().getDouble("safe_fall_distance");
        if (safeFallDistanceAttribute != null) safeFallDistanceAttribute.setBaseValue(safeFallDistance);
    }
}