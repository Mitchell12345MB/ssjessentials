package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WhoisCommand implements CommandExecutor, TabCompleter {
    private final SSJEssentials plugin;

    public WhoisCommand(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    private String formatMessage(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] "));
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ssjessentials.whois")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /whois <player>"));
            return true;
        }

        // Try to find player by name or nickname
        final String searchName = args[0].toLowerCase();
        final Player target = Bukkit.getPlayer(searchName);

        if (target == null) {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                final Player candidate = onlinePlayer;
                plugin.getConfigs().getPlayerDataAsync(candidate).thenAccept(playerData -> {
                    if (playerData != null && playerData.getNickname() != null && playerData.getNickname().toLowerCase().equals(searchName)) {
                        sendWhoisInfo(sender, candidate, playerData);
                    }
                });
            }
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            sendWhoisInfo(sender, target, playerData);
        });
        return true;
    }

    private void sendWhoisInfo(CommandSender sender, Player target, com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData) {
        Location loc = target.getLocation();
        String nickname = playerData != null ? playerData.getNickname() : null;
        String ip = target.getAddress().getAddress().getHostAddress();
        sender.sendMessage(formatMessage("§6=== Player Information for " + target.getName() + " §6==="));
        sender.sendMessage(formatMessage("§7Real Name: §f" + target.getName()));
        sender.sendMessage(formatMessage("§7Nickname: §f" + (nickname != null ? nickname : "None")));
        sender.sendMessage(formatMessage("§7IP Address: §f" + ip));
        sender.sendMessage(formatMessage("§7World: §f" + target.getWorld().getName()));
        sender.sendMessage(formatMessage("§7Location: §fX: " + String.format("%.2f", loc.getX()) + ", Y: " + String.format("%.2f", loc.getY()) + ", Z: " + String.format("%.2f", loc.getZ())));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ssjessentials.whois")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            names.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            // Nicknames are loaded async, so we can't provide them synchronously here
            return names.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 