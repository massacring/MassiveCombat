package me.massacring.massiveCombat.combat.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerChangeHealthEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final double amount;
    private boolean cancelled = false;

    public PlayerChangeHealthEvent(Player player, double amount) {
        super(player);
        this.amount = amount;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public double getAmount() {
        return this.amount;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}