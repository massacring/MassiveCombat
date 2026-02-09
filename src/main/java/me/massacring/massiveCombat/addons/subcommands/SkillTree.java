package me.massacring.massiveCombat.addons.subcommands;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.commands.SubCommand;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SkillTree extends SubCommand {
    public SkillTree(MassiveCombat plugin, String parent, String name, String description) {
        super(plugin, parent, name, description);
    }

    @Override
    public String getSyntax() {
        return String.format("/%s %s <Player> <skillTreeNodeId> add|set <Level>", getParent(), getName());
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("massivecombat.command.admin")) {
            sender.sendMessage(Component.text("You don't have permission to run this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length != 5) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(Component.text("Target player is invalid. May be offline.").color(NamedTextColor.RED));
            return true;
        }

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        try {
            String operation = args[3];
            if (!List.of("add", "set").contains(operation)) {
                sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
                return true;
            }
            AtomicInteger level = new AtomicInteger(Integer.parseInt(args[4]));
            level.set(Math.max(level.get(), 0));

            playerData.getNodeStates().keySet().forEach(node -> {
                if (!node.getId().equalsIgnoreCase(args[2])) return;

                switch (operation) {
                    case "add":
                        level.set(Math.min(playerData.getNodeLevel(node) + level.get(), node.getMaxLevel()));
                    case "set":
                        level.set(Math.min(level.get(), node.getMaxLevel()));
                }
                playerData.setNodeLevel(node, level.get());
                playerData.setNodeState(node, NodeState.UNLOCKED);
            });
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
        }

        return true;
    }
}
