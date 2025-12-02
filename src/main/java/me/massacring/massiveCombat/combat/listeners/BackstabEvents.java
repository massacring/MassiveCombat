package me.massacring.massiveCombat.combat.listeners;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

public class BackstabEvents implements Listener {
    private final MassiveCombat plugin;
    private final double bonusDamage;
    private final double minimumAngle;
    private final String sound;

    public BackstabEvents(MassiveCombat plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        this.bonusDamage = config.getDouble("backstab_damage");
        this.minimumAngle = config.getDouble("backstab_angle");
        this.sound = config.getString("backstab_sound");
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        Vector playerDirection = getHorizontalDirection(player);
        Vector entityDirection = getHorizontalDirection(entity);
        double dotProduct =  playerDirection.dot(entityDirection);
        double angleInDegrees = Math.toDegrees(Math.acos(dotProduct));
        if (angleInDegrees > this.minimumAngle) return;
        event.setDamage(event.getDamage() + bonusDamage);
        player.getWorld().playSound(player.getLocation(), Sound.valueOf(sound), SoundCategory.PLAYERS, 0.5f, 0.5f);
        double range = 0.8;
        double randX = (-range) + Math.random() * (range - (-range));
        double randY = (-range) + Math.random() * (range - (-range));
        double randZ = (-range) + Math.random() * (range - (-range));
        entity.getWorld().spawnParticle(Particle.BLOCK, entity.getLocation().add(entityDirection).add(new Vector(0, 1, 0)),
                20, randX, randY, randZ, Material.NETHER_WART_BLOCK.createBlockData());
    }

    private static Vector getHorizontalDirection(Entity entity) {
        Vector direction = entity.getLocation().getDirection();
        direction.setY(0);
        direction.normalize();
        return direction;
    }
}