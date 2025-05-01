package com.sausaliens.SSJEManagers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEConfig.SSJConfigs;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GroupManager {
    private final SSJEssentials plugin;
    private final File groupsFile;
    private FileConfiguration groupsConfig;
    private final Map<String, Set<String>> groupPermissions;
    private final Map<String, String> groupPrefixes;
    private final Map<String, String> groupSuffixes;
    
    // Cache for player groups to reduce lookups to PlayerData system
    private final Map<UUID, String> playerGroupCache = new ConcurrentHashMap<>();
    
    // Track if groups config needs to be saved
    private boolean groupsDirty = false;

    public GroupManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        this.groupPermissions = new HashMap<>();
        this.groupPrefixes = new HashMap<>();
        this.groupSuffixes = new HashMap<>();
        loadGroups();
        
        // Schedule regular saving of data if changes are made
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (groupsDirty) {
                saveGroups();
                groupsDirty = false;
            }
        }, 6000L, 6000L); // Save every 5 minutes (20 ticks * 60 seconds * 5)
    }

    public void loadGroups() {
        if (!groupsFile.exists()) {
            plugin.saveResource("groups.yml", false);
        }
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);
        
        // Clear existing data
        groupPermissions.clear();
        groupPrefixes.clear();
        groupSuffixes.clear();
        
        // Load default groups if config is empty
        if (groupsConfig.getConfigurationSection("groups") == null) {
            createDefaultGroups();
        }

        // Load all groups and their permissions, prefixes, and suffixes
        for (String groupName : groupsConfig.getConfigurationSection("groups").getKeys(false)) {
            String groupPath = "groups." + groupName;
            String lowercaseGroupName = groupName.toLowerCase();
            
            // Load permissions
            Set<String> permissions = new HashSet<>(groupsConfig.getStringList(groupPath + ".permissions"));
            groupPermissions.put(lowercaseGroupName, permissions);
            
            // Load prefix
            String prefix = groupsConfig.getString(groupPath + ".prefix", "");
            groupPrefixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', prefix));
            
            // Load suffix
            String suffix = groupsConfig.getString(groupPath + ".suffix", "");
            groupSuffixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', suffix));
        }
        
        // Clear player group cache when reloading groups
        playerGroupCache.clear();
    }

    private void createDefaultGroups() {
        // Default group
        groupsConfig.set("groups.default.permissions", Arrays.asList(
            "ssjessentials.default"
        ));
        groupsConfig.set("groups.default.prefix", "");
        groupsConfig.set("groups.default.suffix", "");

        // Mod group
        groupsConfig.set("groups.mod.permissions", Arrays.asList(
            "ssjessentials.mod"
        ));
        groupsConfig.set("groups.mod.prefix", "&b[Mod]");
        groupsConfig.set("groups.mod.suffix", "&f");

        // Admin group
        groupsConfig.set("groups.admin.permissions", Arrays.asList(
            "ssjessentials.admin"
        ));
        groupsConfig.set("groups.admin.prefix", "&3[Admin]");
        groupsConfig.set("groups.admin.suffix", "&f");

        try {
            // First save the config
            groupsConfig.save(groupsFile);
            
            // Read the current content
            String content = new String(java.nio.file.Files.readAllBytes(groupsFile.toPath()));
            
            // Add line breaks between groups
            content = content.replace("  mod:", "\n  mod:");
            content = content.replace("  admin:", "\n  admin:");
            
            // Write the modified content back
            java.nio.file.Files.write(groupsFile.toPath(), content.getBytes());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
        }
    }

    public boolean createGroup(String groupName, List<String> permissions) {
        if (groupPermissions.containsKey(groupName.toLowerCase())) {
            return false;
        }

        String lowercaseGroupName = groupName.toLowerCase();
        
        // Get existing groups configuration
        String existingConfig = "";
        if (groupsFile.exists()) {
            try {
                existingConfig = new String(java.nio.file.Files.readAllBytes(groupsFile.toPath()));
            } catch (IOException e) {
                plugin.getLogger().warning("Error reading groups.yml: " + e.getMessage());
            }
        }
        
        // Process permissions to ensure wildcards are properly quoted
        List<String> processedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (permission.contains("*")) {
                processedPermissions.add("'" + permission + "'");
            } else {
                processedPermissions.add(permission);
            }
        }
        
        // Add the new group with proper formatting
        groupsConfig.set("groups." + lowercaseGroupName + ".permissions", processedPermissions);
        groupsConfig.set("groups." + lowercaseGroupName + ".prefix", "&7[" + groupName + "] ");
        groupsConfig.set("groups." + lowercaseGroupName + ".suffix", "");
        
        // Save the config
        try {
            groupsConfig.save(groupsFile);
            
            // Read the current content
            String content = new String(java.nio.file.Files.readAllBytes(groupsFile.toPath()));
            
            // If this isn't the first group, add an extra newline before it
            if (!existingConfig.trim().isEmpty() && !content.equals(existingConfig)) {
                content = content.replace(
                    "  " + lowercaseGroupName + ":",
                    "\n  " + lowercaseGroupName + ":"
                );
                
                // Write the modified content back
                java.nio.file.Files.write(groupsFile.toPath(), content.getBytes());
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
            return false;
        }
        
        // Update memory maps
        groupPermissions.put(lowercaseGroupName, new HashSet<>(permissions));
        groupPrefixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', "&7[" + groupName + "] "));
        groupSuffixes.put(lowercaseGroupName, "");
        
        return true;
    }

    public boolean deleteGroup(String groupName) {
        String lowercaseGroupName = groupName.toLowerCase();
        if (!groupPermissions.containsKey(lowercaseGroupName)) {
            return false;
        }

        groupsConfig.set("groups." + lowercaseGroupName, null);
        groupPermissions.remove(lowercaseGroupName);
        groupPrefixes.remove(lowercaseGroupName);
        groupSuffixes.remove(lowercaseGroupName);
        
        // Update players who were in this group to default (async)
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            getPlayerGroupAsync(player, group -> {
                if (group.equalsIgnoreCase(groupName)) {
                    setPlayerGroupAsync(player, "default", null);
                }
            });
        }
        
        groupsDirty = true;
        return true;
    }

    public boolean setGroupPrefix(String groupName, String prefix) {
        String lowercaseGroupName = groupName.toLowerCase();
        if (!groupPermissions.containsKey(lowercaseGroupName)) {
            return false;
        }

        groupsConfig.set("groups." + lowercaseGroupName + ".prefix", prefix);
        groupPrefixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', prefix));
        groupsDirty = true;
        return true;
    }

    public boolean setGroupSuffix(String groupName, String suffix) {
        String lowercaseGroupName = groupName.toLowerCase();
        if (!groupPermissions.containsKey(lowercaseGroupName)) {
            return false;
        }

        groupsConfig.set("groups." + lowercaseGroupName + ".suffix", suffix);
        groupSuffixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', suffix));
        groupsDirty = true;
        return true;
    }

    public String getGroupPrefix(String groupName) {
        return groupPrefixes.getOrDefault(groupName.toLowerCase(), "");
    }

    public String getGroupSuffix(String groupName) {
        return groupSuffixes.getOrDefault(groupName.toLowerCase(), "");
    }

    // Deprecated: Use getPlayerGroupAsync instead
    @Deprecated
    public String getPlayerPrefix(Player player) {
        // Use async version in new code
        plugin.getLogger().warning("[DEPRECATED] getPlayerPrefix called synchronously. Use async version instead.");
        String groupName = getPlayerGroup(player);
        return getGroupPrefix(groupName);
    }

    // Deprecated: Use getPlayerGroupAsync instead
    @Deprecated
    public String getPlayerSuffix(Player player) {
        // Use async version in new code
        plugin.getLogger().warning("[DEPRECATED] getPlayerSuffix called synchronously. Use async version instead.");
        String groupName = getPlayerGroup(player);
        return getGroupSuffix(groupName);
    }

    public boolean addPermissionToGroup(String groupName, String permission) {
        Set<String> permissions = groupPermissions.get(groupName.toLowerCase());
        if (permissions == null) {
            return false;
        }

        permissions.add(permission);
        groupsConfig.set("groups." + groupName.toLowerCase() + ".permissions", new ArrayList<>(permissions));
        groupsDirty = true;
        return true;
    }

    public boolean removePermissionFromGroup(String groupName, String permission) {
        Set<String> permissions = groupPermissions.get(groupName.toLowerCase());
        if (permissions == null) {
            return false;
        }

        permissions.remove(permission);
        groupsConfig.set("groups." + groupName.toLowerCase() + ".permissions", new ArrayList<>(permissions));
        groupsDirty = true;
        return true;
    }

    public void updatePlayerTabName(Player player) {
        getPlayerGroupAsync(player, groupName -> {
            String prefix = getGroupPrefix(groupName);
            // Get player data first
            SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
            String displayName = playerData != null && playerData.getNickname() != null ? playerData.getNickname() : player.getName();
            // Set both display name and tab list name
            player.setDisplayName(displayName);
            player.setPlayerListName(prefix + displayName);
        });
    }

    // Async version of setPlayerGroup
    public void setPlayerGroupAsync(Player player, String groupName, Runnable callback) {
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in setPlayerGroupAsync");
                return;
            }
            playerData.setGroup(groupName.toLowerCase());
            plugin.getConfigs().savePlayerData(player);
            // Update cache
            playerGroupCache.put(player.getUniqueId(), groupName.toLowerCase());
            // Update the player's tab list name
            updatePlayerTabName(player);
            if (callback != null) callback.run();
        });
    }

    // Async version of getPlayerGroup
    public void getPlayerGroupAsync(Player player, java.util.function.Consumer<String> callback) {
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in getPlayerGroupAsync");
                callback.accept("default");
                return;
            }
            String group = playerData.getGroup();
            // Store in cache for faster future lookups
            playerGroupCache.put(player.getUniqueId(), group);
            callback.accept(group);
        });
    }

    // Deprecated sync version
    public boolean setPlayerGroup(Player player, String groupName) {
        plugin.getLogger().warning("[DEPRECATED] setPlayerGroup called synchronously. Use setPlayerGroupAsync instead.");
        if (!groupPermissions.containsKey(groupName.toLowerCase())) {
            return false;
        }
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        if (playerData == null) {
            plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in setPlayerGroup");
            return false;
        }
        playerData.setGroup(groupName.toLowerCase());
        plugin.getConfigs().savePlayerData(player);
        // Update cache
        playerGroupCache.put(player.getUniqueId(), groupName.toLowerCase());
        // Update the player's tab list name
        updatePlayerTabName(player);
        return true;
    }

    // Deprecated sync version
    public String getPlayerGroup(Player player) {
        plugin.getLogger().warning("[DEPRECATED] getPlayerGroup called synchronously. Use getPlayerGroupAsync instead.");
        UUID playerUUID = player.getUniqueId();
        // Check cache first
        if (playerGroupCache.containsKey(playerUUID)) {
            String cachedGroup = playerGroupCache.get(playerUUID);
            // Verify the cached data is still accurate
            SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in getPlayerGroup");
                return "default";
            }
            String actualGroup = playerData.getGroup();
            if (!cachedGroup.equals(actualGroup)) {
                // Cache is out of date, update it
                playerGroupCache.put(playerUUID, actualGroup);
                return actualGroup;
            }
            return cachedGroup;
        }
        // If not in cache, get from PlayerData and cache it
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        if (playerData == null) {
            plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in getPlayerGroup");
            return "default";
        }
        String group = playerData.getGroup();
        // Store in cache for faster future lookups
        playerGroupCache.put(playerUUID, group);
        return group;
    }

    /**
     * Removes a player from the group cache - should be called when a player logs out
     * 
     * @param player The player to remove from cache
     */
    public void clearPlayerCache(Player player) {
        playerGroupCache.remove(player.getUniqueId());
    }

    public Set<String> getGroupPermissions(String groupName) {
        return new HashSet<>(groupPermissions.getOrDefault(groupName.toLowerCase(), new HashSet<>()));
    }

    public Set<String> getPlayerPermissions(Player player) {
        String groupName = getPlayerGroup(player);
        return getGroupPermissions(groupName);
    }

    public Set<String> getGroups() {
        return new HashSet<>(groupPermissions.keySet());
    }

    private void saveGroups() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                groupsConfig.save(groupsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
            }
        });
    }
    
    /**
     * Save groups synchronously - only use during server shutdown
     */
    public void saveGroupsSync() {
        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
        }
    }

    /**
     * Saves all data
     */
    public void saveAllData() {
        if (groupsDirty) {
            saveGroups();
            groupsDirty = false;
        }
    }
    
    /**
     * Saves all data synchronously - only use during server shutdown
     */
    public void saveAllDataSync() {
        if (groupsDirty) {
            saveGroupsSync();
            groupsDirty = false;
        }
    }
    
    /**
     * Reloads all group data from disk and clears caches
     */
    public void reload() {
        // Save any pending changes
        saveAllData();
        
        // Clear caches
        playerGroupCache.clear();
        
        // Reload groups
        loadGroups();
    }
} 