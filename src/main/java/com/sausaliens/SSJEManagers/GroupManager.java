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

public class GroupManager {
    private final SSJEssentials plugin;
    private final File groupsFile;
    private FileConfiguration groupsConfig;
    private final Map<String, Set<String>> groupPermissions;
    private final Map<String, String> groupPrefixes;
    private final Map<String, String> groupSuffixes;

    public GroupManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.groupsFile = new File(plugin.getDataFolder(), "groups.yml");
        this.groupPermissions = new HashMap<>();
        this.groupPrefixes = new HashMap<>();
        this.groupSuffixes = new HashMap<>();
        loadGroups();
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
        
        // Update players who were in this group to default
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (getPlayerGroup(player).equalsIgnoreCase(groupName)) {
                setPlayerGroup(player, "default");
            }
        }
        
        saveGroups();
        return true;
    }

    public boolean setGroupPrefix(String groupName, String prefix) {
        String lowercaseGroupName = groupName.toLowerCase();
        if (!groupPermissions.containsKey(lowercaseGroupName)) {
            return false;
        }

        groupsConfig.set("groups." + lowercaseGroupName + ".prefix", prefix);
        groupPrefixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', prefix));
        saveGroups();
        return true;
    }

    public boolean setGroupSuffix(String groupName, String suffix) {
        String lowercaseGroupName = groupName.toLowerCase();
        if (!groupPermissions.containsKey(lowercaseGroupName)) {
            return false;
        }

        groupsConfig.set("groups." + lowercaseGroupName + ".suffix", suffix);
        groupSuffixes.put(lowercaseGroupName, ChatColor.translateAlternateColorCodes('&', suffix));
        saveGroups();
        return true;
    }

    public String getGroupPrefix(String groupName) {
        return groupPrefixes.getOrDefault(groupName.toLowerCase(), "");
    }

    public String getGroupSuffix(String groupName) {
        return groupSuffixes.getOrDefault(groupName.toLowerCase(), "");
    }

    public String getPlayerPrefix(Player player) {
        String groupName = getPlayerGroup(player);
        return getGroupPrefix(groupName);
    }

    public String getPlayerSuffix(Player player) {
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
        saveGroups();
        return true;
    }

    public boolean removePermissionFromGroup(String groupName, String permission) {
        Set<String> permissions = groupPermissions.get(groupName.toLowerCase());
        if (permissions == null) {
            return false;
        }

        permissions.remove(permission);
        groupsConfig.set("groups." + groupName.toLowerCase() + ".permissions", new ArrayList<>(permissions));
        saveGroups();
        return true;
    }

    public void updatePlayerTabName(Player player) {
        String groupName = getPlayerGroup(player);
        String prefix = getGroupPrefix(groupName);
        String displayName = player.getName();
        
        // If player has a nickname, use it instead
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        if (playerData.getNickname() != null) {
            displayName = playerData.getNickname();
        }
        
        // Set the tab list name with the prefix
        player.setPlayerListName(prefix + displayName);
    }

    public boolean setPlayerGroup(Player player, String groupName) {
        if (!groupPermissions.containsKey(groupName.toLowerCase())) {
            return false;
        }

        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        playerData.setGroup(groupName.toLowerCase());
        plugin.getConfigs().savePlayerData(player);
        
        // Update the player's tab list name
        updatePlayerTabName(player);
        return true;
    }

    public String getPlayerGroup(Player player) {
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        return playerData.getGroup();
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
        try {
            groupsConfig.save(groupsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save groups.yml: " + e.getMessage());
        }
    }
} 