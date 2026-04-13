package me.massacring.massiveCombat.addons.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.damage.WeaponDamageType;
import me.deecaad.weaponmechanics.weapon.weaponevents.PrepareWeaponShootEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponDamageEntityEvent;
import me.deecaad.weaponmechanics.weapon.weaponevents.WeaponReloadEvent;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.massacring.massiveCombat.MassiveCombat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.time.Duration;

public class MMOCoreWMEvents implements Listener {
    private final MassiveCombat plugin;

    public MMOCoreWMEvents(MassiveCombat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void weaponDamageEvent(WeaponDamageEntityEvent event) {
        if (!(event.getSource().getShooter() instanceof Player player)) return;
        WeaponDamageType damageType = event.getSource().getDamageType();
        String accuracy_placeholder = "";
        String flat_damage_placeholder = "";
        String percent_damage_placeholder = "";
        if (damageType == WeaponDamageType.PROJECTILE) {
            accuracy_placeholder = "%mmocore_stat_BLASTER_ACCURACY%";
            flat_damage_placeholder = "%mmocore_stat_BLASTER_FLAT_DMG%";
            percent_damage_placeholder = "%mmocore_stat_BLASTER_PERCENT_DMG%";
        } else if (damageType == WeaponDamageType.MELEE) {
            accuracy_placeholder = "%mmocore_stat_MELEE_ACCURACY%";
            flat_damage_placeholder = "%mmocore_stat_MELEE_FLAT_DMG%";
            percent_damage_placeholder = "%mmocore_stat_MELEE_PERCENT_DMG%";
            plugin.getLogger().info("Melee attack.");
        } else if (damageType == WeaponDamageType.EXPLOSION) {
            accuracy_placeholder = "%mmocore_stat_EXPLOSIVE_ACCURACY%";
            flat_damage_placeholder = "%mmocore_stat_EXPLOSIVE_FLAT_DMG%";
            percent_damage_placeholder = "%mmocore_stat_EXPLOSIVE_PERCENT_DMG%";
        }
        if (accuracy_placeholder.isEmpty())
            return;

        double accuracy = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, accuracy_placeholder)
        );

        boolean miss = Math.random() * 100 > accuracy;
        if (miss) {
            showMissTitle(player);
            event.setCancelled(true);
            return;
        }

        double flatDamage = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, flat_damage_placeholder)
        );
        double damagePercent = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, percent_damage_placeholder)
        );

        double baseDamage = event.getBaseDamage();
        double newDamage = baseDamage + flatDamage;
        newDamage += baseDamage * (damagePercent/100);

        if (event.getPoint() == DamagePoint.HEAD) {
            double headshotDamage = Double.parseDouble(
                    PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_HEADSHOT_DMG%")
            );
            newDamage *= (headshotDamage/100 + 1);
        }

        double distance = player.getLocation().distance(event.getEntity().getLocation());
        if (distance < 5) {
            double damage = Double.parseDouble(
                    PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_CLOSE_DMG%")
            );
            newDamage *= (damage/100 + 1);
        } else if (distance > 15) {
            double damage = Double.parseDouble(
                    PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_FAR_DMG%")
            );
            newDamage *= (damage/100 + 1);
        }

        if (damageType == WeaponDamageType.MELEE) {
            double control = Double.parseDouble(
                    PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_MELEE_CONTROL%")
            );
            boolean control_fail = Math.random() * 100 > control;
            if (control_fail) {
                double controlEfficiency = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_MELEE_CONTROL_EFFICIENCY%")
                );
                showControlFailTitle(player);
                player.damage(newDamage * (controlEfficiency/100));
            }
        }

        event.setBaseDamage(Math.max(newDamage, 0));
    }

    @EventHandler
    public void weaponReloadEvent(WeaponReloadEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        double blasterReload = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_FLAT_RELOAD_RDC%")
        );
        double blasterReloadPercent = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_PERCENT_RELOAD_RDC%")
        );
        double baseReload = event.getReloadTime() - blasterReload;
        double newReload = baseReload - (baseReload * blasterReloadPercent/100);
        event.setReloadTime((int) Math.max(newReload, 0));
    }

    @EventHandler
    public void weaponShootEvent(PrepareWeaponShootEvent event) {
        if (!(event.getShooter() instanceof Player player)) return;

        double baseSpread = event.getBaseSpread();

        EntityWrapper playerWrapper = WeaponMechanics.getInstance().getEntityWrapper(player, true);
        double newSpread = baseSpread;
        if (playerWrapper != null) {
            if (playerWrapper.isInMidair()) {
                double blasterSpreadMid = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_PERCENT_SPREAD_MID%")
                );
                newSpread *= blasterSpreadMid / 100;
            }
            if (playerWrapper.isZooming()) {
                double blasterSpreadScoped = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_PERCENT_SPREAD_SCOPED%")
                );
                newSpread *= blasterSpreadScoped / 100;
            }
            else {
                double blasterSpreadHip = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_PERCENT_SPREAD_HIP%")
                );
                newSpread *= blasterSpreadHip / 100;
            }
        }
        event.setBaseSpread(Math.max(newSpread, 0));

        double blasterRecoil = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_RECOIL%")
        );
        double baseRecoilYaw = event.getRecoilYaw();
        double baseRecoilPitch = event.getRecoilPitch();
        double newRecoilYaw = baseRecoilYaw * (blasterRecoil/100);
        double newRecoilPitch = baseRecoilPitch * (blasterRecoil/100);
        event.setRecoilYaw(newRecoilYaw);
        event.setRecoilPitch(newRecoilPitch);
    }

    private void showMissTitle(Player player) {
        final Component actionBar = Component.text("Miss!", NamedTextColor.RED).decorate(TextDecoration.ITALIC);
        final Title.Times times = Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1000), Duration.ofMillis(500));
        final Title title = Title.title(Component.empty(), actionBar, times);
        player.showTitle(title);
    }

    private void showControlFailTitle(Player player) {
        final Component actionBar = Component.text("Your fingers slipped.", NamedTextColor.RED).decorate(TextDecoration.ITALIC);
        final Title.Times times = Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(1000), Duration.ofMillis(500));
        final Title title = Title.title(Component.empty(), actionBar, times);
        player.showTitle(title);
    }
}
