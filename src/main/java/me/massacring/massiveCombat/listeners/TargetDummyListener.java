package me.massacring.massiveCombat.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TargetDummyListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onTargetDummyHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand dummy)) return;
        if (!dummy.getScoreboardTags().contains("TargetDummy")) return;
        player.sendMessage(Component.text("Target Dummy hit for: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(event.getDamage())
                        .color(NamedTextColor.GOLD))
                .append(Component.text(" dmg")
                        .color(NamedTextColor.RED))
                .append(Component.text(".")
                        .color(NamedTextColor.GRAY)));
        event.setCancelled(true);
    }
}
