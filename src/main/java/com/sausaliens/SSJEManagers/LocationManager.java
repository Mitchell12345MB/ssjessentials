package com.sausaliens.SSJEManagers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import com.sausaliens.SSJEssentials;

public class LocationManager {
    private final File warpsFile;
    private FileConfiguration warpsConfig;
    
    // Cache for frequently accessed warps to reduce disk I/O
    private final Map<String, Location> warpCache = new ConcurrentHashMap<>();
    
    // Cache for home locations to reduce disk I/O
    private final Map<String, Map<String, Location>> homeCache = new ConcurrentHashMap<>();
    
    // Track if cache needs to be flushed to disk
    private boolean warpsDirty = false;

    private final SSJEssentials ssjEssentials;

    /**
     * Manages warp and home locations for the server
     * 
     * @param ssjEssentials The main plugin instance
     */
    public LocationManager(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
        this.warpsFile = new File(ssjEssentials.getDataFolder(), "warps.yml");
        loadConfigs();
        
        // Schedule regular saving of data if changes are made
        ssjEssentials.getServer().getScheduler().runTaskTimer(ssjEssentials, () -> {
            if (warpsDirty) {
                saveWarps();
                warpsDirty = false;
            }
        }, 6000L, 6000L); // Save every 5 minutes (20 ticks * 60 seconds * 5)
    }

    /**
     * Loads configurations from disk
     */
    private void loadConfigs() {
        if (!warpsFile.exists()) {
            warpsConfig = new YamlConfiguration();
            warpsConfig.createSection("warps");
            saveWarps();
        } else {
            warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
            
            // Populate warp cache
            if (warpsConfig.contains("warps")) {
                for (String warpName : warpsConfig.getConfigurationSection("warps").getKeys(false)) {
                    try {
                        String path = "warps." + warpName;
                        Location location = new Location(
                            org.bukkit.Bukkit.getWorld(warpsConfig.getString(path + ".world")),
                            warpsConfig.getDouble(path + ".x"),
                            warpsConfig.getDouble(path + ".y"),
                            warpsConfig.getDouble(path + ".z"),
                            (float) warpsConfig.getDouble(path + ".yaw"),
                            (float) warpsConfig.getDouble(path + ".pitch")
                        );
                        warpCache.put(warpName, location);
                    } catch (Exception e) {
                        ssjEssentials.getLogger().log(Level.WARNING, "Failed to load warp: " + warpName, e);
                    }
                }
            }
        }
    }

    /**
     * Saves warps to disk
     */
    public void saveWarps() {
        ssjEssentials.getServer().getScheduler().runTaskAsynchronously(ssjEssentials, () -> {
            try {
                warpsConfig.save(warpsFile);
            } catch (IOException e) {
                ssjEssentials.getLogger().log(Level.SEVERE, "Could not save warps file", e);
            }
        });
    }

    /**
     * Sets a warp location
     * 
     * @param name The name of the warp
     * @param location The location to set
     * @param description Optional description of the warp
     */
    public void setWarp(String name, Location location, String description) {
        if (location == null || location.getWorld() == null) {
            ssjEssentials.getLogger().warning("Attempted to set warp with invalid location: " + name);
            return;
        }
        
        String path = "warps." + name;
        warpsConfig.set(path + ".x", location.getX());
        warpsConfig.set(path + ".y", location.getY());
        warpsConfig.set(path + ".z", location.getZ());
        warpsConfig.set(path + ".yaw", location.getYaw());
        warpsConfig.set(path + ".pitch", location.getPitch());
        warpsConfig.set(path + ".world", location.getWorld().getName());
        warpsConfig.set(path + ".description", description);
        
        // Update cache
        warpCache.put(name, location.clone());
        warpsDirty = true;
    }

    /**
     * Gets the file for a player's home data
     * 
     * @param player The player
     * @return The player's home data file
     */
    private File getPlayerHomeFile(Player player) {
        return new File(ssjEssentials.getDataFolder(), "playerdata/" + player.getUniqueId().toString() + ".yml");
    }

