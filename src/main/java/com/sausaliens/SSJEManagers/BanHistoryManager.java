package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.profile.PlayerProfile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages unban history tracking for the server
 */
public class BanHistoryManager {
    private final SSJEssentials plugin;
    private final File banHistoryFile;
    private FileConfiguration banHistoryConfig;
    
    private final Map<UUID, List<UnbanHistoryEntry>> unbanHistoryMap = new HashMap<>();
    
    private long lastSaveTime = 0;
    
    public BanHistoryManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.banHistoryFile = new File(plugin.getDataFolder(), "banhistory.yml");
        this.loadUnbanHistory();
    }
    
    /**
     * Represents a single unban history entry
     */
    public static class UnbanHistoryEntry {
        private final String playerName;
        private final UUID playerUUID;
        private final String reason;
        private final String source;
        private final Date banDate;
        private final Date unbanDate;
        private final String unbannedBy;
        
        public UnbanHistoryEntry(String playerName, UUID playerUUID, String reason, String source,
                              Date banDate, Date unbanDate, String unbannedBy) {
            this.playerName = playerName;
            this.playerUUID = playerUUID;
            this.reason = reason;
            this.source = source;
            this.banDate = banDate;
            this.unbanDate = unbanDate;
            this.unbannedBy = unbannedBy;
        }
        
        // Getters
        public String getPlayerName() { return playerName; }
        public UUID getPlayerUUID() { return playerUUID; }
        public String getReason() { return reason; }
        public String getSource() { return source; }
        public Date getBanDate() { return banDate; }
        public Date getUnbanDate() { return unbanDate; }
        public String getUnbannedBy() { return unbannedBy; }
    }
    
    /**
     * Load unban history from file
     */
    private void loadUnbanHistory() {
        if (!banHistoryFile.exists()) {
            try {
                // Make sure plugin data folder exists
                if (!plugin.getDataFolder().exists()) {
                    plugin.getDataFolder().mkdirs();
                }
                
                // Save default banhistory.yml from resources
                plugin.saveResource("banhistory.yml", false);
                plugin.getLogger().info("Created default unban history file");
                
                // Wait a moment for the file system to complete operations
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignore
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create unban history file: " + e.getMessage());
                // Fallback to create empty file if resource saving fails
                try {
                    banHistoryFile.createNewFile();
                } catch (IOException ex) {
                    plugin.getLogger().severe("Failed fallback creation of unban history file: " + ex.getMessage());
                }
            }
        }
        
        banHistoryConfig = YamlConfiguration.loadConfiguration(banHistoryFile);
        unbanHistoryMap.clear();
        
        ConfigurationSection unbansSection = banHistoryConfig.getConfigurationSection("unbans");
        if (unbansSection == null) {
            // Create an empty unbans section if it doesn't exist, but don't save it immediately
            unbansSection = banHistoryConfig.createSection("unbans");
            // Don't save immediately to avoid triggering the file watcher
            return;
        }
        
        for (String uuidString : unbansSection.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(uuidString);
                ConfigurationSection playerSection = unbansSection.getConfigurationSection(uuidString);
                if (playerSection == null) continue;
                
                List<UnbanHistoryEntry> entries = new ArrayList<>();
                
                for (String entryKey : playerSection.getKeys(false)) {
                    ConfigurationSection entrySection = playerSection.getConfigurationSection(entryKey);
                    if (entrySection == null) continue;
                    
                    String playerName = entrySection.getString("player_name", "Unknown");
                    String reason = entrySection.getString("reason", "Unknown");
                    String source = entrySection.getString("source", "Unknown");
                    long banTimestamp = entrySection.getLong("ban_date", 0);
                    long unbanTimestamp = entrySection.getLong("unban_date", 0);
                    String unbannedBy = entrySection.getString("unbanned_by", "Unknown");
                    
                    Date banDate = new Date(banTimestamp);
                    Date unbanDate = new Date(unbanTimestamp);
                    
                    UnbanHistoryEntry entry = new UnbanHistoryEntry(
                        playerName, playerUUID, reason, source, banDate, unbanDate, unbannedBy
                    );
                    
                    entries.add(entry);
                }
                
                unbanHistoryMap.put(playerUUID, entries);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in unban history: " + uuidString);
            }
        }
    }
    
    /**
     * Save unban history to file
     */
    public void saveUnbanHistory() {
        // Skip saving if we've saved recently (with a 2-second cooldown)
        long now = System.currentTimeMillis();
        if (now - lastSaveTime < 2000) {
            return;
        }
        lastSaveTime = now;
        
        banHistoryConfig = new YamlConfiguration();
        
        ConfigurationSection unbansSection = banHistoryConfig.createSection("unbans");
        
        for (Map.Entry<UUID, List<UnbanHistoryEntry>> entry : unbanHistoryMap.entrySet()) {
            String uuidString = entry.getKey().toString();
            ConfigurationSection playerSection = unbansSection.createSection(uuidString);
            
            List<UnbanHistoryEntry> entries = entry.getValue();
            for (int i = 0; i < entries.size(); i++) {
                UnbanHistoryEntry historyEntry = entries.get(i);
                ConfigurationSection entrySection = playerSection.createSection(String.valueOf(i));
                
                entrySection.set("player_name", historyEntry.getPlayerName());
                entrySection.set("reason", historyEntry.getReason());
                entrySection.set("source", historyEntry.getSource());
                entrySection.set("ban_date", historyEntry.getBanDate().getTime());
                entrySection.set("unban_date", historyEntry.getUnbanDate().getTime());
                entrySection.set("unbanned_by", historyEntry.getUnbannedBy());
            }
        }
        
        // Save async to prevent lag
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                banHistoryConfig.save(banHistoryFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save unban history: " + e.getMessage());
            }
        });
    }
    
    /**
     * Saves unban history synchronously - only use during server shutdown
     */
    public void saveUnbanHistorySync() {
        banHistoryConfig = new YamlConfiguration();
        
        ConfigurationSection unbansSection = banHistoryConfig.createSection("unbans");
        
        for (Map.Entry<UUID, List<UnbanHistoryEntry>> entry : unbanHistoryMap.entrySet()) {
            String uuidString = entry.getKey().toString();
            ConfigurationSection playerSection = unbansSection.createSection(uuidString);
            
            List<UnbanHistoryEntry> entries = entry.getValue();
            for (int i = 0; i < entries.size(); i++) {
                UnbanHistoryEntry historyEntry = entries.get(i);
                ConfigurationSection entrySection = playerSection.createSection(String.valueOf(i));
                
                entrySection.set("player_name", historyEntry.getPlayerName());
                entrySection.set("reason", historyEntry.getReason());
                entrySection.set("source", historyEntry.getSource());
                entrySection.set("ban_date", historyEntry.getBanDate().getTime());
                entrySection.set("unban_date", historyEntry.getUnbanDate().getTime());
                entrySection.set("unbanned_by", historyEntry.getUnbannedBy());
            }
        }
        
        try {
            banHistoryConfig.save(banHistoryFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save unban history: " + e.getMessage());
        }
    }
    
    /**
     * Record a player being unbanned, capturing all details from their ban entry
     * @param target The player's profile
     * @param unbannedBy Who unbanned them
     */
    public void recordUnban(PlayerProfile target, String unbannedBy) {
        UUID playerUUID = target.getUniqueId();
        
        // Attempt multiple ways to get a valid player name
        String playerName = "Unknown";
        if (target.getName() != null && !target.getName().isEmpty()) {
            playerName = target.getName();
        } else {
            // Try to get the name from cached data or server API
            try {
                // First check if the player has been online before and we can get their name from the server
                org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
                if (offlinePlayer != null && offlinePlayer.getName() != null && !offlinePlayer.getName().isEmpty()) {
                    playerName = offlinePlayer.getName();
                    plugin.getLogger().info("Retrieved player name from OfflinePlayer data: " + playerName);
                } else {
                    // If the ban entry exists, try to get the name from the ban entry's profile
                    BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                    BanEntry<PlayerProfile> banEntry = banList.getBanEntry(target);
                    if (banEntry != null && banEntry.getBanTarget() != null) {
                        PlayerProfile bannedProfile = banEntry.getBanTarget();
                        if (bannedProfile.getName() != null && !bannedProfile.getName().isEmpty()) {
                            playerName = bannedProfile.getName();
                            plugin.getLogger().info("Retrieved player name from ban entry profile: " + playerName);
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error retrieving player name: " + e.getMessage());
            }
        }
        
        if (playerUUID == null) {
            plugin.getLogger().warning("Cannot record unban for player with null UUID");
            return;
        }
        
        // Find the ban entry before it's pardoned
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        BanEntry<PlayerProfile> banEntry = banList.getBanEntry(target);
        
        // Default values in case ban entry is not found
        String reason = "No reason specified";
        String source = "Unknown";
        Date banDate = new Date(0); // Jan 1, 1970
        Date unbanDate = new Date(); // Current time
        
        // If we have a ban entry, get the details from it
        if (banEntry != null) {
            reason = banEntry.getReason() != null && !banEntry.getReason().isEmpty() 
                ? banEntry.getReason() : "No reason specified";
            source = banEntry.getSource() != null ? banEntry.getSource() : "Unknown";
            banDate = banEntry.getCreated() != null ? banEntry.getCreated() : new Date(0);
            
            // One more attempt to get player name from the banned profile if it's still unknown
            if (playerName.equals("Unknown") && banEntry.getBanTarget() != null && banEntry.getBanTarget().getName() != null) {
                playerName = banEntry.getBanTarget().getName();
                plugin.getLogger().info("Last resort: Retrieved player name from ban entry banned profile: " + playerName);
            }
        } else {
            plugin.getLogger().info("No active ban entry found for " + playerName + " when recording unban");
        }
        
        // Log the player name we're using
        plugin.getLogger().info("Recording unban with player name: " + playerName);
        
        UnbanHistoryEntry entry = new UnbanHistoryEntry(
            playerName, playerUUID, reason, source, banDate, unbanDate, unbannedBy
        );
        
        // Add the entry to the history map
        List<UnbanHistoryEntry> playerEntries = unbanHistoryMap.computeIfAbsent(playerUUID, k -> new ArrayList<>());
        playerEntries.add(entry);
        
        // Log info about the unban
        plugin.getLogger().info("Recorded unban for " + playerName + " by " + unbannedBy);
        
        // Save history to disk
        saveUnbanHistory();
    }
    
    /**
     * Get all unban history entries
     * @return List of all unban history entries
     */
    public List<UnbanHistoryEntry> getAllUnbanHistory() {
        List<UnbanHistoryEntry> allEntries = new ArrayList<>();
        for (List<UnbanHistoryEntry> entries : unbanHistoryMap.values()) {
            allEntries.addAll(entries);
        }
        return allEntries;
    }
    
    /**
     * Search unban history by player name (partial matches allowed)
     * @param nameSearch The search string for player names
     * @return List of matching unban history entries
     */
    public List<UnbanHistoryEntry> searchUnbanHistory(String nameSearch) {
        List<UnbanHistoryEntry> results = new ArrayList<>();
        String lowerSearch = nameSearch.toLowerCase();
        
        for (List<UnbanHistoryEntry> entries : unbanHistoryMap.values()) {
            for (UnbanHistoryEntry entry : entries) {
                if (entry.getPlayerName().toLowerCase().contains(lowerSearch)) {
                    results.add(entry);
                }
            }
        }
        
        return results;
    }
    
    /**
     * Format a message about an unban history entry
     * @param entry The unban history entry
     * @param dateFormat Date formatter
     * @return Formatted unban entry message
     */
    public String formatUnbanHistoryEntry(UnbanHistoryEntry entry, SimpleDateFormat dateFormat) {
        StringBuilder message = new StringBuilder();
        
        message.append("§e").append(entry.getPlayerName()).append("\n");
        message.append("§7Original Ban Reason: §f").append(entry.getReason()).append("\n");
        message.append("§7Banned by: §f").append(entry.getSource()).append("\n");
        message.append("§7Banned on: §f").append(dateFormat.format(entry.getBanDate())).append("\n");
        message.append("§7Unbanned by: §f").append(entry.getUnbannedBy()).append("\n");
        message.append("§7Unbanned on: §f").append(dateFormat.format(entry.getUnbanDate())).append("\n");
        message.append("§7§m----------------------------------------");
        
        return message.toString();
    }
} 