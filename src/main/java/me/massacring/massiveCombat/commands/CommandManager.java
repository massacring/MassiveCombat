package me.massacring.massiveCombat.commands;

import me.massacring.massiveCombat.MassiveCombat;
import me.massacring.massiveCombat.addons.commands.subcommands.SkillTree;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class CommandManager implements CommandExecutor {
    private final HashMap<String, Callable<Boolean>> commands = new HashMap<>();
    private final HashMap<String, List<SubCommand>> subCommands = new HashMap<>();

    public CommandManager(MassiveCombat plugin){
        List<SubCommand> subCommandList = new ArrayList<>();
        if (plugin.getServer().getPluginManager().isPluginEnabled("MMOCore")) {
            subCommandList.add(new SkillTree(plugin, "combat", "skill-tree", "Lets you modify MMOCore skill-tree data."));
        }

        addCommand("combat", () -> false, subCommandList);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!commands.containsKey(label)) return false;
        if (args.length == 0) {
            try {
                return commands.get(label).call();
            } catch (Exception e) {
                return false;
            }
        }
        List<SubCommand> subCommandList = getSubcommands(label);
        for (SubCommand subCommand : subCommandList) {
            if (!args[0].equals(subCommand.getName())) continue;
            return subCommand.perform(commandSender, args);
        }
        StringBuilder arguments = new StringBuilder();
        for (String text : args) {
            arguments.append(" ").append(text);
        }
        commandSender.sendMessage(Component.text("Incorrect argument for command.").color(NamedTextColor.RED));
        commandSender.sendMessage(Component.text(label + " ").color(NamedTextColor.GRAY)
                .append(Component.text(arguments.toString().replaceFirst("^ *", "")).color(NamedTextColor.RED).decorate(TextDecoration.UNDERLINED))
                .append(Component.text("<--[HERE]").color(NamedTextColor.RED).decorate(TextDecoration.ITALIC)));
        return false;
    }

    private List<SubCommand> getSubcommands(String label) {
        return this.subCommands.get(label);
    }

    private void addCommand(String label, Callable<Boolean> command, List<SubCommand> subCommands) {
        this.commands.put(label, command);
        this.subCommands.put(label, subCommands);
    }
}
