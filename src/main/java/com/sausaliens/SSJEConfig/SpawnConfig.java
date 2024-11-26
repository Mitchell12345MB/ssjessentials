package com.sausaliens.SSJEConfig;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.sausaliens.SSJEssentials;
import java.io.File;
import java.io.IOException;

public class SpawnConfig {
    private final SSJEssentials plugin;
    private final File configFile;
    private FileConfiguration config;
    private Location spawnLocation;

    public SpawnConfig(SSJEssentials plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadConfig();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            try {
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create spawn.yml!");
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        loadSpawnLocation();
    }

    private void loadSpawnLocation() {
        if (config.contains("spawn")) {
            spawnLocation = (Location) config.get("spawn");
        } else {
            // Inherit from server's spawn location
            spawnLocation = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location location) {
        spawnLocation = location;
        config.set("spawn", location);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn location!");
        }
    }
} 