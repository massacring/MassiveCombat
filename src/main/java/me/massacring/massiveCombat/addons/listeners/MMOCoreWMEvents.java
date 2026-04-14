package me.massacring.massiveCombat.addons.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import me.deecaad.weaponmechanics.WeaponMechanics;
import me.deecaad.weaponmechanics.weapon.damage.DamagePoint;
import me.deecaad.weaponmechanics.weapon.damage.WeaponDamageType;
import me.deecaad.weaponmechanics.weapon.explode.Explosion;
import me.deecaad.weaponmechanics.weapon.explode.shapes.*;
import me.deecaad.weaponmechanics.weapon.weaponevents.*;
import me.deecaad.weaponmechanics.wrappers.EntityWrapper;
import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.files.CustomConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMOCoreWMEvents implements Listener {
    private final MassiveCombat plugin;
    private final FileConfiguration weaponGroups;

    public MMOCoreWMEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        weaponGroups = CustomConfig.getFile(plugin, "weapon_groups.yml");
    }

    @EventHandler
    public void blasterDamageEvent(WeaponDamageEntityEvent event) {
        if (!(event.getSource().getShooter() instanceof Player player)) return;
        WeaponDamageType damageType = event.getSource().getDamageType();
        if (damageType != WeaponDamageType.PROJECTILE) return;

        ConfigurationSection blasterSection = weaponGroups.getConfigurationSection("Blasters");
        if (blasterSection == null) return;

        boolean isGrouped = false;
        String weaponID = event.getWeaponTitle();
        for (String key : blasterSection.getKeys(false)) {
            List<String> blasters = blasterSection.getStringList(key);
            if (blasters.contains(weaponID)) {
                isGrouped = true;
                break;
            }
        }
        if (!isGrouped) return;

        String accuracy_placeholder = "%mmocore_stat_BLASTER_ACCURACY%";
        String flat_damage_placeholder = "%mmocore_stat_BLASTER_FLAT_DMG%";
        String percent_damage_placeholder = "%mmocore_stat_BLASTER_PERCENT_DMG%";

        double baseDamage = event.getBaseDamage();
        double newDamage = damageEvent(baseDamage, player, accuracy_placeholder, flat_damage_placeholder, percent_damage_placeholder);
        if (baseDamage == newDamage) {
            event.setCancelled(true);
            return;
        }

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

        newDamage = blasterDamageEvents(weaponID, newDamage, player, event.getPoint() == DamagePoint.HEAD);

        event.setBaseDamage(Math.max(newDamage, 0));
    }

    @EventHandler
    public void explosiveDamageEvent(WeaponDamageEntityEvent event) {
        if (!(event.getSource().getShooter() instanceof Player player)) return;
        WeaponDamageType damageType = event.getSource().getDamageType();
        if (damageType != WeaponDamageType.EXPLOSION) return;

        ConfigurationSection blasterSection = weaponGroups.getConfigurationSection("Explosives");
        if (blasterSection == null) return;

        boolean isGrouped = false;
        String weaponID = event.getWeaponTitle();
        for (String key : blasterSection.getKeys(false)) {
            List<String> blasters = blasterSection.getStringList(key);
            if (blasters.contains(weaponID)) {
                isGrouped = true;
                break;
            }
        }
        if (!isGrouped) return;

        String accuracy_placeholder = "%mmocore_stat_EXPLOSIVE_ACCURACY%";
        String flat_damage_placeholder = "%mmocore_stat_EXPLOSIVE_FLAT_DMG%";
        String percent_damage_placeholder = "%mmocore_stat_EXPLOSIVE_PERCENT_DMG%";

        double baseDamage = event.getBaseDamage();
        double newDamage = damageEvent(baseDamage, player, accuracy_placeholder, flat_damage_placeholder, percent_damage_placeholder);
        if (baseDamage == newDamage) {
            event.setCancelled(true);
            return;
        }

        event.setBaseDamage(Math.max(newDamage, 0));
    }

    @EventHandler
    public void meleeDamageEvent(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        DamageType damageType = event.getDamageSource().getDamageType();
        if (damageType != DamageType.PLAYER_ATTACK && damageType != DamageType.MACE_SMASH) return;

        String accuracy_placeholder = "%mmocore_stat_MELEE_ACCURACY%";
        String flat_damage_placeholder = "%mmocore_stat_MELEE_FLAT_DMG%";
        String percent_damage_placeholder = "%mmocore_stat_MELEE_PERCENT_DMG%";

        double baseDamage = event.getDamage();
        double newDamage = damageEvent(baseDamage, player, accuracy_placeholder, flat_damage_placeholder, percent_damage_placeholder);
        if (newDamage == baseDamage) {
            event.setCancelled(true);
            return;
        }

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

        event.setDamage(newDamage);
    }

    private double blasterDamageEvents(String weaponID, double baseDamage, Player player, boolean headshot) {
        ConfigurationSection blasterSection = weaponGroups.getConfigurationSection("Blasters");
        if (blasterSection == null) return baseDamage;

        try {
            for (String key : blasterSection.getKeys(false)) {
                List<String> blasters = blasterSection.getStringList(key);
                if (blasters.contains(weaponID)) {
                    String flat_damage_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_FLAT_DMG", key.toUpperCase()) + "%";
                    String percent_damage_placeholder = "%" + String.format("mmocore_stat_%s_PERCENT_DMG", key.toUpperCase()) + "%";

                    double flatDamage = Double.parseDouble(
                            PlaceholderAPI.setPlaceholders(player, flat_damage_placeholder)
                    );
                    double damagePercent = Double.parseDouble(
                            PlaceholderAPI.setPlaceholders(player, percent_damage_placeholder)
                    );

                    double newDamage = baseDamage + flatDamage;
                    newDamage += baseDamage * (damagePercent/100);

                    if (headshot) {
                        double headshotDamage = Double.parseDouble(
                                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_HEADSHOT_DMG%")
                        );
                        newDamage *= (headshotDamage/100 + 1);
                    }

                    return newDamage;
                }
            }
        } catch (Exception e) {
            return baseDamage;
        }

        return baseDamage;
    }

    private double damageEvent(double baseDamage, Player player, String accuracy_placeholder, String flat_damage_placeholder, String percent_damage_placeholder) {
        double accuracy = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, accuracy_placeholder)
        );

        boolean miss = Math.random() * 100 > accuracy;
        if (miss) {
            showMissTitle(player);
            return baseDamage;
        }

        double flatDamage = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, flat_damage_placeholder)
        );
        double damagePercent = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, percent_damage_placeholder)
        );

        double newDamage = baseDamage + flatDamage;
        newDamage += baseDamage * (damagePercent/100);

        return newDamage;
    }

    @EventHandler
    public void explosionEvent(ProjectilePreExplodeEvent event) {
        Explosion oldExplosion = event.getExplosion();
        if (!(event.getShooter() instanceof Player player)) return;

        double bonusRadius = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_EXPLOSIVE_RADIUS%")
        );
        bonusRadius /= 100;
        bonusRadius += 1;

        ExplosionShape oldShape = oldExplosion.getShape();
        ExplosionShape newShape;
        Map<String, String> shapeData = parseShape(oldShape);
        switch (oldShape) {
            case SphereExplosion ignored -> {
                double radius = Double.parseDouble(shapeData.get("radius"));
                radius *= bonusRadius;
                newShape = new SphereExplosion(radius);
            }
            case CubeExplosion ignored -> {
                double width = Double.parseDouble(shapeData.get("width"));
                double height = Double.parseDouble(shapeData.get("height"));
                width *= bonusRadius;
                height *= bonusRadius;
                newShape = new CubeExplosion(width, height);
            }
            default -> {
                return;
            }
        }

        Explosion newExplosion = new Explosion(newShape, oldExplosion.getExposure(), oldExplosion.getBlockDamage(),
                oldExplosion.getRegeneration(), oldExplosion.getDetonation(), oldExplosion.getBlockChance(), oldExplosion.getKnockbackRate(),
                oldExplosion.getCluster(), oldExplosion.getAirStrike(), oldExplosion.getFlashbang(), oldExplosion.getMechanics());
        event.setExplosion(newExplosion);
    }

    @EventHandler
    public void blasterReloadEvent(WeaponReloadEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ConfigurationSection blasterSection = weaponGroups.getConfigurationSection("Blasters");
        if (blasterSection == null) return;

        double blasterReload = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_RELOAD_RDC%")
        );
        double baseReload = event.getReloadTime();
        double newReload = baseReload - (baseReload * blasterReload/100);

        try {
            String weaponID = event.getWeaponTitle();
            for (String key : blasterSection.getKeys(false)) {
                List<String> blasters = blasterSection.getStringList(key);
                if (blasters.contains(weaponID)) {
                    String reload_rdc_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_RELOAD_RDC", key.toUpperCase()) + "%";

                    double weaponReload = Double.parseDouble(
                            PlaceholderAPI.setPlaceholders(player, reload_rdc_placeholder)
                    );

                    newReload = newReload - (newReload * weaponReload/100);
                }
            }
        } catch (Exception ignored) {}

        event.setReloadTime((int) Math.max(newReload, 0));
    }

    @EventHandler
    public void blasterShootEvent(PrepareWeaponShootEvent event) {
        if (!(event.getShooter() instanceof Player player)) return;

        ConfigurationSection blasterSection = weaponGroups.getConfigurationSection("Blasters");
        if (blasterSection == null) return;

        double baseSpread = event.getBaseSpread();

        EntityWrapper playerWrapper = WeaponMechanics.getInstance().getEntityWrapper(player, true);
        double newSpread = baseSpread;
        if (playerWrapper != null) {
            if (playerWrapper.isInMidair()) {
                double blasterSpreadMid = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_SPREAD_MID%")
                );
                newSpread *= blasterSpreadMid / 100;
            }
            if (playerWrapper.isZooming()) {
                double blasterSpreadScoped = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_SPREAD_SCOPED%")
                );
                newSpread *= blasterSpreadScoped / 100;
            }
            else {
                double blasterSpreadHip = Double.parseDouble(
                        PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_SPREAD_HIP%")
                );
                newSpread *= blasterSpreadHip / 100;
            }
        }

        try {
            String weaponID = event.getWeaponTitle();
            for (String key : blasterSection.getKeys(false)) {
                List<String> blasters = blasterSection.getStringList(key);
                if (blasters.contains(weaponID)) {
                    if (playerWrapper != null) {
                        if (playerWrapper.isInMidair()) {
                            String spread_mid_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_RELOAD_RDC", key.toUpperCase()) + "%";
                            double blasterSpreadMid = Double.parseDouble(
                                    PlaceholderAPI.setPlaceholders(player, spread_mid_placeholder)
                            );
                            newSpread *= blasterSpreadMid / 100;
                        }
                        if (playerWrapper.isZooming()) {
                            String spread_scoped_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_SPREAD_SCOPED", key.toUpperCase()) + "%";
                            double blasterSpreadScoped = Double.parseDouble(
                                    PlaceholderAPI.setPlaceholders(player, spread_scoped_placeholder)
                            );
                            newSpread *= blasterSpreadScoped / 100;
                        }
                        else {
                            String spread_hip_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_SPREAD_HIP", key.toUpperCase()) + "%";
                            double blasterSpreadHip = Double.parseDouble(
                                    PlaceholderAPI.setPlaceholders(player, spread_hip_placeholder)
                            );
                            newSpread *= blasterSpreadHip / 100;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        event.setBaseSpread(Math.max(newSpread, 0));

        double blasterRecoil = Double.parseDouble(
                PlaceholderAPI.setPlaceholders(player, "%mmocore_stat_BLASTER_RECOIL%")
        );
        double baseRecoilYaw = event.getRecoilYaw();
        double baseRecoilPitch = event.getRecoilPitch();
        double newRecoilYaw = baseRecoilYaw * (blasterRecoil/100);
        double newRecoilPitch = baseRecoilPitch * (blasterRecoil/100);

        try {
            String weaponID = event.getWeaponTitle();
            for (String key : blasterSection.getKeys(false)) {
                List<String> blasters = blasterSection.getStringList(key);
                if (blasters.contains(weaponID)) {
                    String recoil_placeholder = "%" + String.format("mmocore_stat_BLASTER_%s_RECOIL", key.toUpperCase()) + "%";

                    double weaponRecoil = Double.parseDouble(
                            PlaceholderAPI.setPlaceholders(player, recoil_placeholder)
                    );

                    newRecoilYaw *= weaponRecoil/100;
                    newRecoilPitch *= weaponRecoil/100;
                }
            }
        } catch (Exception ignored) {}

        event.setRecoilYaw(newRecoilYaw);
        event.setRecoilPitch(newRecoilPitch);
    }

    private Map<String, String> parseShape(ExplosionShape shape) {
        Map<String, String> map = new HashMap<>();

        String input = shape.toString();

        int start = input.indexOf('{');
        int end = input.indexOf('}');

        if (start == -1 || end == -1 || start >= end) {
            return map;
        }

        String content = input.substring(start + 1, end);
        String[] pairs = content.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return map;
    }

    private void showMissTitle(Player player) {
        final Component actionBar = Component.text("Miss!", NamedTextColor.RED).decorate(TextDecoration.ITALIC);
        final Title.Times times = Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofMillis(250));
        final Title title = Title.title(Component.empty(), actionBar, times);
        player.showTitle(title);
    }

    private void showControlFailTitle(Player player) {
        final Component actionBar = Component.text("Your fingers slipped.", NamedTextColor.RED).decorate(TextDecoration.ITALIC);
        final Title.Times times = Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(500), Duration.ofMillis(250));
        final Title title = Title.title(Component.empty(), actionBar, times);
        player.showTitle(title);
    }
}
