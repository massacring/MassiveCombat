package me.massacring.massiveCombat.combat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.PrepareWeaponShootEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WeaponMechanicsEvents implements Listener {
    @EventHandler
    public void weaponDamageEvent(WeaponDamageEntityEvent event) {
        if (!(event.getSource().getShooter() instanceof Player player)) return;
        double blasterDamage = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_damage%")
        );
        double blasterDamagePercent = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_damage_percent%")
        );
        double baseDamage = event.getBaseDamage() + blasterDamage;
        double newDamage = baseDamage + (baseDamage * blasterDamagePercent/100);
        event.setBaseDamage(Math.max(newDamage, 0));
    }

    @EventHandler
    public void weaponReloadEvent(WeaponReloadEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double blasterReload = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_reload_reduction%")
        );
        double blasterReloadPercent = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_reload_reduction_percent%")
        );
        double baseReload = event.getReloadTime() - blasterReload;
        double newReload = baseReload - (baseReload * blasterReloadPercent/100);
        event.setReloadTime((int) Math.max(newReload, 0));
    }

    @EventHandler
    public void weaponShootEvent(PrepareWeaponShootEvent event) {
        if (!(event.getShooter() instanceof Player player)) return;
        double blasterSpread = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_spread_reduction%")
        );
        double baseSpread = event.getBaseSpread();
        double newSpread = baseSpread - (baseSpread * (blasterSpread/100));
        event.setBaseSpread(Math.max(newSpread, 0));
    }
}
