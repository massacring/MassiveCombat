package me.massacring.massiveCombat.combat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsEvents implements Listener {
    @EventHandler
    public void weaponShootEvent(WeaponDamageEntityEvent event) {
        if (!(event.getSource().getShooter() instanceof Player player)) return;
        double blasterDamage = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_damage%")
        );
        event.setBaseDamage(event.getBaseDamage() + blasterDamage);
    }
}
