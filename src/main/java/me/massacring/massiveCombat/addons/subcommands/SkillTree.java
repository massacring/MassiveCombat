package me.massacring.massiveCombat.addons.subcommands;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.commands.SubCommand;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.gui.skilltree.NodeIncrementResult;
import net.Indyuce.mmocore.skilltree.NodeState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SkillTree extends SubCommand {
    public SkillTree(MassiveCombat plugin, String parent, String name, String description) {
        super(plugin, parent, name, description);
    }

    @Override
    public String getSyntax() {
        return String.format("/%s %s <Player> increment <skillTreeNodeId>", getParent(), getName());
    }

    @Override
    public boolean perform(CommandSender sender, String[] args) {
        if (!sender.hasPermission("massivecombat.command.admin")) {
            sender.sendMessage(Component.text("You don't have permission to run this command.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length != 4) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
            return true;
        }

        Player player = Bukkit.getPlayerExact(args[1]);
        if (player == null) {
            sender.sendMessage(Component.text("Target player is invalid. May be offline.").color(NamedTextColor.RED));
            return true;
        }

        PlayerData playerData = PlayerData.get(player.getUniqueId());
        String operation = args[2];
        if (!Objects.equals("increment", operation)) {
            sender.sendMessage(Component.text("Incorrect arguments. " + getSyntax()).color(NamedTextColor.RED));
            return true;
        }

        playerData.getNodeStates().keySet().forEach(node -> {
            if (!node.getId().equalsIgnoreCase(args[3])) return;

            NodeIncrementResult result = playerData.canIncrementNodeLevel(node);

            switch (result) {
                case NodeIncrementResult.MAX_LEVEL_REACHED -> {
                    sender.sendMessage(Component.text("Attempted to upgrade a skill beyond its maximum level.").color(NamedTextColor.RED));
                    return;
                }
                case NodeIncrementResult.PERMISSION_DENIED -> {
                    sender.sendMessage(Component.text("You don't have permission to upgrade this skill.").color(NamedTextColor.RED));
                    return;
                }
            }

            playerData.setNodeState(node, NodeState.UNLOCKED);
            playerData.incrementNodeLevel(node);
        });

        return true;
    }
}
