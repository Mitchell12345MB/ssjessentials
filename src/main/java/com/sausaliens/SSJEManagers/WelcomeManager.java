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
        enabled = plugin.getConfigs().getBoolean("welcome.enabled", true);
        showToReturningPlayers = plugin.getConfigs().getBoolean("welcome.show_to_returning_players", false);
        
        // Load and colorize private message
        privateMessage = plugin.getConfigs().getStringList("welcome.private_message")
            .stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());

        // Load and colorize broadcast message
        broadcastMessage = plugin.getConfigs().getStringList("welcome.broadcast_message")
            .stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
    }

    public void handlePlayerJoin(Player player) {
        if (!enabled) return;

        // Check if player is new or if we should show message to returning players
        if (!player.hasPlayedBefore() || showToReturningPlayers) {
            // Broadcast welcome message first
            for (String line : broadcastMessage) {
                Bukkit.broadcastMessage(replacePlaceholders(line, player));
            }

            // Send private message after a short delay (1 tick)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (String line : privateMessage) {
                    player.sendMessage(replacePlaceholders(line, player));
                }
            }, 1L);
        }
    }

    private String replacePlaceholders(String message, Player player) {
        return message.replace("%player%", player.getName());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfigs().set("welcome.enabled", enabled);
        plugin.getConfigs().saveConfigs();
    }

    public void setShowToReturningPlayers(boolean show) {
        this.showToReturningPlayers = show;
        plugin.getConfigs().set("welcome.show_to_returning_players", show);
        plugin.getConfigs().saveConfigs();
    }

    public void setPrivateMessage(List<String> message) {
        this.privateMessage = message.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
        plugin.getConfigs().set("welcome.private_message", message);
        plugin.getConfigs().saveConfigs();
    }

    public void setBroadcastMessage(List<String> message) {
        this.broadcastMessage = message.stream()
            .map(line -> ChatColor.translateAlternateColorCodes('&', line))
            .collect(Collectors.toList());
        plugin.getConfigs().set("welcome.broadcast_message", message);
        plugin.getConfigs().saveConfigs();
    }
} 