package com.sausaliens;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sausaliens.SSJECommands.GroupCommand;
import com.sausaliens.SSJECommands.SSJECommands;
import com.sausaliens.SSJEConfig.SSJConfigs;
import com.sausaliens.SSJEListeners.ChatListener;
import com.sausaliens.SSJEListeners.PlayerFlightListener;
import com.sausaliens.SSJEListeners.PlayerFreezeListener;
import com.sausaliens.SSJEPlayerData.SSJEPlayerData;
import com.sausaliens.SSJEListeners.SSJECommandListener;
import com.sausaliens.SSJEListeners.PlayerJoinListener;
import com.sausaliens.SSJEConfig.SpawnConfig;
import com.sausaliens.SSJEManagers.TeleportManager;
import com.sausaliens.SSJEManagers.GroupManager;
import com.sausaliens.SSJEManagers.LocationManager;
import com.sausaliens.SSJEManagers.ConfigWatcher;

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
    private LocationManager locationManager;
    private GroupManager groupManager;

    private ConfigWatcher configWatcher;
    
    @Override
    public void onEnable() {
        // Save default config first
        saveDefaultConfig();
        
        // Initialize configs first
        this.configs = new SSJConfigs(this);
        
        // Initialize managers
        this.groupManager = new GroupManager(this);
        this.locationManager = new LocationManager(this);
        this.teleportManager = new TeleportManager(this);
        this.spawnConfig = new SpawnConfig(this);
        
        // Initialize command executor after managers
        this.commandExecutor = new SSJECommands(this);
        
        // Initialize config watcher last
        this.configWatcher = new ConfigWatcher(this);

        // Register group command
        GroupCommand groupCommand = new GroupCommand(this, groupManager);
        getCommand("group").setExecutor(groupCommand);
        getCommand("group").setTabCompleter(groupCommand);

        // Register chat listener for prefixes and suffixes
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        // Register other commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Initialize player data map
        this.playerDataMap = new HashMap<>();

        getLogger().info("SSJEssentials has been enabled!");
    }

    private void registerCommands() {
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
        getCommand("/ban").setExecutor(commandExecutor);
        getCommand("editwarp").setExecutor(commandExecutor);
        getCommand("resetwarp").setExecutor(commandExecutor);
        getCommand("warpall").setExecutor(commandExecutor);
        getCommand("spawnall").setExecutor(commandExecutor);
        getCommand("warp").setExecutor(commandExecutor);
        getCommand("setwarp").setExecutor(commandExecutor);
        getCommand("delwarp").setExecutor(commandExecutor);
        getCommand("/warp").setExecutor(commandExecutor);
        getCommand("home").setExecutor(commandExecutor);
        getCommand("sethome").setExecutor(commandExecutor);
        getCommand("delhome").setExecutor(commandExecutor);
        getCommand("/home").setExecutor(commandExecutor);
        getCommand("edithome").setExecutor(commandExecutor);
    }

    private void registerListeners() {
        this.playerFlightListener = new PlayerFlightListener(this);
        getServer().getPluginManager().registerEvents(playerFlightListener, this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new SSJECommandListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        if (configs != null) {
            configs.reloadConfig();
        }
        
        if (groupManager != null) {
            groupManager.loadGroups();
            
            // Update all online players' tab list names after reload
            for (Player player : getServer().getOnlinePlayers()) {
                groupManager.updatePlayerTabName(player);
            }
        }
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

    public SpawnConfig getSpawnConfig() {
        return spawnConfig;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public ConfigWatcher getConfigWatcher() {
        return configWatcher;
    }
}