package me.massacring.massiveCombat.commands;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {
    private final String parent;
    private final String name;
    private final String description;
    public SubCommand(MassiveCombat plugin, String parent, String name, String description) {
        this.parent = parent;
        this.name = name;
        this.description = description;
        plugin.getLogger().info("Registered subcommand: " + getSyntax());
    }

    public String getParent() { return parent; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSyntax() {
        return String.format("/%s %s", parent, name);
    }

    public abstract boolean perform(CommandSender commandSender, String[] args);
}
