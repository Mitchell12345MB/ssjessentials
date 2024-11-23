package com.sausaliens;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.sausaliens.SSJECommands.SSJECommands;
import com.sausaliens.SSJEConfig.SSJConfigs;
import com.sausaliens.SSJEListeners.PlayerFlightListener;
import com.sausaliens.SSJEListeners.PlayerFreezeListener;
import com.sausaliens.SSJEPlayerData.SSJEPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SSJEssentials extends JavaPlugin {

    private SSJECommands commandExecutor;
    private SSJConfigs configs;
    private PlayerFlightListener playerFlightListener;

    // Add player data map
    private Map<UUID, SSJEPlayerData> playerDataMap;

    @Override
    public void onEnable() {
        this.configs = new SSJConfigs(this);
        this.commandExecutor = new SSJECommands(this);

        getCommand("fly").setExecutor(commandExecutor);
        getCommand("vanish").setExecutor(commandExecutor);
        getCommand("heal").setExecutor(commandExecutor);
        getCommand("feed").setExecutor(commandExecutor);
        getCommand("freeze").setExecutor(commandExecutor);

        this.playerFlightListener = new PlayerFlightListener(this);
        getServer().getPluginManager().registerEvents(playerFlightListener, this);

        getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);

        // Initialize player data map
        this.playerDataMap = new HashMap<>();

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
}
