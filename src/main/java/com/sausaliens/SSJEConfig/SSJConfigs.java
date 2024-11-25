package com.sausaliens.SSJEConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;
import org.bukkit.GameMode;

public class SSJConfigs {
    private final SSJEssentials plugin;
    private final HashMap<UUID, PlayerData> playerData;
    private final File playerDataFolder;

    public SSJConfigs(SSJEssentials plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
        setupConfig();
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
            loadPlayerData(player);
        }
        return playerData.get(uuid);
    }

    private void loadPlayerData(Player player) {
        File playerFile = new File(playerDataFolder, player.getName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        PlayerData data = new PlayerData();
        if (playerFile.exists()) {
            data.setFlying(config.getBoolean("flying", false));
            data.setVanished(config.getBoolean("vanished", false));
            data.setFrozen(config.getBoolean("frozen", false));
            String gameModeStr = config.getString("gamemode", "SURVIVAL");
            data.setGameMode(GameMode.valueOf(gameModeStr));
        }
        
        playerData.put(player.getUniqueId(), data);
        savePlayerData(player);
    }

    public void savePlayerData(Player player) {
        File playerFile = new File(playerDataFolder, player.getName() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        
        PlayerData data = playerData.get(player.getUniqueId());
        if (data != null) {
            config.set("uuid", player.getUniqueId().toString());
            config.set("name", player.getName());
            config.set("flying", data.isFlying());
            config.set("vanished", data.isVanished());
            config.set("frozen", data.isFrozen());
            config.set("gamemode", data.getGameMode().toString());
            
            try {
                config.save(playerFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save data for player: " + player.getName());
            }
        }
    }

    public void saveAllData() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            savePlayerData(player);
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

    public class PlayerData {
        private boolean isFlying = false;
        private boolean isVanished = false;
        private boolean isFrozen = false;
        private GameMode gameMode = GameMode.SURVIVAL;

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
    }
} 