package com.sausaliens.SSJEManagers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import com.sausaliens.SSJEssentials;

public class LocationManager {
    private final File warpsFile;
    private FileConfiguration warpsConfig;

    private final SSJEssentials ssjEssentials;

    public LocationManager(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
        this.warpsFile = new File(ssjEssentials.getDataFolder(), "warps.yml");
        loadConfigs();
    }

    private void loadConfigs() {
        if (!warpsFile.exists()) {
            warpsConfig = new YamlConfiguration();
            warpsConfig.createSection("warps");
            saveWarps();
        } else {
            warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        }
    }

    public void saveWarps() {
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setWarp(String name, Location location, String description) {
        String path = "warps." + name;
        warpsConfig.set(path + ".x", location.getX());
        warpsConfig.set(path + ".y", location.getY());
        warpsConfig.set(path + ".z", location.getZ());
        warpsConfig.set(path + ".yaw", location.getYaw());
        warpsConfig.set(path + ".pitch", location.getPitch());
        warpsConfig.set(path + ".world", location.getWorld().getName());
        warpsConfig.set(path + ".description", description);
        saveWarps();
    }

    private File getPlayerHomeFile(Player player) {
        return new File(ssjEssentials.getDataFolder(), "PlayerData/" + player.getName() + ".yml");
    }

    private FileConfiguration getPlayerHomeConfig(Player player) {
        File file = getPlayerHomeFile(player);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private void savePlayerHomeConfig(Player player, FileConfiguration config) {
        try {
            config.save(getPlayerHomeFile(player));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("ssjessentials.admin")) {
            return ssjEssentials.getConfig().getInt("home.max-homes-admin", 1000);
        } else if (player.hasPermission("ssjessentials.mod")) {
            return ssjEssentials.getConfig().getInt("home.max-homes-mod", 20);
        }
        return ssjEssentials.getConfig().getInt("home.max-homes", 3);
    }

    public void setHome(Player player, String name, Location location) {
        FileConfiguration config = getPlayerHomeConfig(player);
        Set<String> homes = config.getConfigurationSection("homes") != null ? 
            config.getConfigurationSection("homes").getKeys(false) : new HashSet<>();
        
        if (homes.size() >= getMaxHomes(player) && !homes.contains(name)) {
            player.sendMessage(ssjEssentials.getCommandExecutor().formatMessage("§cYou have reached your maximum number of homes!"));
            return;
        }

        String path = "homes." + name;
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        config.set(path + ".world", location.getWorld().getName());
        savePlayerHomeConfig(player, config);
    }

    public Location getWarp(String name) {
        String path = "warps." + name;
        if (!warpsConfig.contains(path)) return null;
        return new Location(
            org.bukkit.Bukkit.getWorld(warpsConfig.getString(path + ".world")),
            warpsConfig.getDouble(path + ".x"),
            warpsConfig.getDouble(path + ".y"),
            warpsConfig.getDouble(path + ".z"),
            (float) warpsConfig.getDouble(path + ".yaw"),
            (float) warpsConfig.getDouble(path + ".pitch")
        );
    }

    public Location getHome(Player player, String name) {
        FileConfiguration config = getPlayerHomeConfig(player);
        String path = "homes." + name;
        if (!config.contains(path)) return null;
        
        return new Location(
            org.bukkit.Bukkit.getWorld(config.getString(path + ".world")),
            config.getDouble(path + ".x"),
            config.getDouble(path + ".y"),
            config.getDouble(path + ".z"),
            (float) config.getDouble(path + ".yaw"),
            (float) config.getDouble(path + ".pitch")
        );
    }

    public Set<String> getWarps() {
        return warpsConfig.getConfigurationSection("warps").getKeys(false);
    }

    public Set<String> getHomes(Player player) {
        FileConfiguration config = getPlayerHomeConfig(player);
        return config.getConfigurationSection("homes") != null ? 
            config.getConfigurationSection("homes").getKeys(false) : new HashSet<>();
    }

    public void deleteWarp(String name) {
        warpsConfig.set("warps." + name, null);
        saveWarps();
    }

    public void deleteHome(Player player, String name) {
        FileConfiguration config = getPlayerHomeConfig(player);
        config.set("homes." + name, null);
        savePlayerHomeConfig(player, config);
    }
} 