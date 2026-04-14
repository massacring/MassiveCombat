package me.massacring.massiveCombat.files;

import me.massacring.massiveCombat.MassiveCombat;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfig {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static FileConfiguration getFile(MassiveCombat plugin, String path) {
        File file = new File(plugin.getDataFolder(), path);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning(String.format("Config file '%s' could not be created.", path));
            }
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveFile(MassiveCombat plugin, String path, FileConfiguration customFile) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.getLogger().warning(String.format("Cannot save file '%s' as it does not exist.", path));
            return;
        }

        try {
            customFile.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning(String.format("Failed to save file '%s'.", path));
        }
    }
}
