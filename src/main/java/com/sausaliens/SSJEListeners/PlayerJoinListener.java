package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.sausaliens.SSJEConfig.SSJConfigs;

public class PlayerJoinListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerJoinListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Get player data
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(event.getPlayer());
        
        // If player has no group or their group doesn't exist, set them to default
        String currentGroup = playerData.getGroup();
        if (currentGroup == null || currentGroup.isEmpty() || 
            !plugin.getGroupManager().getGroups().contains(currentGroup.toLowerCase())) {
            plugin.getGroupManager().setPlayerGroup(event.getPlayer(), "default");
        }
        
        // Update the player's tab list name with their group prefix
        plugin.getGroupManager().updatePlayerTabName(event.getPlayer());
    }
} 