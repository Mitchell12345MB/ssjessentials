package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportCancelListener implements Listener {
    private final SSJEssentials plugin;

    public TeleportCancelListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    /**
     * Cancel teleport on movement
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only cancel if the player moved to a different block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        plugin.getTeleportManager().cancelTeleport(player, true);
    }
    
    /**
     * Cancel teleport on damage
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        // Check if cancelling on damage is enabled
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-damage", true)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        plugin.getTeleportManager().cancelTeleport(player, true);
    }
    
    /**
     * Cancel teleport on command
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Check if cancelling on command is enabled
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-command", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        plugin.getTeleportManager().cancelTeleport(player, true);
    }
    
    /**
     * Cancel teleport on quit
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getTeleportManager().cancelTeleport(player, false);
    }
} 