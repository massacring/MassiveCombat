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
        List<String> empty = new ArrayList<>();
        if (!(commandSender instanceof Player player)) return empty;
        if (!player.hasPermission("massivecombat.command.admin")) return empty;
        if (!command.getName().equalsIgnoreCase("combat")) return empty;

        boolean MMOCoreEnabled = plugin.getServer().getPluginManager().isPluginEnabled("MMOCore");

        switch (args.length) {
            case 1:
                if (MMOCoreEnabled) return List.of("skill-tree");
            case 2:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    return SkillTreeTabComplete.getTabComplete(plugin, args);
                }
            case 3:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    return SkillTreeTabComplete.getTabComplete(plugin, args);
                }
            case 4:
                if (MMOCoreEnabled && args[0].equals("skill-tree")) {
                    return SkillTreeTabComplete.getTabComplete(plugin, args);
                }
        }

        return empty;
    }
}
