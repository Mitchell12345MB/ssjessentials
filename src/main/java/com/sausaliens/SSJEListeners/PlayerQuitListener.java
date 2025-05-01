package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener that handles player quit events
 */
public class PlayerQuitListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerQuitListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear the player from the group cache to free up memory
        plugin.getGroupManager().clearPlayerCache(player);
        // No need to remove player data here; async system manages it.
    }
} 