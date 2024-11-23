package com.sausaliens.SSJEListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import com.sausaliens.SSJEssentials;

public class PlayerFreezeListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerFreezeListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.getConfigs().getPlayerData(player).isFrozen()) {
            // Allow head movement but prevent position changes
            if (event.getFrom().getX() != event.getTo().getX() 
                || event.getFrom().getY() != event.getTo().getY() 
                || event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
        }
    }
} 