package com.sausaliens;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;

import com.sausaliens.SSJECommands.AutoMessageCommand;
import com.sausaliens.SSJECommands.GroupCommand;
import com.sausaliens.SSJECommands.InvSeeCommand;
import com.sausaliens.SSJECommands.PMCommand;
import com.sausaliens.SSJECommands.WhoisCommand;
import com.sausaliens.SSJECommands.SocialSpyCommand;
import com.sausaliens.SSJECommands.BackCommand;
import com.sausaliens.SSJEConfig.SSJConfigs;
import com.sausaliens.SSJEListeners.ChatListener;
import com.sausaliens.SSJEListeners.PlayerFlightListener;
import com.sausaliens.SSJEListeners.PlayerFreezeListener;
import com.sausaliens.SSJEListeners.SSJECommandListener;
import com.sausaliens.SSJEListeners.PlayerJoinListener;
import com.sausaliens.SSJEConfig.SpawnConfig;
import com.sausaliens.SSJEManagers.TeleportManager;
import com.sausaliens.SSJEManagers.GroupManager;
import com.sausaliens.SSJEManagers.LocationManager;
import com.sausaliens.SSJEManagers.ConfigWatcher;
import com.sausaliens.SSJEManagers.AutoMessageManager;
import com.sausaliens.SSJEManagers.TabListManager;
import com.sausaliens.SSJEManagers.WelcomeManager;
import com.sausaliens.SSJEManagers.BanHistoryManager;
import com.sausaliens.SSJEListeners.ServerListPingListener;
import com.sausaliens.SSJEListeners.WarpSignListener;
import com.sausaliens.SSJEListeners.PlayerRespawnListener;
import com.sausaliens.SSJECommands.HealthCommands;
import com.sausaliens.SSJECommands.ModerationCommands;
import com.sausaliens.SSJECommands.MovementCommands;
import com.sausaliens.SSJECommands.TeleportCommands;
import com.sausaliens.SSJECommands.LocationCommands;
import com.sausaliens.SSJECommands.GameModeCommands;
import com.sausaliens.SSJECommands.AdminCommands;
import com.sausaliens.SSJECommands.NicknameCommands;
import com.sausaliens.SSJEListeners.PlayerQuitListener;
import com.sausaliens.SSJEListeners.TeleportCancelListener;
import com.sausaliens.SSJECommands.HomeCommand;

public class SSJEssentials extends JavaPlugin {

    private SSJConfigs configs;
    private PlayerFlightListener playerFlightListener;

    private SpawnConfig spawnConfig;
    private TeleportManager teleportManager;
    private LocationManager locationManager;
    private GroupManager groupManager;
    private BanHistoryManager banHistoryManager;

    private ConfigWatcher configWatcher;
    
    private AutoMessageManager autoMessageManager;

    private TabListManager tabListManager;

    private WelcomeManager welcomeManager;

    private ServerListPingListener serverListPingListener;
    
    // Command classes
    private MovementCommands movementCommands;
    private ModerationCommands moderationCommands;
    private HealthCommands healthCommands;
    private TeleportCommands teleportCommands;
    private LocationCommands locationCommands;
    private GameModeCommands gameModeCommands;
    private AdminCommands adminCommands;
    private NicknameCommands nicknameCommands;

