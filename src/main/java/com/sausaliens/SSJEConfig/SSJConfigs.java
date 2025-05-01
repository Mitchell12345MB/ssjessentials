package com.sausaliens.SSJEConfig;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;
import org.bukkit.GameMode;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

public class SSJConfigs {
    private final SSJEssentials plugin;
    private final ConcurrentHashMap<UUID, PlayerData> playerData;
    private final File playerDataFolder;
    private final ConcurrentHashMap<UUID, Long> lastSaveTime;
    private static final long SAVE_COOLDOWN = 2000; // 2 seconds cooldown between saves for the same player
    private File configFile;
    private FileConfiguration config;

    public SSJConfigs(SSJEssentials plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.lastSaveTime = new ConcurrentHashMap<>();
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        setupConfig();
        loadConfig();
    }

    private void setupConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdir();
        }
    }

    public PlayerData getPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerData.containsKey(uuid)) {
            File playerFile = new File(playerDataFolder, player.getName() + ".yml");
            if (playerFile.exists()) {
                // File exists, load asynchronously and return null until loaded
                loadPlayerData(player);
                return null;
            } else {
                // File does not exist, create and save default data
                PlayerData defaultData = createDefaultPlayerData(player);
                return defaultData;
            }
        }
        return playerData.get(uuid);
    }

    private void loadPlayerData(Player player) {
        // Run async to load player data
        new BukkitRunnable() {
            @Override
            public void run() {
                File playerFile = new File(playerDataFolder, player.getName() + ".yml");
                if (!playerFile.exists()) {
                    // File does not exist, create and save default data on main thread
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            createDefaultPlayerData(player);
                        }
                    }.runTask(plugin);
                    return;
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                PlayerData data = new PlayerData();
                data.setFlying(config.getBoolean("flying", false));
                data.setVanished(config.getBoolean("vanished", false));
                data.setFrozen(config.getBoolean("frozen", false));
                data.setGodMode(config.getBoolean("godmode", false));
                data.setGameMode(GameMode.valueOf(config.getString("gamemode", "SURVIVAL")));
                data.setNickname(config.getString("nickname", null));
                String group = config.getString("group", "default").toLowerCase();
                if (group == null || group.isEmpty()) {
                    group = "default";
                }
                data.setGroup(group);
                if (config.contains("homes")) {
                    for (String homeName : config.getConfigurationSection("homes").getKeys(false)) {
                        String path = "homes." + homeName;
                        String world = config.getString(path + ".world");
                        double x = config.getDouble(path + ".x");
                        double y = config.getDouble(path + ".y");
                        double z = config.getDouble(path + ".z");
                        float yaw = (float) config.getDouble(path + ".yaw");
                        float pitch = (float) config.getDouble(path + ".pitch");
                        org.bukkit.Location loc = new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                        data.getHomes().put(homeName, loc);
                    }
                }
                data.lastTeleportRequest = config.getLong("lastTeleportRequest", 0L);
                // Apply the data on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        playerData.put(player.getUniqueId(), data);
                        if (data.getNickname() != null) {
                            player.setDisplayName(data.getNickname());
                            player.setPlayerListName(data.getNickname());
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Creates default player data for a player and adds it to the cache
     * @param player The player to create data for
     * @return The created default player data
     */
    public PlayerData createDefaultPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        
        PlayerData data = new PlayerData();
        data.setFlying(false);
        data.setVanished(false);
        data.setFrozen(false);
        data.setGodMode(false);
        data.setGameMode(GameMode.SURVIVAL);
        data.setNickname(null);
        data.setGroup("default");
        
        // Add to the cache
        playerData.put(uuid, data);
        
        // Save it to disk
        savePlayerData(player, true);
        
        plugin.getLogger().info("Created default player data for " + player.getName());
        return data;
    }

    /**
     * Saves player data asynchronously to prevent lag on the main thread
     * @param player The player whose data to save
     */
    public void savePlayerData(Player player) {
        savePlayerData(player, false);
    }
    
    /**
     * Saves player data with the option to force an immediate save
     * @param player The player whose data to save
     * @param force Whether to ignore the cooldown and save immediately
     */
    public void savePlayerData(Player player, boolean force) {
        UUID uuid = player.getUniqueId();
        PlayerData data = playerData.get(uuid);
        if (data == null) return;
        
        // Check if we should save now or respect the cooldown
        long currentTime = System.currentTimeMillis();
        if (!force && lastSaveTime.containsKey(uuid)) {
            long lastSave = lastSaveTime.get(uuid);
            if (currentTime - lastSave < SAVE_COOLDOWN) {
                return; // Skip this save as it's too soon after the last one
            }
        }
        
        // Update the last save time
        lastSaveTime.put(uuid, currentTime);
        
        // Copy necessary data for async operations
        final String playerName = player.getName();
        final String playerUUID = uuid.toString();
        final boolean isFlying = data.isFlying();
        final boolean isVanished = data.isVanished();
        final boolean isFrozen = data.isFrozen();
        final boolean isGodMode = data.isGodMode();
        final String gameMode = data.getGameMode().toString();
        final String nickname = data.getNickname();
        final String group = data.getGroup();
        
        // Save asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                File playerFile = new File(playerDataFolder, playerName + ".yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                
                config.set("uuid", playerUUID);
                config.set("name", playerName);
                config.set("flying", isFlying);
                config.set("vanished", isVanished);
                config.set("frozen", isFrozen);
                config.set("godmode", isGodMode);
                config.set("gamemode", gameMode);
                config.set("nickname", nickname);
                config.set("group", group);
                config.set("homes", null);
                for (Map.Entry<String, org.bukkit.Location> entry : data.getHomes().entrySet()) {
                    String path = "homes." + entry.getKey();
                    org.bukkit.Location loc = entry.getValue();
                    config.set(path + ".world", loc.getWorld().getName());
                    config.set(path + ".x", loc.getX());
                    config.set(path + ".y", loc.getY());
                    config.set(path + ".z", loc.getZ());
                    config.set(path + ".yaw", loc.getYaw());
                    config.set(path + ".pitch", loc.getPitch());
                }
                config.set("lastTeleportRequest", data.lastTeleportRequest);
                
                try {
                    config.save(playerFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save data for player: " + playerName + ", error: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Saves all player data asynchronously
     */
    public void saveAllData() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player, true); // Force save all players
        }
    }

    /**
     * Forces an immediate save of all data and waits for completion
     * This should only be used during plugin shutdown
     */
    public void saveAllDataSync() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData data = playerData.get(uuid);
            
            if (data != null) {
                File playerFile = new File(playerDataFolder, player.getName() + ".yml");
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                
                config.set("uuid", player.getUniqueId().toString());
                config.set("name", player.getName());
                config.set("flying", data.isFlying());
                config.set("vanished", data.isVanished());
                config.set("frozen", data.isFrozen());
                config.set("godmode", data.isGodMode());
                config.set("gamemode", data.getGameMode().toString());
                config.set("nickname", data.getNickname());
                config.set("group", data.getGroup());
                config.set("homes", null);
                for (Map.Entry<String, org.bukkit.Location> entry : data.getHomes().entrySet()) {
                    String path = "homes." + entry.getKey();
                    org.bukkit.Location loc = entry.getValue();
                    config.set(path + ".world", loc.getWorld().getName());
                    config.set(path + ".x", loc.getX());
                    config.set(path + ".y", loc.getY());
                    config.set(path + ".z", loc.getZ());
                    config.set(path + ".yaw", loc.getYaw());
                    config.set(path + ".pitch", loc.getPitch());
                }
                config.set("lastTeleportRequest", data.lastTeleportRequest);
                
                try {
                    config.save(playerFile);
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not save data for player: " + player.getName());
                }
            }
        }
    }

    public void reloadConfig() {
        // Save current data
        saveAllData();
        
        // Clear current data
        playerData.clear();
        
        // Reload all online players' data
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            loadPlayerData(player);
        }
    }

    /**
     * Asynchronously gets player data, loading from disk if needed.
     * @param player The player
     * @return CompletableFuture that completes with the PlayerData
     */
    public CompletableFuture<PlayerData> getPlayerDataAsync(Player player) {
        UUID uuid = player.getUniqueId();
        File playerFile = new File(playerDataFolder, player.getName() + ".yml");
        CompletableFuture<PlayerData> future = new CompletableFuture<>();

        if (playerData.containsKey(uuid)) {
            future.complete(playerData.get(uuid));
        } else if (playerFile.exists()) {
            // Load async
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
                PlayerData data = new PlayerData();
                data.setFlying(config.getBoolean("flying", false));
                data.setVanished(config.getBoolean("vanished", false));
                data.setFrozen(config.getBoolean("frozen", false));
                data.setGodMode(config.getBoolean("godmode", false));
                data.setGameMode(GameMode.valueOf(config.getString("gamemode", "SURVIVAL")));
                data.setNickname(config.getString("nickname", null));
                String group = config.getString("group", "default").toLowerCase();
                if (group == null || group.isEmpty()) {
                    group = "default";
                }
                data.setGroup(group);
                if (config.contains("homes")) {
                    for (String homeName : config.getConfigurationSection("homes").getKeys(false)) {
                        String path = "homes." + homeName;
                        String world = config.getString(path + ".world");
                        double x = config.getDouble(path + ".x");
                        double y = config.getDouble(path + ".y");
                        double z = config.getDouble(path + ".z");
                        float yaw = (float) config.getDouble(path + ".yaw");
                        float pitch = (float) config.getDouble(path + ".pitch");
                        org.bukkit.Location loc = new org.bukkit.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                        data.getHomes().put(homeName, loc);
                    }
                }
                data.lastTeleportRequest = config.getLong("lastTeleportRequest", 0L);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerData.put(uuid, data);
                    if (data.getNickname() != null) {
                        player.setDisplayName(data.getNickname());
                        player.setPlayerListName(data.getNickname());
                    }
                    future.complete(data);
                });
            });
        } else {
            PlayerData data = createDefaultPlayerData(player);
            future.complete(data);
        }
        return future;
    }

    public class PlayerData {
        private boolean isFlying = false;
        private boolean isVanished = false;
        private boolean isFrozen = false;
        private boolean isGodMode = false;
        private GameMode gameMode = GameMode.SURVIVAL;
        private String nickname = null;
        private String group = "default";
        private final Map<String, org.bukkit.Location> homes = new java.util.HashMap<>();
        private long lastTeleportRequest = 0L;

        public boolean isFlying() {
            return isFlying;
        }

        public void setFlying(boolean flying) {
            isFlying = flying;
        }

        public boolean isVanished() {
            return isVanished;
        }

        public void setVanished(boolean vanished) {
            isVanished = vanished;
        }

        public boolean isFrozen() {
            return isFrozen;
        }

        public void setFrozen(boolean frozen) {
            isFrozen = frozen;
        }

        public GameMode getGameMode() {
            return gameMode;
        }

        public void setGameMode(GameMode gameMode) {
            this.gameMode = gameMode;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public boolean isGodMode() {
            return isGodMode;
        }

        public void setGodMode(boolean godMode) {
            this.isGodMode = godMode;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        // Home management
        public Map<String, org.bukkit.Location> getHomes() {
            return homes;
        }
        public void setHome(String homeName, org.bukkit.Location location) {
            homes.put(homeName, location);
        }
        public boolean deleteHome(String homeName) {
            if (!homes.containsKey(homeName)) return false;
            homes.remove(homeName);
            return true;
        }
        public org.bukkit.Location getHome(String homeName) {
            return homes.get(homeName);
        }
        // Teleport cooldown
        public void recordTeleportRequest() {
            lastTeleportRequest = System.currentTimeMillis();
        }
        public boolean hasTeleportCooldown(int cooldownSeconds) {
            if (lastTeleportRequest == 0) return false;
            long cooldownMillis = cooldownSeconds * 1000L;
            return System.currentTimeMillis() - lastTeleportRequest < cooldownMillis;
        }
        public int getRemainingCooldown(int cooldownSeconds) {
            if (!hasTeleportCooldown(cooldownSeconds)) return 0;
            long cooldownMillis = cooldownSeconds * 1000L;
            long elapsedMillis = System.currentTimeMillis() - lastTeleportRequest;
            long remainingMillis = cooldownMillis - elapsedMillis;
            return (int) Math.ceil(remainingMillis / 1000.0);
        }
    }

    public void loadConfig() {
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().info("Creating default config.yml...");
            plugin.saveDefaultConfig();
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        addDefaults();
        saveConfig();
    }

    private void addDefaults() {
        addDefault("teleport.delays.tp", 3);
        addDefault("teleport.delays.tpa", 3);
        addDefault("teleport.delays.spawn", 3);
        addDefault("teleport.delays.home", 3);
        addDefault("teleport.delays.warp", 3);
        addDefault("teleport.cooldowns.tp", 30);
        addDefault("teleport.cooldowns.tpa", 60);
        addDefault("teleport.cooldowns.spawn", 30);
        addDefault("teleport.cooldowns.home", 60);
        addDefault("teleport.cooldowns.warp", 30);
        addDefault("teleport.cancel-on-damage", true);
        addDefault("teleport.cancel-on-command", true);
        addDefault("permissions.teleport.nodelay", "ssjessentials.teleport.nodelay");
        addDefault("permissions.teleport.nocooldown", "ssjessentials.teleport.nocooldown");
    }

    private void addDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml");
            e.printStackTrace();
        }
    }

    public String getString(String path) {
        return config.getString(path);
    }
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    public int getInt(String path) {
        return config.getInt(path);
    }
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    public long getLong(String path) {
        return config.getLong(path);
    }
    public long getLong(String path, long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    public java.util.List<String> getStringList(String path) {
        return config.getStringList(path);
    }
    public FileConfiguration getFileConfiguration() {
        return config;
    }

    public void set(String path, Object value) {
        config.set(path, value);
    }
    public void saveConfigs() {
        saveConfig();
    }
} 