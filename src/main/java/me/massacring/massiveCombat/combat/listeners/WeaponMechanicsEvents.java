package me.massacring.massiveCombat.combat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.weaponmechanics.weapon.weaponevents.PrepareWeaponShootEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import net.kyori.adventure.text.Component;
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
        event.setBaseDamage(event.getBaseDamage() + blasterDamage);
    }

    @EventHandler
    public void weaponReloadEvent(WeaponReloadEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double blasterReload = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_reload_reduction%")
        );
        event.setReloadTime((int) (event.getReloadTime() - blasterReload));
    }

    @EventHandler
    public void weaponShootEvent(PrepareWeaponShootEvent event) {
        if (!(event.getShooter() instanceof Player player)) return;
        double blasterSpread = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_attribute_blaster_spread_reduction%")
        );
        event.setBaseSpread(event.getBaseSpread() - (blasterSpread/100));
    }
}
