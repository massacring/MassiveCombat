package me.massacring.massiveCombat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CustomTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> tabOptions = new ArrayList<>();
        if (commandSender instanceof Player player) {
            if (!player.hasPermission("massivecombat.command.admin")) {
                return tabOptions;
            }
            if (args.length == 1) {
                tabOptions.add("subcommand");
            }
        }
        return tabOptions;
    }
}
