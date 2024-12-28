package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

public class ChatListener implements Listener {
    private final SSJEssentials plugin;

    public ChatListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        String prefix = plugin.getGroupManager().getPlayerPrefix(sender);
        String suffix = plugin.getGroupManager().getPlayerSuffix(sender);
        String playerName = sender.getDisplayName();
        
        // Check if sender is staff
        if (sender.hasPermission("ssjessentials.mod") || sender.hasPermission("ssjessentials.admin")) {
            // Check for player mentions in the message
            for (Player target : Bukkit.getOnlinePlayers()) {
                String targetName = target.getName();
                String targetNick = target.getDisplayName();
                
                // If the message contains the player's full name or nickname
                if (message.contains(targetName) || message.contains(targetNick)) {
                    // Play sound only for the mentioned player
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    });
                }
            }
        }
        
        // Build the format based on whether there's a prefix/suffix
        StringBuilder format = new StringBuilder();
        
        // Add prefix if it exists (not empty)
        if (!prefix.trim().isEmpty()) {
            format.append(prefix).append("");
        }
        
        // Add player name in angle brackets
        format.append("&f<").append(playerName).append("&f>");
        
        // Add suffix if it exists (not empty)
        if (!suffix.trim().isEmpty()) {
            format.append("").append(suffix);
        }
        
        // Add message in white
        format.append(" &f%2$s");
        
        // Set the chat format
        event.setFormat(ChatColor.translateAlternateColorCodes('&', format.toString()));
    }
} 