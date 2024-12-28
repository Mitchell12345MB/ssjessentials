package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.sausaliens.SSJEConfig.SSJConfigs;
import org.bukkit.entity.Player;

public class PlayerJoinListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerJoinListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Play join sound for all players only if it's a new player
        if (!player.hasPlayedBefore()) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
        
        // Handle welcome message
        plugin.getWelcomeManager().handlePlayerJoin(player);
        
        // Set default group for new players
        if (!player.hasPlayedBefore()) {
            plugin.getGroupManager().setPlayerGroup(player, "default");
        }
        
        // Update tab list
        plugin.getTabListManager().updatePlayer(player);
        
        // Get player data
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(player);
        
        // If player has no group or their group doesn't exist, set them to default
        String currentGroup = playerData.getGroup();
        if (currentGroup == null || currentGroup.isEmpty() || 
            !plugin.getGroupManager().getGroups().contains(currentGroup.toLowerCase())) {
            plugin.getGroupManager().setPlayerGroup(player, "default");
        }
        
        // Update the player's tab list name with their group prefix
        plugin.getGroupManager().updatePlayerTabName(player);
    }
} 