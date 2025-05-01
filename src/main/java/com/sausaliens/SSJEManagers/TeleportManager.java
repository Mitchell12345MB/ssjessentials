package com.sausaliens.SSJEManagers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEListeners.TeleportListener;

public class TeleportManager {
    private final Map<UUID, UUID> teleportRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Long> requestTimestamps = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    
    // For teleport delays
    private final Map<UUID, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Location> teleportInitialLocations = new ConcurrentHashMap<>();
    
    private final long requestTimeout;
    private final long cooldownDuration;
    private final boolean delaysEnabled;
    private final int tpCommandDelay;
    private final int tprCommandDelay;
    private final int spawnCommandDelay;
    private final int warpCommandDelay;
    private final int homeCommandDelay;
    private final boolean cancelOnMove;
    private final String bypassPermission;
    private final String countdownMessage;
    private final String cancelledMessage;

    private final SSJEssentials plugin;
    private TeleportListener teleportListener;

    public TeleportManager(SSJEssentials plugin) {
        this.plugin = plugin;
        
        // Use configManager instead of getConfig()
        this.requestTimeout = plugin.getConfigs().getLong("teleport.teleport-request-timeout", 60000);
        this.cooldownDuration = plugin.getConfigs().getLong("teleport.teleport-request-delay", 1000);
        
        // Load teleport delay settings
        this.delaysEnabled = plugin.getConfigs().getBoolean("teleport.delays.enabled", true);
        this.tpCommandDelay = plugin.getConfigs().getInt("teleport.delays.tp-command", 3);
        this.tprCommandDelay = plugin.getConfigs().getInt("teleport.delays.tpr-command", 5);
        this.spawnCommandDelay = plugin.getConfigs().getInt("teleport.delays.spawn-command", 3);
        this.warpCommandDelay = plugin.getConfigs().getInt("teleport.delays.warp-command", 3);
        this.homeCommandDelay = plugin.getConfigs().getInt("teleport.delays.home-command", 3);
        this.cancelOnMove = plugin.getConfigs().getBoolean("teleport.delays.cancel-on-move", true);
        this.bypassPermission = plugin.getConfigs().getString("teleport.delays.bypass-permission", "ssjessentials.teleport.nodelay");
        this.countdownMessage = plugin.getConfigs().getString("teleport.delays.countdown-message", "&aTeleporting in &e%time% &aseconds. Don't move!");
        this.cancelledMessage = plugin.getConfigs().getString("teleport.delays.cancelled-message", "&cTeleport cancelled due to movement!");
        
        plugin.getLogger().info("TeleportManager initialized with timeout: " + 
            TimeUnit.MILLISECONDS.toSeconds(requestTimeout) + "s, cooldown: " + 
            TimeUnit.MILLISECONDS.toSeconds(cooldownDuration) + "s");
        plugin.getLogger().info("Teleport delays: " + (delaysEnabled ? "enabled" : "disabled"));
    }

