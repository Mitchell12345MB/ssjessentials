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
        // Use async group and player data APIs
        plugin.getGroupManager().getPlayerGroupAsync(sender, groupName -> {
            String prefix = plugin.getGroupManager().getGroupPrefix(groupName);
            String suffix = plugin.getGroupManager().getGroupSuffix(groupName);
            plugin.getConfigs().getPlayerDataAsync(sender).thenAccept(playerData -> {
                String playerName = (playerData != null && playerData.getNickname() != null)
                        ? playerData.getNickname() : sender.getName();
                // Process colored text for players with permission
                String processedMessage = message;
                if (sender.hasPermission("ssjessentials.cooltext")) {
                    processedMessage = ChatColor.translateAlternateColorCodes('&', message);
                }
                // Check if sender is staff
                if (sender.hasPermission("ssjessentials.mod") || sender.hasPermission("ssjessentials.admin")) {
                    // Check for player mentions in the message
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        String targetName = target.getName();
                        String targetNick = target.getDisplayName();
                        // If the message contains the player's full name or nickname
                        if (processedMessage.contains(targetName) || processedMessage.contains(targetNick)) {
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
                // Add message in white (we'll use the processed message)
                format.append(" &f%2$s");
                // Set the chat format with the possibly colorized message
                String finalFormat = ChatColor.translateAlternateColorCodes('&', format.toString());
                // Update the message in the event
                event.setMessage(processedMessage);
                event.setFormat(finalFormat);
            });
        });
    }
} 