package me.massacring.massiveCombat.commands;

import org.bukkit.entity.Player;

public abstract class SubCommand {
    private final String parent;
    private final String name;
    private final String description;
    public SubCommand(String parent, String name, String description) {
        this.parent = parent;
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSyntax() {
        return String.format("/%s %s", parent, name);
    }

    public abstract boolean perform(Player player, String[] args);
}
