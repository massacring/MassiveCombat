package me.massacring.massiveCombat.addons.subcommands;

import me.massacring.massiveCombat.commands.SubCommand;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillTree extends SubCommand {
    public SkillTree(String parent, String name, String description) {
        super(parent, name, description);
    }

    @Override
    public String getSyntax() {
        return String.format("/%s %s <Player> <skillTreeNodeId> <Level>", getParent(), getName());
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("massivecombat.command.admin")) {
            sender.sendMessage(Component.text("You don't have permission to run this command.").color(NamedTextColor.RED));
            return false;
        }

        if (args.length != 4) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
            return false;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(Component.text("Target player is invalid. May be offline.").color(NamedTextColor.RED));
            return false;
        }

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        try {
            int level = Integer.parseInt(args[3]);

            playerData.getNodeStates().keySet().forEach(node -> {
                if (node.getId().equalsIgnoreCase(args[2])) {
                    playerData.setNodeState(node, NodeState.UNLOCKED);
                    playerData.setNodeLevel(node, level);
                }
            });
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
        }

        return true;
    }
}