    /**
     * Creates a teleport request from requester to target
     * 
     * @param requester The player requesting the teleport
     * @param target The player to teleport to
     * @return true if request was created, false if on cooldown
     */
    public boolean createRequest(Player requester, Player target) {
        if (isOnCooldown(requester)) {
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Teleport request from " + requester.getName() + 
                    " to " + target.getName() + " denied: on cooldown");
            }
            return false;
        }
        
        teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
        requestTimestamps.put(target.getUniqueId(), System.currentTimeMillis());
        setCooldown(requester);
        
        if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Teleport request from " + requester.getName() + 
                " to " + target.getName() + " created");
        }
        return true;
    }

    /**
     * Checks if a player has an active teleport request
     * 
     * @param target The player to check
     * @return true if there's an active request for the player
     */
    public boolean hasActiveRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!teleportRequests.containsKey(targetUUID)) return false;
        
        long timestamp = requestTimestamps.getOrDefault(targetUUID, 0L);
        if (System.currentTimeMillis() - timestamp > requestTimeout) {
            removeRequest(target);
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Teleport request to " + target.getName() + 
                    " expired and was removed");
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the player who requested to teleport
     * 
     * @param target The player who received the request
     * @return The requester, or null if not found or offline
     */
    public Player getRequester(Player target) {
        UUID requesterUUID = teleportRequests.get(target.getUniqueId());
        if (requesterUUID == null) return null;
        
        Player requester = plugin.getServer().getPlayer(requesterUUID);
        
        if (requester == null && plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Requester for " + target.getName() + 
                " is no longer online");
        }
        
        return requester;
    }

    /**
     * Removes a teleport request
     * 
     * @param target The player who received the request
     */
    public void removeRequest(Player target) {
        teleportRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());
        
        if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Teleport request to " + target.getName() + " removed");
        }
    }
    
    /**
     * Checks if a player is on cooldown for sending teleport requests
     * 
     * @param player The player to check
     * @return true if player is on cooldown
     */
    public boolean isOnCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!cooldowns.containsKey(playerUUID)) return false;
        
        long lastRequestTime = cooldowns.get(playerUUID);
        boolean onCooldown = System.currentTimeMillis() - lastRequestTime < cooldownDuration;
        
        if (!onCooldown) {
            cooldowns.remove(playerUUID);
        }
        
        return onCooldown;
    }
    
    /**
     * Places a player on cooldown for sending teleport requests
     * 
     * @param player The player to set on cooldown
     */
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Gets the remaining cooldown time in seconds
     * 
     * @param player The player to check
     * @return Remaining cooldown time in seconds, 0 if not on cooldown
     */
    public int getRemainingCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!cooldowns.containsKey(playerUUID)) return 0;
        
        long lastRequestTime = cooldowns.get(playerUUID);
        long elapsedTime = System.currentTimeMillis() - lastRequestTime;
        long remainingTime = cooldownDuration - elapsedTime;
        
        return remainingTime > 0 ? (int) TimeUnit.MILLISECONDS.toSeconds(remainingTime) + 1 : 0;
    }
    
    /**
     * Checks if teleport delays are enabled
     * 
     * @return true if delays are enabled
     */
    public boolean areDelaysEnabled() {
        return delaysEnabled;
    }
    
    /**
     * Gets the delay for a specific teleport command type
     * 
     * @param type The type of teleport command
     * @return The delay in seconds
     */
    public int getDelayForCommand(TeleportType type) {
        return switch (type) {
            case TP -> tpCommandDelay;
            case TPR -> tprCommandDelay;
            case SPAWN -> spawnCommandDelay;
            case WARP -> warpCommandDelay;
            case HOME -> homeCommandDelay;
        };
    }
    
    /**
     * Check if a player can bypass teleport delays
     * 
     * @param player The player to check
     * @return true if player can bypass delays
     */
    public boolean canBypassDelay(Player player) {
        return player.hasPermission(bypassPermission);
    }
    
    /**
     * Checks if a player has a pending teleport
     * 
     * @param player The player to check
     * @return true if player has pending teleport
     */
    public boolean hasPendingTeleport(Player player) {
        if (player == null) return false;
        return pendingTeleports.containsKey(player.getUniqueId());
    }
    
    /**
     * Cancels a pending teleport
     * 
     * @param player The player whose teleport to cancel
     * @param sendMessage Whether to send the cancelled message
     * @return true if a teleport was cancelled
     */
    public boolean cancelPendingTeleport(Player player, boolean sendMessage) {
        UUID playerUUID = player.getUniqueId();
        PendingTeleport task = pendingTeleports.remove(playerUUID);
        if (task != null) {
            task.getTask().cancel();
            teleportInitialLocations.remove(playerUUID);
            
            if (sendMessage) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', cancelledMessage));
            }
            
            if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
                plugin.getLogger().info("Pending teleport for " + player.getName() + " was cancelled");
            }
            return true;
        }
        return false;
    }
    
    /**
     * Initiates a delayed teleport for a player
     * 
     * @param player The player to teleport
     * @param destination The destination to teleport to
     * @param type The type of teleport command
     * @return true if teleport was initiated, false if bypassed or already has pending teleport
     */
    public boolean initiateDelayedTeleport(Player player, Location destination, TeleportType type) {
        // Check if delays are disabled or player can bypass
        if (!delaysEnabled || canBypassDelay(player) || getDelayForCommand(type) <= 0) {
            player.teleport(destination);
            return false;
        }
        
        // Check if player already has pending teleport
        if (hasPendingTeleport(player)) {
            cancelPendingTeleport(player, true);
        }
        
        // Store initial location if we want to cancel on move
        if (cancelOnMove) {
            teleportInitialLocations.put(player.getUniqueId(), player.getLocation().clone());
        }
        
        final int delay = getDelayForCommand(type);
        final UUID playerUUID = player.getUniqueId();
        
        // Send countdown message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            countdownMessage.replace("%time%", String.valueOf(delay))));
        
        // Schedule teleport task
        BukkitTask task = new BukkitRunnable() {
            private int secondsLeft = delay;
            
            @Override
            public void run() {
                // Check if player is still online
                Player p = plugin.getServer().getPlayer(playerUUID);
                if (p == null || !p.isOnline()) {
                    cancel();
                    pendingTeleports.remove(playerUUID);
                    teleportInitialLocations.remove(playerUUID);
                    return;
                }
                
                // Check if player moved
                if (cancelOnMove && teleportInitialLocations.containsKey(playerUUID)) {
                    Location initialLoc = teleportInitialLocations.get(playerUUID);
                    Location currentLoc = p.getLocation();
                    
                    // Compare if player moved (ignore looking direction)
                    if (initialLoc.getWorld() != currentLoc.getWorld() ||
                        initialLoc.getBlockX() != currentLoc.getBlockX() ||
                        initialLoc.getBlockY() != currentLoc.getBlockY() ||
                        initialLoc.getBlockZ() != currentLoc.getBlockZ()) {
                        
                        // Player moved, cancel teleport
                        cancel();
                        pendingTeleports.remove(playerUUID);
                        teleportInitialLocations.remove(playerUUID);
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', cancelledMessage));
                        return;
                    }
                }
                
                // Countdown
                secondsLeft--;
                
                if (secondsLeft <= 0) {
                    // Time's up, teleport the player
                    p.teleport(destination);
                    pendingTeleports.remove(playerUUID);
                    teleportInitialLocations.remove(playerUUID);
                    cancel();
                    
                    // Clean up the listener if no more pending teleports
                    if (pendingTeleports.isEmpty() && teleportListener != null) {
                        HandlerList.unregisterAll(teleportListener);
                        teleportListener = null;
                    }
                } else if (secondsLeft <= 3) {
                    // Show countdown for last 3 seconds
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        countdownMessage.replace("%time%", String.valueOf(secondsLeft))));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
        
        // Store the task
        pendingTeleports.put(playerUUID, new PendingTeleport(player.getLocation().clone(), destination, type.name(), task));
        
        // Register listener if not already registered
        if (teleportListener == null) {
            teleportListener = new TeleportListener(plugin, this);
            plugin.getServer().getPluginManager().registerEvents(teleportListener, plugin);
        }
        
        if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Initiated delayed teleport for " + player.getName() + 
                " with delay of " + delay + " seconds");
        }
        
        return true;
    }
    
    /**
     * Enum for different teleport command types
     */
    public enum TeleportType {
        TP, TPR, SPAWN, WARP, HOME
    }
    
    /**
     * Cleans up expired requests to prevent memory leaks
     */
    public void cleanupExpiredRequests() {
        long currentTime = System.currentTimeMillis();
        int removedRequests = 0;
        int removedCooldowns = 0;
        
        // Remove expired requests
        Iterator<Map.Entry<UUID, Long>> requestIterator = requestTimestamps.entrySet().iterator();
        while (requestIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = requestIterator.next();
            if (currentTime - entry.getValue() > requestTimeout) {
                UUID targetUUID = entry.getKey();
                teleportRequests.remove(targetUUID);
                requestIterator.remove();
                removedRequests++;
            }
        }
        
        // Remove expired cooldowns
        Iterator<Map.Entry<UUID, Long>> cooldownIterator = cooldowns.entrySet().iterator();
        while (cooldownIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = cooldownIterator.next();
            if (currentTime - entry.getValue() > cooldownDuration) {
                cooldownIterator.remove();
                removedCooldowns++;
            }
        }
        
        // Only log if something was actually cleaned up
        if ((removedRequests > 0 || removedCooldowns > 0) && 
            plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Cleaned up " + removedRequests + 
                " expired teleport requests and " + removedCooldowns + " cooldowns");
        }
    }
    
    /**
     * Gets the total number of active teleport requests
     * 
     * @return The count of active requests
     */
    public int getActiveRequestCount() {
        cleanupExpiredRequests(); // Clean up to get accurate count
        return teleportRequests.size();
    }
    
    /**
     * Gets the total number of players on cooldown
     * 
     * @return The count of players on cooldown
     */
    public int getPlayersOnCooldownCount() {
        long currentTime = System.currentTimeMillis();
        
        // Count only valid cooldowns
        return (int) cooldowns.entrySet().stream()
            .filter(entry -> currentTime - entry.getValue() < cooldownDuration)
            .count();
    }

    /**
     * Teleport a player with delay
     * @param player The player to teleport
     * @param destination The destination to teleport to
     * @param teleportType The type of teleport (tp, tpr, spawn, home, warp)
     * @return True if teleport was initiated, false if not
     */
    public boolean teleport(Player player, Location destination, String teleportType) {
        if (player == null || destination == null) return false;
        
        // Check if player has bypass permission
        if (player.hasPermission(bypassPermission)) {
            player.teleport(destination);
            return true;
        }
        
        // Get delay from config
        int delay = plugin.getConfigs().getInt("teleport.delays." + teleportType, 3);
        
        // If delay is 0, teleport immediately
        if (delay <= 0) {
            player.teleport(destination);
            return true;
        }
        
        // Check if player already has a pending teleport
        if (hasPendingTeleport(player)) {
            player.sendMessage(plugin.formatMessage("§cYou already have a pending teleport!"));
            return false;
        }
        
        // Create pending teleport
        player.sendMessage(plugin.formatMessage("§eTeleporting in §6" + delay + " §eseconds. §cDon't move!"));
        
        // Store player's initial location
        Location initialLocation = player.getLocation().clone();
        
        // Create teleport task
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!hasPendingTeleport(player)) return;
            
            player.teleport(destination);
            player.sendMessage(plugin.formatMessage("§aTeleported!"));
            
            // Remove pending teleport
            pendingTeleports.remove(player.getUniqueId());
            
            // Unregister listener if no more pending teleports
            if (pendingTeleports.isEmpty() && teleportListener != null) {
                HandlerList.unregisterAll(teleportListener);
                teleportListener = null;
            }
        }, delay * 20L);  // Convert seconds to ticks
        
        // Store pending teleport
        pendingTeleports.put(player.getUniqueId(), new PendingTeleport(initialLocation, destination, teleportType, task));
        
        // Register listener if not already registered
        if (teleportListener == null) {
            teleportListener = new TeleportListener(plugin, this);
            plugin.getServer().getPluginManager().registerEvents(teleportListener, plugin);
        }
        
        return true;
    }
    
    /**
     * Cancel a pending teleport
     * @param player The player whose teleport to cancel
     * @param sendMessage Whether to send a cancellation message
     */
    public void cancelTeleport(Player player, boolean sendMessage) {
        if (player == null) return;
        
        PendingTeleport pendingTeleport = pendingTeleports.get(player.getUniqueId());
        if (pendingTeleport == null) return;
        
        // Get teleport information for logging
        String teleportType = pendingTeleport.getTeleportType();
        Location destination = pendingTeleport.getDestination();
        
        // Cancel task
        pendingTeleport.getTask().cancel();
        
        // Remove pending teleport
        pendingTeleports.remove(player.getUniqueId());
        
        // Send message
        if (sendMessage) {
            player.sendMessage(plugin.formatMessage("§cTeleport cancelled!"));
        }
        
        // Log if debug mode is enabled
        if (plugin.getConfigs().getBoolean("teleport.debug-mode", false)) {
            plugin.getLogger().info("Cancelled " + teleportType + " teleport for " + player.getName() + 
                " to " + formatLocation(destination));
        }
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
    
    /**
     * Get a player's initial location before teleporting
     * @param player The player to check
     * @return The player's initial location, or null if not found
     */
    public Location getInitialLocation(Player player) {
        if (player == null) return null;
        
        PendingTeleport pendingTeleport = pendingTeleports.get(player.getUniqueId());
        if (pendingTeleport == null) return null;
        
        return pendingTeleport.getInitialLocation();
    }
    
    /**
     * Get a player's intended destination for their pending teleport
     * @param player The player to check
     * @return The destination location, or null if not found
     */
    public Location getDestination(Player player) {
        if (player == null) return null;
        
        PendingTeleport pendingTeleport = pendingTeleports.get(player.getUniqueId());
        if (pendingTeleport == null) return null;
        
        return pendingTeleport.getDestination();
    }
    
    /**
     * Get the type of teleport pending for a player
     * @param player The player to check
     * @return The teleport type as a string, or null if not found
     */
    public String getPendingTeleportType(Player player) {
        if (player == null) return null;
        
        PendingTeleport pendingTeleport = pendingTeleports.get(player.getUniqueId());
        if (pendingTeleport == null) return null;
        
        return pendingTeleport.getTeleportType();
    }
    
    /**
     * Get the count of pending teleports across all players
     * @return The number of pending teleports
     */
    public int getPendingTeleportCount() {
        return pendingTeleports.size();
    }

    /**
     * Inner class to store pending teleport data
     */
    private static class PendingTeleport {
        private final Location initialLocation;
        private final Location destination;
        private final String teleportType;
        private final BukkitTask task;
        
        public PendingTeleport(Location initialLocation, Location destination, String teleportType, BukkitTask task) {
            this.initialLocation = initialLocation;
            this.destination = destination;
            this.teleportType = teleportType;
            this.task = task;
        }
        
        public Location getInitialLocation() {
            return initialLocation;
        }
        
        public Location getDestination() {
            return destination;
        }
        
        public String getTeleportType() {
            return teleportType;
        }
        
        public BukkitTask getTask() {
            return task;
        }
    }

    /**
     * Check if teleport should be canceled on damage
     * @return true if teleport should be canceled when player takes damage
     */
    public boolean shouldCancelOnDamage() {
        return plugin.getConfigs().getBoolean("teleport.cancel-on-damage", true);
    }
    
    /**
     * Check if teleport should be canceled on command execution
     * @return true if teleport should be canceled when player executes a command
     */
    public boolean shouldCancelOnCommand() {
        return plugin.getConfigs().getBoolean("teleport.cancel-on-command", true);
    }
} 