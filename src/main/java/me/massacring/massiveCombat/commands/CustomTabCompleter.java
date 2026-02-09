package me.massacring.massiveCombat.commands;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.addons.commands.tabCompleters.SkillTreeTabComplete;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CustomTabCompleter implements TabCompleter {

    private final MassiveCombat plugin;

    public CustomTabCompleter(MassiveCombat plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> tabOptions = new ArrayList<>();
        if (!(commandSender instanceof Player player)) return tabOptions;
        if (!player.hasPermission("massivecombat.command.admin")) return tabOptions;
        if (!command.getName().equalsIgnoreCase("combat")) return tabOptions;

        boolean MMOCoreEnabled = plugin.getServer().getPluginManager().isPluginEnabled("MMOCore");

        switch (args.length) {
            case 0:
                if (MMOCoreEnabled) tabOptions.add("skill-tree");
            case 1:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    tabOptions.addAll(SkillTreeTabComplete.getTabComplete(plugin, args));
                }
            case 2:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    tabOptions.addAll(SkillTreeTabComplete.getTabComplete(plugin, args));
                }
            case 3:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    tabOptions.addAll(SkillTreeTabComplete.getTabComplete(plugin, args));
                }
        }

        return tabOptions;
    }
}
