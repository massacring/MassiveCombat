package me.massacring.massiveCombat.addons.listeners;

import me.deecaad.weaponmechanics.weapon.projectile.weaponprojectile.WeaponProjectile;
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileHitEntityEvent;
import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

import static me.massacring.massiveCombat.combat.Deflection.deflectAesthetics;
import static me.massacring.massiveCombat.combat.Deflection.deflectCheck;

public class WMDeflectionEvents implements Listener {
    private final List<String> whitelistTags;
    private final double minimumAngle;
    private final int power;
    private final Sound sound;
    private final boolean useCooldown;
    private final int cooldownTicks;
    private final boolean requiresBlocking;

    public WMDeflectionEvents() {
        FileConfiguration config = MassiveCombat.getInstance().getConfig();
        this.whitelistTags = config.getStringList("deflection_whitelist_tags");
        this.minimumAngle = config.getDouble("deflection_minimum_angle");
        this.power = config.getInt("deflection_power");

        String soundStr = config.getString("deflection_sound");
        if (soundStr == null) soundStr = "";
        NamespacedKey soundKey = NamespacedKey.fromString(soundStr);
        if (soundKey != null)
            this.sound = Registry.SOUNDS.get(soundKey);
        else
            this.sound = Sound.ENTITY_BREEZE_DEFLECT;

        this.useCooldown = config.getBoolean("deflection_use_cooldown");
        this.cooldownTicks = config.getInt("deflection_cooldown");
        this.requiresBlocking = config.getBoolean("deflection_requires_blocking");
    }

    @EventHandler
    public void deflectWMProjectile(ProjectileHitEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!player.hasPermission("massivecombat.ability.starter.deflect")) return;
        if (player.isBlocking()) return;
        WeaponProjectile projectile = event.getProjectile();

        if (!deflectCheck(MassiveCombat.getInstance(), player, projectile.getBukkitLocation().getDirection().normalize(), this.whitelistTags, this.minimumAngle, this.requiresBlocking)) return;

        deflectAesthetics(player, this.sound);

        // Reverse the arrow's direction.
        new BukkitRunnable() {
            @Override
            public void run() {
                projectile.setMotion(player.getEyeLocation().getDirection().multiply(power));
            }
        }.runTaskLater(MassiveCombat.getInstance(), 1);

        event.setCancelled(true);

        // set deflection cooldown tag
        long cooldownTime = System.currentTimeMillis() + (this.useCooldown ? (this.cooldownTicks * 50L) : 0);
        PersistentDataContainer playerNBT = player.getPersistentDataContainer();
        playerNBT.set(new NamespacedKey(MassiveCombat.getInstance(), "massivecombat.deflect.cooldown"), PersistentDataType.LONG, cooldownTime);
    }
}
