package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEManagers.GroupManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class GroupCommand implements CommandExecutor, TabCompleter {
    private final GroupManager groupManager;

    public GroupCommand(SSJEssentials plugin, GroupManager groupManager) {
        this.groupManager = groupManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ssjessentials.group")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group create <groupname>");
                    return true;
                }
                if (groupManager.createGroup(args[1], new ArrayList<>())) {
                    sender.sendMessage(ChatColor.GREEN + "Group " + args[1] + " created successfully!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[1] + " already exists!");
                }
                break;

            case "delete":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group delete <groupname>");
                    return true;
                }
                if (groupManager.deleteGroup(args[1])) {
                    sender.sendMessage(ChatColor.GREEN + "Group " + args[1] + " deleted successfully!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[1] + " doesn't exist!");
                }
                break;

            case "addperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group addperm <groupname> <permission>");
                    return true;
                }
                if (groupManager.addPermissionToGroup(args[1], args[2])) {
                    sender.sendMessage(ChatColor.GREEN + "Permission " + args[2] + " added to group " + args[1] + "!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[1] + " doesn't exist!");
                }
                break;

            case "removeperm":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group removeperm <groupname> <permission>");
                    return true;
                }
                if (groupManager.removePermissionFromGroup(args[1], args[2])) {
                    sender.sendMessage(ChatColor.GREEN + "Permission " + args[2] + " removed from group " + args[1] + "!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[1] + " doesn't exist!");
                }
                break;

            case "setgroup":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group setgroup <player> <groupname>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found!");
                    return true;
                }
                if (groupManager.setPlayerGroup(target, args[2])) {
                    sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " added to group " + args[2] + "!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[2] + " doesn't exist!");
                }
                break;

            case "list":
                sender.sendMessage(ChatColor.GREEN + "Available groups:");
                for (String group : groupManager.getGroups()) {
                    sender.sendMessage(ChatColor.YELLOW + "- " + group);
                }
                break;

            case "info":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /group info <groupname>");
                    return true;
                }
                Set<String> permissions = groupManager.getGroupPermissions(args[1]);
                if (permissions != null) {
                    sender.sendMessage(ChatColor.GREEN + "Permissions for group " + args[1] + ":");
                    for (String perm : permissions) {
                        sender.sendMessage(ChatColor.YELLOW + "- " + perm);
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Group " + args[1] + " doesn't exist!");
                }
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "Group Management Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/group create <groupname> " + ChatColor.WHITE + "- Create a new group");
        sender.sendMessage(ChatColor.YELLOW + "/group delete <groupname> " + ChatColor.WHITE + "- Delete a group");
        sender.sendMessage(ChatColor.YELLOW + "/group addperm <groupname> <permission> " + ChatColor.WHITE + "- Add permission to group");
        sender.sendMessage(ChatColor.YELLOW + "/group removeperm <groupname> <permission> " + ChatColor.WHITE + "- Remove permission from group");
        sender.sendMessage(ChatColor.YELLOW + "/group setgroup <player> <groupname> " + ChatColor.WHITE + "- Set player's group");
        sender.sendMessage(ChatColor.YELLOW + "/group list " + ChatColor.WHITE + "- List all groups");
        sender.sendMessage(ChatColor.YELLOW + "/group info <groupname> " + ChatColor.WHITE + "- Show group permissions");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ssjessentials.group")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("create", "delete", "addperm", "removeperm", "setgroup", "list", "info")
                    .stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "delete":
                case "addperm":
                case "removeperm":
                case "info":
                    return new ArrayList<>(groupManager.getGroups())
                            .stream()
                            .filter(s -> s.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                case "setgroup":
                    return null; // Return null to show online players
            }
        }

        if (args.length == 3 && args[0].toLowerCase().equals("setgroup")) {
            return new ArrayList<>(groupManager.getGroups())
                    .stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
} 