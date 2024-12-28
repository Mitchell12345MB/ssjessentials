package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SocialSpyCommand implements CommandExecutor {
    private final SSJEssentials plugin;
    private static final Set<UUID> socialSpyEnabled = new HashSet<>();

    public SocialSpyCommand(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    private String formatMessage(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] "));
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("§cThis command can only be used by players!"));
            return true;
        }

        if (!sender.hasPermission("ssjessentials.socialspy")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (socialSpyEnabled.contains(playerUUID)) {
            socialSpyEnabled.remove(playerUUID);
            player.sendMessage(formatMessage("§cSocialSpy disabled. You will no longer see private messages."));
        } else {
            socialSpyEnabled.add(playerUUID);
            player.sendMessage(formatMessage("§aSocialSpy enabled. You will now see all private messages."));
        }

        return true;
    }

    // Static method to check if a player has SocialSpy enabled
    public static boolean hasSocialSpyEnabled(UUID playerUUID) {
        return socialSpyEnabled.contains(playerUUID);
    }

    // Static method to broadcast a message to all players with SocialSpy enabled
    public static void broadcastToSpies(SSJEssentials plugin, String message, Player sender, Player recipient) {
        String spyMessage = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] ") + 
            "§7[SPY] " + sender.getName() + " → " + recipient.getName() + ": " + message);

        for (UUID uuid : socialSpyEnabled) {
            Player spy = plugin.getServer().getPlayer(uuid);
            if (spy != null && spy != sender && spy != recipient) {
                spy.sendMessage(spyMessage);
            }
        }
    }
} 