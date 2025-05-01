package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEManagers.TeleportManager;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class TeleportListener implements Listener {
    private final SSJEssentials plugin;
    private final TeleportManager teleportManager;

    public TeleportListener(SSJEssentials plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }

    /**
     * Cancel teleport if player moves
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!teleportManager.hasPendingTeleport(player)) return;
        
        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Allow looking around but not moving
        if (to != null && (from.getBlockX() != to.getBlockX() || 
                          from.getBlockY() != to.getBlockY() || 
                          from.getBlockZ() != to.getBlockZ())) {
            
            // Get teleport type for a more detailed message
            String teleportType = teleportManager.getPendingTeleportType(player);
            Location destination = teleportManager.getDestination(player);
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Player " + player.getName() + " moved, cancelling " + 
                    teleportType + " teleport to " + formatLocation(destination));
            }
            
            teleportManager.cancelTeleport(player, true);
            // Check if we need to unregister this listener
            checkAndUnregister();
        }
    }
    
    /**
     * Cancel teleport if player takes damage
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!teleportManager.hasPendingTeleport(player)) return;
        
        // Check if damage should cancel teleport
        if (teleportManager.shouldCancelOnDamage()) {
            // Get teleport type for a more detailed message
            String teleportType = teleportManager.getPendingTeleportType(player);
            Location destination = teleportManager.getDestination(player);
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Player " + player.getName() + " took damage, cancelling " + 
                    teleportType + " teleport to " + formatLocation(destination));
            }
            
            teleportManager.cancelTeleport(player, true);
            // Check if we need to unregister this listener
            checkAndUnregister();
        }
    }
    
    /**
     * Clean up when player quits
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (teleportManager.hasPendingTeleport(player)) {
            // Get teleport type for a more detailed message
            String teleportType = teleportManager.getPendingTeleportType(player);
            Location destination = teleportManager.getDestination(player);
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Player " + player.getName() + " quit, cancelling " + 
                    teleportType + " teleport to " + formatLocation(destination));
            }
            
            teleportManager.cancelTeleport(player, false);
            // Check if we need to unregister this listener
            checkAndUnregister();
        }
    }
    
    /**
     * Cancel teleport if player executes a command
     */
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!teleportManager.hasPendingTeleport(player)) return;
        
        // Check if command execution should cancel teleport
        if (teleportManager.shouldCancelOnCommand()) {
            // Ignore teleport-related commands that should not cancel teleport
            String command = event.getMessage().toLowerCase();
            if (command.startsWith("/tpaccept") || command.startsWith("/tpyes") ||
                command.startsWith("/tpdeny") || command.startsWith("/tpno")) {
                return;
            }
            
            // Get teleport type for a more detailed message
            String teleportType = teleportManager.getPendingTeleportType(player);
            Location destination = teleportManager.getDestination(player);
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Player " + player.getName() + " executed command '" + command + 
                    "', cancelling " + teleportType + " teleport to " + formatLocation(destination));
            }
            
            teleportManager.cancelTeleport(player, true);
            // Check if we need to unregister this listener
            checkAndUnregister();
        }
    }
    
    /**
     * Check if there are any pending teleports and unregister this listener if not
     */
    private void checkAndUnregister() {
        // Schedule a task to run after the current server tick to check if any teleports remain
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Get count of pending teleports across all players
            int pendingCount = teleportManager.getPendingTeleportCount();
            
            // If no pending teleports remain, unregister this listener
            if (pendingCount == 0) {
                if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                    plugin.getLogger().info("No pending teleports remain, unregistering TeleportListener");
                }
                HandlerList.unregisterAll(this);
            }
        }, 1L); // Run 1 tick later to ensure all teleport operations are complete
    }
    
    /**
     * Format a location for logging
     * @param loc The location to format
     * @return A formatted string representation of the location
     */
    private String formatLocation(Location loc) {
        if (loc == null) return "unknown";
        return loc.getWorld().getName() + ":" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }
} 