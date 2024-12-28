package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class WelcomeManager {
    private final SSJEssentials plugin;
    private boolean enabled;
    private List<String> privateMessage;
    private List<String> broadcastMessage;
    private boolean showToReturningPlayers;

    public WelcomeManager(SSJEssentials plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("welcome.enabled", true);
        showToReturningPlayers = plugin.getConfig().getBoolean("welcome.show_to_returning_players", false);
        
        // Load and colorize private message
        privateMessage = plugin.getConfig().getStringList("welcome.private_message")
            .stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());

        // Load and colorize broadcast message
        broadcastMessage = plugin.getConfig().getStringList("welcome.broadcast_message")
            .stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
    }

    public void handlePlayerJoin(Player player) {
        if (!enabled) return;

        // Check if player is new or if we should show message to returning players
        if (!player.hasPlayedBefore() || showToReturningPlayers) {
            // Send private message
            for (String line : privateMessage) {
                player.sendMessage(replacePlaceholders(line, player));
            }

            // Broadcast welcome message
            for (String line : broadcastMessage) {
                Bukkit.broadcastMessage(replacePlaceholders(line, player));
            }
        }
    }

    private String replacePlaceholders(String message, Player player) {
        return message.replace("%player%", player.getName());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("welcome.enabled", enabled);
        plugin.saveConfig();
    }

    public void setShowToReturningPlayers(boolean show) {
        this.showToReturningPlayers = show;
        plugin.getConfig().set("welcome.show_to_returning_players", show);
        plugin.saveConfig();
    }

    public void setPrivateMessage(List<String> message) {
        this.privateMessage = message.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
        plugin.getConfig().set("welcome.private_message", message);
        plugin.saveConfig();
    }

    public void setBroadcastMessage(List<String> message) {
        this.broadcastMessage = message.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
        plugin.getConfig().set("welcome.broadcast_message", message);
        plugin.saveConfig();
    }
} 