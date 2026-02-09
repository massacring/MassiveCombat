package me.massacring.massiveCombat.commands.subcommands;

import me.massacring.massiveCombat.commands.SubCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class TargetDummy extends SubCommand {
    public TargetDummy(String parent, String name, String description) {
        super(parent, name, description);
    }

    private float snapToCardinal(float yaw) {
        yaw = (yaw % 360 + 360) % 360; // normalize to 0–360

        if (yaw >= 315 || yaw < 45) {
            return 0f;
        } else if (yaw < 135) {
            return 90f;
        } else if (yaw < 225) {
            return 180f;
        } else {
            return -90f;
        }
    }

    private ArmorStand summonTargetDummy(Location location) {
        Location dummyLocation = location.toBlockLocation().add(0.5, 0, 0.5);
        dummyLocation.setYaw(snapToCardinal(dummyLocation.getYaw()));

        World world = dummyLocation.getWorld();

        if (!world.getBlockAt(dummyLocation).getType().equals(Material.AIR)) return null;

        world.getBlockAt(dummyLocation).setType(Material.SPRUCE_FENCE);
        ArmorStand dummy = (ArmorStand) world.spawnEntity(dummyLocation, EntityType.ARMOR_STAND);

        dummy.getEquipment().setHelmet(new ItemStack(Material.HAY_BLOCK));
        ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        if (leatherArmorMeta != null) {
            leatherArmorMeta.setColor(Color.fromRGB(255, 187, 0));
            chestplate.setItemMeta(leatherArmorMeta);
        }
        dummy.getEquipment().setChestplate(chestplate);

        dummy.setGravity(false);
        dummy.setBasePlate(false);

        dummy.addScoreboardTag("TargetDummy");

        return dummy;
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return false;
        }
        Location location = player.getLocation();

        ArmorStand dummy = summonTargetDummy(location);
        if (dummy == null) {
            player.sendMessage(Component.text("Cannot place Target Dummy: Space occupied.").color(NamedTextColor.RED));
            return false;
        }

        player.sendMessage(Component.text("Summoning Target Dummy.").color(NamedTextColor.GOLD));
        return true;
    }
}
