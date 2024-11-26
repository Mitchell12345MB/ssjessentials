package com.sausaliens;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sausaliens.SSJECommands.SSJECommands;
import com.sausaliens.SSJEConfig.SSJConfigs;
import com.sausaliens.SSJEListeners.PlayerFlightListener;
import com.sausaliens.SSJEListeners.PlayerFreezeListener;
import com.sausaliens.SSJEPlayerData.SSJEPlayerData;
import com.sausaliens.SSJEListeners.SSJECommandListener;
import com.sausaliens.SSJEListeners.PlayerJoinListener;
import com.sausaliens.SSJEConfig.SpawnConfig;
import com.sausaliens.SSJEManagers.TeleportManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SSJEssentials extends JavaPlugin {

    private SSJECommands commandExecutor;
    private SSJConfigs configs;
    private PlayerFlightListener playerFlightListener;

    // Add player data map
    private Map<UUID, SSJEPlayerData> playerDataMap;

    private SpawnConfig spawnConfig;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        this.configs = new SSJConfigs(this);
        this.commandExecutor = new SSJECommands(this);
        this.spawnConfig = new SpawnConfig(this);
        this.teleportManager = new TeleportManager(this);

        getCommand("fly").setExecutor(commandExecutor);
        getCommand("vanish").setExecutor(commandExecutor);
        getCommand("heal").setExecutor(commandExecutor);
        getCommand("feed").setExecutor(commandExecutor);
        getCommand("freeze").setExecutor(commandExecutor);
        getCommand("ssjereload").setExecutor(commandExecutor);
        getCommand("banlist").setExecutor(commandExecutor);
        getCommand("god").setExecutor(commandExecutor);
        getCommand("unban").setExecutor(commandExecutor);
        getCommand("nick").setExecutor(commandExecutor);
        getCommand("gm").setExecutor(commandExecutor);
        getCommand("tempban").setExecutor(commandExecutor);
        getCommand("spawn").setExecutor(commandExecutor);
        getCommand("setspawn").setExecutor(commandExecutor);
        getCommand("tpr").setExecutor(commandExecutor);
        getCommand("tp").setExecutor(commandExecutor);
        getCommand("tpraccept").setExecutor(commandExecutor);
        getCommand("kill").setExecutor(commandExecutor);
        getCommand("killall").setExecutor(commandExecutor);
        getCommand("kick").setExecutor(commandExecutor);
        getCommand("ban").setExecutor(commandExecutor);

        this.playerFlightListener = new PlayerFlightListener(this);
        getServer().getPluginManager().registerEvents(playerFlightListener, this);

        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);

        getServer().getPluginManager().registerEvents(new SSJECommandListener(this), this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Initialize player data map
        this.playerDataMap = new HashMap<>();

        saveDefaultConfig();

        getLogger().info("SSJEssentials has been enabled!");
    }

    @Override
    public void onDisable() {
        if (configs != null) {
            configs.saveAllData();
        }
        getLogger().info("SSJEssentials has been disabled!");
    }

    public SSJECommands getCommandExecutor() {
        return commandExecutor;
    }

    public SSJConfigs getConfigs() {
        return configs;
    }

    // Add methods to get and manage player data
    public SSJEPlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new SSJEPlayerData(uuid));
    }

    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public void reloadConfig() {
        super.reloadConfig();
        configs.reloadConfig();
    }

    public SpawnConfig getSpawnConfig() {
        return spawnConfig;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }
}