    /**
     * Gets the configuration for a player's home data
     * 
     * @param player The player
     * @return The player's home data configuration
     */
    private FileConfiguration getPlayerHomeConfig(Player player) {
        File file = getPlayerHomeFile(player);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                ssjEssentials.getLogger().log(Level.SEVERE, "Could not create player data file for " + player.getName(), e);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    /**
     * Saves a player's home data
     * 
     * @param player The player
     * @param config The configuration to save
     */
    private void savePlayerHomeConfig(Player player, FileConfiguration config) {
        // Clone the UUID and configuration for async use
        final File homeFile = getPlayerHomeFile(player);
        final FileConfiguration configCopy = config;
        final String playerName = player.getName(); // Store name for logging
        
        ssjEssentials.getServer().getScheduler().runTaskAsynchronously(ssjEssentials, () -> {
            try {
                configCopy.save(homeFile);
            } catch (IOException e) {
                ssjEssentials.getLogger().log(Level.SEVERE, "Could not save player data for " + playerName, e);
            }
        });
    }

    /**
     * Gets the maximum number of homes a player can have
     * 
     * @param player The player
     * @return The maximum number of homes allowed
     */
    public int getMaxHomes(Player player) {
        if (player.hasPermission("ssjessentials.admin")) {
            return ssjEssentials.getConfigs().getInt("home.max-homes-admin", 1000);
        } else if (player.hasPermission("ssjessentials.mod")) {
            return ssjEssentials.getConfigs().getInt("home.max-homes-mod", 20);
        }
        return ssjEssentials.getConfigs().getInt("home.max-homes", 3);
    }

    /**
     * Sets a home location for a player
     * 
     * @param player The player
     * @param name The name of the home
     * @param location The location to set
     * @return true if successful, false if at home limit
     */
    public boolean setHome(Player player, String name, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        FileConfiguration config = getPlayerHomeConfig(player);
        Set<String> homes = config.getConfigurationSection("homes") != null ? 
            config.getConfigurationSection("homes").getKeys(false) : new HashSet<>();
        
        if (homes.size() >= getMaxHomes(player) && !homes.contains(name)) {
            return false;
        }

        String path = "homes." + name;
        config.set(path + ".x", location.getX());
        config.set(path + ".y", location.getY());
        config.set(path + ".z", location.getZ());
        config.set(path + ".yaw", location.getYaw());
        config.set(path + ".pitch", location.getPitch());
        config.set(path + ".world", location.getWorld().getName());
        savePlayerHomeConfig(player, config);
        
        // Update cache
        String playerKey = player.getUniqueId().toString();
        Map<String, Location> playerHomes = homeCache.computeIfAbsent(playerKey, k -> new HashMap<>());
        playerHomes.put(name, location.clone());
        
        return true;
    }

    /**
     * Gets a warp location
     * 
     * @param name The name of the warp
     * @return The warp location, or null if not found
     */
    public Location getWarp(String name) {
        // Check cache first
        if (warpCache.containsKey(name)) {
            return warpCache.get(name).clone();
        }
        
        // Fall back to loading from config
        String path = "warps." + name;
        if (!warpsConfig.contains(path)) return null;
        
        try {
            Location location = new Location(
                org.bukkit.Bukkit.getWorld(warpsConfig.getString(path + ".world")),
                warpsConfig.getDouble(path + ".x"),
                warpsConfig.getDouble(path + ".y"),
                warpsConfig.getDouble(path + ".z"),
                (float) warpsConfig.getDouble(path + ".yaw"),
                (float) warpsConfig.getDouble(path + ".pitch")
            );
            
            // Update cache
            warpCache.put(name, location.clone());
            
            return location;
        } catch (Exception e) {
            ssjEssentials.getLogger().log(Level.WARNING, "Error loading warp: " + name, e);
            return null;
        }
    }

    /**
     * Gets a home location for a player
     * 
     * @param player The player
     * @param name The name of the home
     * @return The home location, or null if not found
     */
    public Location getHome(Player player, String name) {
        // Check cache first
        String playerKey = player.getUniqueId().toString();
        if (homeCache.containsKey(playerKey) && homeCache.get(playerKey).containsKey(name)) {
            return homeCache.get(playerKey).get(name).clone();
        }
        
        // Load home from file
        FileConfiguration config = getPlayerHomeConfig(player);
        String path = "homes." + name;
        if (!config.contains(path)) return null;
        
        try {
            Location location = new Location(
                org.bukkit.Bukkit.getWorld(config.getString(path + ".world")),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
            );
            
            // Update cache
            Map<String, Location> playerHomes = homeCache.computeIfAbsent(playerKey, k -> new HashMap<>());
            playerHomes.put(name, location.clone());
            
            return location;
        } catch (Exception e) {
            ssjEssentials.getLogger().log(Level.WARNING, "Error loading home for " + player.getName() + ": " + name, e);
            return null;
        }
    }

    /**
     * Gets all available warps
     * 
     * @return A set of warp names
     */
    public Set<String> getWarps() {
        return warpsConfig.getConfigurationSection("warps") != null ?
            warpsConfig.getConfigurationSection("warps").getKeys(false) : new HashSet<>();
    }

    /**
     * Gets all homes for a player
     * 
     * @param player The player
     * @return A set of home names
     */
    public Set<String> getHomes(Player player) {
        FileConfiguration config = getPlayerHomeConfig(player);
        return config.getConfigurationSection("homes") != null ? 
            config.getConfigurationSection("homes").getKeys(false) : new HashSet<>();
    }

    /**
     * Deletes a warp
     * 
     * @param name The name of the warp to delete
     */
    public void deleteWarp(String name) {
        warpsConfig.set("warps." + name, null);
        warpCache.remove(name);
        warpsDirty = true;
    }

    /**
     * Deletes a home for a player
     * 
     * @param player The player
     * @param name The name of the home to delete
     */
    public void deleteHome(Player player, String name) {
        FileConfiguration config = getPlayerHomeConfig(player);
        config.set("homes." + name, null);
        savePlayerHomeConfig(player, config);
        
        // Update cache
        String playerKey = player.getUniqueId().toString();
        if (homeCache.containsKey(playerKey)) {
            homeCache.get(playerKey).remove(name);
        }
    }
    
    /**
     * Gets the description of a warp
     * 
     * @param name The name of the warp
     * @return The description, or null if not found
     */
    public String getWarpDescription(String name) {
        String path = "warps." + name + ".description";
        return warpsConfig.contains(path) ? warpsConfig.getString(path) : null;
    }
    
    /**
     * Updates the description of a warp
     * 
     * @param name The name of the warp
     * @param description The new description
     * @return true if the warp was found and updated
     */
    public boolean updateWarpDescription(String name, String description) {
        String path = "warps." + name;
        if (!warpsConfig.contains(path)) return false;
        
        warpsConfig.set(path + ".description", description);
        warpsDirty = true;
        return true;
    }
    
    /**
     * Checks if a warp exists
     * 
     * @param name The name of the warp
     * @return true if the warp exists
     */
    public boolean warpExists(String name) {
        if (warpCache.containsKey(name)) {
            return true;
        }
        
        return warpsConfig.contains("warps." + name);
    }
    
    /**
     * Gets the number of homes a player has
     * 
     * @param player The player
     * @return The number of homes the player has
     */
    public int getHomeCount(Player player) {
        Set<String> homes = getHomes(player);
        return homes != null ? homes.size() : 0;
    }
    
    /**
     * Checks if a home exists for a player
     * 
     * @param player The player
     * @param name The name of the home
     * @return true if the home exists
     */
    public boolean homeExists(Player player, String name) {
        String playerKey = player.getUniqueId().toString();
        if (homeCache.containsKey(playerKey) && homeCache.get(playerKey).containsKey(name)) {
            return true;
        }
        
        FileConfiguration config = getPlayerHomeConfig(player);
        return config.contains("homes." + name);
    }
    
    /**
     * Saves all data immediately (for shutdown)
     */
    public void saveAllData() {
        try {
            warpsConfig.save(warpsFile);
            warpsDirty = false;
        } catch (IOException e) {
            ssjEssentials.getLogger().log(Level.SEVERE, "Could not save warps file during shutdown", e);
        }
    }

    /**
     * Force save all data synchronously
     * Only use for plugin shutdown
     */
    public void saveAllDataSync() {
        try {
            warpsConfig.save(warpsFile);
            warpsDirty = false;
        } catch (IOException e) {
            ssjEssentials.getLogger().log(Level.SEVERE, "Could not save warps file during shutdown", e);
        }
    }
    
    /**
     * Clears all caches and reloads from disk
     */
    public void reload() {
        saveAllData();
        warpCache.clear();
        homeCache.clear();
        loadConfigs();
    }
} 