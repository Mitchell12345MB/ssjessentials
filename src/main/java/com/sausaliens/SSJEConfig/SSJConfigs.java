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
        
        playerData.put(player.getUniqueId(), data);
        
        if (data.getNickname() != null) {
            player.setDisplayName(data.getNickname());
            player.setPlayerListName(data.getNickname());
        }
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
            config.set("godmode", data.isGodMode());
            config.set("gamemode", data.getGameMode().toString());
            config.set("nickname", data.getNickname());
            config.set("group", data.getGroup());
            
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
        private boolean isGodMode = false;
        private GameMode gameMode = GameMode.SURVIVAL;
        private String nickname = null;
        private String group = "default";

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
    }
} 