    @Override
    public void onEnable() {
        // Check for ProtocolLib
        if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            getLogger().severe("ProtocolLib is required for SSJEssentials to work properly!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save default config first
        saveDefaultConfig();
        
        // Initialize configs first
        this.configs = new SSJConfigs(this);
        
        // Initialize managers
        this.groupManager = new GroupManager(this);
        this.locationManager = new LocationManager(this);
        this.teleportManager = new TeleportManager(this);
        this.spawnConfig = new SpawnConfig(this);
        this.banHistoryManager = new BanHistoryManager(this);
        
        // Initialize command classes
        this.movementCommands = new MovementCommands(this);
        this.moderationCommands = new ModerationCommands(this);
        this.healthCommands = new HealthCommands(this);
        this.teleportCommands = new TeleportCommands(this);
        this.locationCommands = new LocationCommands(this);
        this.gameModeCommands = new GameModeCommands(this);
        this.adminCommands = new AdminCommands(this);
        this.nicknameCommands = new NicknameCommands(this);
        
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
        
        // Register teleport cancel listener
        getServer().getPluginManager().registerEvents(new TeleportCancelListener(this), this);

        // Initialize AutoMessageManager
        autoMessageManager = new AutoMessageManager(this);
        
        // Register AutoMessageCommand
        getCommand("am").setExecutor(new AutoMessageCommand(this));
        getCommand("am").setTabCompleter(new AutoMessageCommand(this));

        // Initialize TabListManager
        tabListManager = new TabListManager(this);

        // Initialize WelcomeManager
        welcomeManager = new WelcomeManager(this);

        // Initialize ServerListPingListener
        serverListPingListener = new ServerListPingListener(this);
        
        // Schedule regular cleanup of expired teleport requests
        getServer().getScheduler().runTaskTimer(this, () -> {
            teleportManager.cleanupExpiredRequests();
        }, 1200L, 1200L); // Run every minute (20 ticks * 60 seconds)
        
        // Register home commands
        HomeCommand homeCommand = new HomeCommand(this);
        getCommand("home").setExecutor(homeCommand);
        getCommand("homes").setExecutor(homeCommand);
        getCommand("sethome").setExecutor(homeCommand);
        getCommand("delhome").setExecutor(homeCommand);
        
        getLogger().info("SSJEssentials has been enabled!");
    }

    private void registerCommands() {
        // Register movement commands
        getCommand("fly").setExecutor(movementCommands);
        getCommand("fly").setTabCompleter(movementCommands);
        getCommand("vanish").setExecutor(movementCommands);
        getCommand("vanish").setTabCompleter(movementCommands);
        
        // Register health commands
        getCommand("heal").setExecutor(healthCommands);
        getCommand("heal").setTabCompleter(healthCommands);
        getCommand("feed").setExecutor(healthCommands);
        getCommand("feed").setTabCompleter(healthCommands);
        
        // Register moderation commands
        getCommand("freeze").setExecutor(moderationCommands);
        getCommand("freeze").setTabCompleter(moderationCommands);
        getCommand("banlist").setExecutor(moderationCommands);
        getCommand("banlist").setTabCompleter(moderationCommands);
        getCommand("unban").setExecutor(moderationCommands);
        getCommand("unban").setTabCompleter(moderationCommands);
        getCommand("tempban").setExecutor(moderationCommands);
        getCommand("tempban").setTabCompleter(moderationCommands);
        getCommand("kill").setExecutor(moderationCommands);
        getCommand("kill").setTabCompleter(moderationCommands);
        getCommand("killall").setExecutor(moderationCommands);
        getCommand("killall").setTabCompleter(moderationCommands);
        getCommand("kick").setExecutor(moderationCommands);
        getCommand("kick").setTabCompleter(moderationCommands);
        getCommand("ban").setExecutor(moderationCommands);
        getCommand("ban").setTabCompleter(moderationCommands);
        getCommand("/ban").setExecutor(moderationCommands);
        getCommand("/ban").setTabCompleter(moderationCommands);
        
        // Register teleport commands
        getCommand("tp").setExecutor(teleportCommands);
        getCommand("tp").setTabCompleter(teleportCommands);
        getCommand("tpr").setExecutor(teleportCommands);
        getCommand("tpr").setTabCompleter(teleportCommands);
        getCommand("tpraccept").setExecutor(teleportCommands);
        getCommand("tpraccept").setTabCompleter(teleportCommands);
        
        // Register location commands
        getCommand("spawn").setExecutor(locationCommands);
        getCommand("spawn").setTabCompleter(locationCommands);
        getCommand("setspawn").setExecutor(locationCommands);
        getCommand("setspawn").setTabCompleter(locationCommands);
        getCommand("editwarp").setExecutor(locationCommands);
        getCommand("editwarp").setTabCompleter(locationCommands);
        getCommand("resetwarp").setExecutor(locationCommands);
        getCommand("resetwarp").setTabCompleter(locationCommands);
        getCommand("warpall").setExecutor(locationCommands);
        getCommand("warpall").setTabCompleter(locationCommands);
        getCommand("spawnall").setExecutor(locationCommands);
        getCommand("spawnall").setTabCompleter(locationCommands);
        getCommand("warp").setExecutor(locationCommands);
        getCommand("warp").setTabCompleter(locationCommands);
        getCommand("setwarp").setExecutor(locationCommands);
        getCommand("setwarp").setTabCompleter(locationCommands);
        getCommand("delwarp").setExecutor(locationCommands);
        getCommand("delwarp").setTabCompleter(locationCommands);
        getCommand("/warp").setExecutor(locationCommands);
        getCommand("/warp").setTabCompleter(locationCommands);
        getCommand("home").setExecutor(locationCommands);
        getCommand("home").setTabCompleter(locationCommands);
        getCommand("sethome").setExecutor(locationCommands);
        getCommand("sethome").setTabCompleter(locationCommands);
        getCommand("delhome").setExecutor(locationCommands);
        getCommand("delhome").setTabCompleter(locationCommands);
        getCommand("/home").setExecutor(locationCommands);
        getCommand("/home").setTabCompleter(locationCommands);
        getCommand("edithome").setExecutor(locationCommands);
        getCommand("edithome").setTabCompleter(locationCommands);
        
        // Register gamemode commands
        getCommand("gm").setExecutor(gameModeCommands);
        getCommand("gm").setTabCompleter(gameModeCommands);
        getCommand("god").setExecutor(gameModeCommands);
        getCommand("god").setTabCompleter(gameModeCommands);
        
        // Register admin commands
        getCommand("ssjereload").setExecutor(adminCommands);
        getCommand("ssjereload").setTabCompleter(adminCommands);
        
        // Register group command
        GroupCommand groupCommand = new GroupCommand(this, groupManager);
        getCommand("group").setExecutor(groupCommand);
        getCommand("group").setTabCompleter(groupCommand);

        // Register AutoMessageCommand
        getCommand("am").setExecutor(new AutoMessageCommand(this));
        getCommand("am").setTabCompleter(new AutoMessageCommand(this));

        // Register InvSeeCommand
        InvSeeCommand invSeeCommand = new InvSeeCommand(this);
        getCommand("invsee").setExecutor(invSeeCommand);
        getServer().getPluginManager().registerEvents(invSeeCommand, this);

        // Register PM command
        PMCommand pmCommand = new PMCommand(this);
        getCommand("pm").setExecutor(pmCommand);
        getCommand("pm").setTabCompleter(pmCommand);

        // Register Whois command
        WhoisCommand whoisCommand = new WhoisCommand(this);
        getCommand("whois").setExecutor(whoisCommand);
        getCommand("whois").setTabCompleter(whoisCommand);

        // Register SocialSpy command
        SocialSpyCommand socialSpyCommand = new SocialSpyCommand(this);
        getCommand("socialspy").setExecutor(socialSpyCommand);

        // Register Back command
        BackCommand backCommand = new BackCommand(this);
        getCommand("back").setExecutor(backCommand);
        getCommand("back").setTabCompleter(backCommand);
        getServer().getPluginManager().registerEvents(backCommand, this);

        // Register nick command
        getCommand("nick").setExecutor(nicknameCommands);
        getCommand("nick").setTabCompleter(nicknameCommands);
    }

    private void registerListeners() {
        this.playerFlightListener = new PlayerFlightListener(this);
        getServer().getPluginManager().registerEvents(playerFlightListener, this);
        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);
        getServer().getPluginManager().registerEvents(new SSJECommandListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new WarpSignListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        // ServerListPingListener is now handled through ProtocolLib
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        
        // Reload main configs
        if (configs != null) {
            configs.reloadConfig();
        }
        
        // Reload location systems
        if (locationManager != null) {
            locationManager.reload();
        }
        
        if (spawnConfig != null) {
            spawnConfig.reload();
        }
        
        // Reload player management systems
        if (groupManager != null) {
            groupManager.reload();
            
            // Update all online players' tab list names after reload
            for (Player player : getServer().getOnlinePlayers()) {
                groupManager.updatePlayerTabName(player);
            }
        }
        
        // Reload UI systems
        if (tabListManager != null) {
            tabListManager.loadConfig();
        }

        if (welcomeManager != null) {
            welcomeManager.loadConfig();
        }
        
        if (autoMessageManager != null) {
            autoMessageManager.reloadMessages();
        }

        if (serverListPingListener != null) {
            serverListPingListener.forceUpdate();
        }
        
        // Force teleport manager to reload by recreating it
        if (teleportManager != null) {
            // Save pending teleports
            teleportManager.cleanupExpiredRequests();
            // Create a new instance to reload with fresh config values
            teleportManager = new TeleportManager(this);
        }
        
        // Ban history needs to be reloaded by creating a new instance
        if (banHistoryManager != null) {
            // Save first
            banHistoryManager.saveUnbanHistory();
            // Create a new instance to reload
            banHistoryManager = new BanHistoryManager(this);
        }
        
        // Update the ConfigWatcher's file timestamps to prevent reload loops
        if (configWatcher != null) {
            configWatcher.updateFileTimestamps();
        }
        
        getLogger().info("All configurations and managers have been reloaded!");
    }

    @Override
    public void onDisable() {
        // Save location data
        if (locationManager != null) {
            locationManager.saveAllDataSync();
        }
        
        // Save groups data
        if (groupManager != null) {
            groupManager.saveAllDataSync();
        }
        
        // Save ban history data
        if (banHistoryManager != null) {
            banHistoryManager.saveUnbanHistorySync();
        }
        
        // Shut down managers
        if (tabListManager != null) {
            tabListManager.shutdown();
        }
        
        if (serverListPingListener != null) {
            serverListPingListener.unregister();
        }
        
        getLogger().info("SSJEssentials has been disabled!");
    }

    public SSJConfigs getConfigs() {
        return configs;
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

    public AutoMessageManager getAutoMessageManager() {
        return autoMessageManager;
    }

    public TabListManager getTabListManager() {
        return tabListManager;
    }

    public WelcomeManager getWelcomeManager() {
        return welcomeManager;
    }

    public ServerListPingListener getServerListPingListener() {
        return serverListPingListener;
    }
    
    public MovementCommands getMovementCommands() {
        return movementCommands;
    }
    
    public ModerationCommands getModerationCommands() {
        return moderationCommands;
    }
    
    public HealthCommands getHealthCommands() {
        return healthCommands;
    }
    
    public TeleportCommands getTeleportCommands() {
        return teleportCommands;
    }
    
    public LocationCommands getLocationCommands() {
        return locationCommands;
    }
    
    public GameModeCommands getGameModeCommands() {
        return gameModeCommands;
    }
    
    public AdminCommands getAdminCommands() {
        return adminCommands;
    }

    public NicknameCommands getNicknameCommands() {
        return nicknameCommands;
    }

    public BanHistoryManager getBanHistoryManager() {
        return banHistoryManager;
    }

    /**
     * Format a message with the plugin prefix
     * @param message The message to format
     * @return The formatted message
     */
    public String formatMessage(String message) {
        String prefix = configs.getString("settings.prefix", "&7[&bSSJE&7] ");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    @Override
    public FileConfiguration getConfig() {
        return super.getConfig();
    }
}