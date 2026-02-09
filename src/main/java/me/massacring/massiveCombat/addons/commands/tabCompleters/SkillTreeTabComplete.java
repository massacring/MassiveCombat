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
        List<String> tabOptions = new ArrayList<>();

        switch (args.length) {
            case 1:
                tabOptions.addAll(plugin.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
            case 2:
                tabOptions.add("increment");
            case 3:
                tabOptions.addAll(MMOCore.plugin.skillTreeManager.getAllNodes().stream().map(SkillTreeNode::getName).toList());
        }

        return tabOptions;
    }
}
