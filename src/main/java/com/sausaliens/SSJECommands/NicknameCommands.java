package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles nickname-related commands
 */
public class NicknameCommands extends BaseCommand {

    public NicknameCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("nick")) {
            return handleNick(sender, args);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("nick")) {
            if (args.length == 1) {
                // If sender has permission to change others' nicknames, suggest players
                if (sender.hasPermission("ssjessentials.nick.others")) {
                    List<String> suggestions = new ArrayList<>();
                    
                    // Add "none" option
                    if ("none".startsWith(args[0].toLowerCase())) {
                        suggestions.add("none");
                    }
                    
                    // Add player names
                    suggestions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList()));
                    
                    return suggestions;
                } else {
                    // Only suggest "none" option
                    if ("none".startsWith(args[0].toLowerCase())) {
                        return Arrays.asList("none");
                    }
                }
            } else if (args.length == 2 && sender.hasPermission("ssjessentials.nick.others")) {
                // Suggest "none" for second argument if first arg is a player
                if ("none".startsWith(args[1].toLowerCase())) {
                    return Arrays.asList("none");
                }
            }
        }
        return new ArrayList<>();
    }

    public boolean handleNick(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /nick <nickname> or /nick <player> <nickname>"));
            return true;
        }

        final Player target = sender instanceof Player ? (Player) sender : Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        final String nickname;

        if (args.length > 1) {
            if (!hasPermission(sender, "ssjessentials.nick.others")) {
                return true;
            }
            Player tempTarget = Bukkit.getPlayer(args[0]);
            if (tempTarget == null) {
                sender.sendMessage(formatMessage("§cPlayer not found!"));
                return true;
            }
            nickname = args[1];
        } else {
            if (!hasPermission(sender, "ssjessentials.nick")) {
                return true;
            }
            nickname = args[0];
        }

        // Handle "none" option to remove nickname
        if (nickname.equalsIgnoreCase("none")) {
            plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
                if (playerData == null) {
                    plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in nick command");
                    return;
                }
                playerData.setNickname(null);
                plugin.getConfigs().savePlayerData(target);
                target.setDisplayName(target.getName());
                plugin.getGroupManager().updatePlayerTabName(target);
                target.sendMessage(formatMessage("§aYour nickname has been removed."));
                if (sender != target) {
                    sender.sendMessage(formatMessage("§aRemoved " + target.getName() + "'s nickname."));
                }
            });
            return true;
        }

        int maxLength = plugin.getConfig().getInt("nickname.max-length", 16);
        if (nickname.length() > maxLength) {
            sender.sendMessage(formatMessage("§cNickname too long! Maximum length is " + maxLength + " characters."));
            return true;
        }

        final String finalNickname;
        if (plugin.getConfig().getBoolean("nickname.allow-colors", true)) {
            finalNickname = ChatColor.translateAlternateColorCodes('&', nickname);
        } else {
            finalNickname = nickname;
        }

        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in nick command");
                return;
            }
            playerData.setNickname(finalNickname);
            plugin.getConfigs().savePlayerData(target);
            target.setDisplayName(finalNickname);
            plugin.getGroupManager().updatePlayerTabName(target);
            target.sendMessage(formatMessage("§aYour nickname has been changed to: " + finalNickname));
            if (sender != target) {
                sender.sendMessage(formatMessage("§aChanged " + target.getName() + "'s nickname to: " + finalNickname));
            }
        });
        return true;
    }
} 