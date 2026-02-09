package me.massacring.massiveCombat.addons.commands.tabCompleters;

import me.massacring.massiveCombat.MassiveCombat;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skilltree.SkillTreeNode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SkillTreeTabComplete {
    public static List<String> getTabComplete(MassiveCombat plugin, @NotNull String @NotNull [] args) {
        return switch (args.length) {
            case 2 -> plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            case 3 -> List.of("increment");
            case 4 -> MMOCore.plugin.skillTreeManager.getAllNodes().stream().map(SkillTreeNode::getId).toList();
            default -> new ArrayList<>();
        };
    }
}
