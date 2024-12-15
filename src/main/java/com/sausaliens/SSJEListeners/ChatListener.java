package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
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
        String prefix = plugin.getGroupManager().getPlayerPrefix(event.getPlayer());
        String suffix = plugin.getGroupManager().getPlayerSuffix(event.getPlayer());
        String playerName = event.getPlayer().getDisplayName();
        
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