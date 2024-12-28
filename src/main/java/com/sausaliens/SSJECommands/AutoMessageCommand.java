package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AutoMessageCommand implements CommandExecutor, TabCompleter {
    private final SSJEssentials plugin;

    public AutoMessageCommand(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ssjessentials.am")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /am add <message>");
                    return true;
                }
                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (plugin.getAutoMessageManager().addMessage(message)) {
                    sender.sendMessage(ChatColor.GREEN + "Auto message added successfully!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Failed to add auto message!");
                }
                break;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /am remove <index>");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]) - 1; // Convert to 0-based index
                    if (plugin.getAutoMessageManager().removeMessage(index)) {
                        sender.sendMessage(ChatColor.GREEN + "Auto message removed successfully!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid message index!");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please provide a valid number!");
                }
                break;

            case "list":
                List<String> messages = plugin.getAutoMessageManager().getMessages();
                if (messages.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "No auto messages configured!");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "Auto Messages:");
                for (int i = 0; i < messages.size(); i++) {
                    sender.sendMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + ChatColor.WHITE + messages.get(i));
                }
                break;

            case "interval":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /am interval <seconds>");
                    return true;
                }
                try {
                    int seconds = Integer.parseInt(args[1]);
                    if (seconds < 1) {
                        sender.sendMessage(ChatColor.RED + "Interval must be at least 1 second!");
                        return true;
                    }
                    plugin.getAutoMessageManager().setInterval(seconds);
                    sender.sendMessage(ChatColor.GREEN + "Auto message interval set to " + seconds + " seconds!");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please provide a valid number!");
                }
                break;

            case "random":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /am random <true/false>");
                    return true;
                }
                boolean random = Boolean.parseBoolean(args[1]);
                plugin.getAutoMessageManager().setRandom(random);
                sender.sendMessage(ChatColor.GREEN + "Random message order set to: " + random);
                break;

            case "reload":
                plugin.getAutoMessageManager().reloadMessages();
                sender.sendMessage(ChatColor.GREEN + "Auto messages reloaded!");
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Auto Message Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/am add <message> " + ChatColor.WHITE + "- Add a new auto message");
        sender.sendMessage(ChatColor.YELLOW + "/am remove <index> " + ChatColor.WHITE + "- Remove an auto message");
        sender.sendMessage(ChatColor.YELLOW + "/am list " + ChatColor.WHITE + "- List all auto messages");
        sender.sendMessage(ChatColor.YELLOW + "/am interval <seconds> " + ChatColor.WHITE + "- Set message interval");
        sender.sendMessage(ChatColor.YELLOW + "/am random <true/false> " + ChatColor.WHITE + "- Toggle random order");
        sender.sendMessage(ChatColor.YELLOW + "/am reload " + ChatColor.WHITE + "- Reload auto messages");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ssjessentials.am")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("add", "remove", "list", "interval", "random", "reload")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "random":
                    return Arrays.asList("true", "false")
                            .stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "remove":
                    List<String> messages = plugin.getAutoMessageManager().getMessages();
                    List<String> indices = new ArrayList<>();
                    for (int i = 1; i <= messages.size(); i++) {
                        indices.add(String.valueOf(i));
                    }
                    return indices;
            }
        }

        return Collections.emptyList();
    }
} 