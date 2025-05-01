package com.sausaliens.SSJECommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.List;

/**
 * Base command class that provides common functionality for all SSJEssentials commands
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    
    protected final SSJEssentials plugin;
    
    public BaseCommand(SSJEssentials plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Formats a message with the plugin prefix
     * @param message The message to format
     * @return The formatted message
     */
    protected String formatMessage(String message) {
        return plugin.formatMessage(message);
    }
    
    /**
     * Checks if the sender has a specific permission
     * @param sender The command sender
     * @param permission The permission to check
     * @return true if the sender has the permission
     */
    protected boolean hasPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the sender has a specific permission with a custom error message
     * @param sender The command sender
     * @param permission The permission to check
     * @param errorMessage Custom error message to display if permission is denied
     * @return true if the sender has the permission
     */
    protected boolean hasPermission(CommandSender sender, String permission, String errorMessage) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(formatMessage(errorMessage));
            return false;
        }
        return true;
    }
    
    /**
     * Checks if the sender has any of the provided permissions
     * @param sender The command sender
     * @param permissions Array of permissions to check
     * @return true if the sender has any of the permissions
     */
    protected boolean hasAnyPermission(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
        return false;
    }
    
    /**
     * Checks if the sender has all of the provided permissions
     * @param sender The command sender
     * @param permissions Array of permissions to check
     * @return true if the sender has all of the permissions
     */
    protected boolean hasAllPermissions(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if the sender has permission to modify another player
     * @param sender The command sender
     * @param target The target player
     * @param basePermission The base permission
     * @param othersPermission The others permission
     * @return true if the sender has permission
     */
    protected boolean hasPlayerModifyPermission(CommandSender sender, Player target, String basePermission, String othersPermission) {
        // If sender is targeting themselves, only need basic permission
        if (sender instanceof Player && sender.equals(target)) {
            return hasPermission(sender, basePermission);
        } 
        // If targeting another player, need others permission
        else {
            return hasPermission(sender, othersPermission);
        }
    }
    
    /**
     * Checks if the sender is a player
     * @param sender The command sender
     * @return The player instance, or null if the sender is not a player
     */
    protected Player getPlayerSender(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("§cThis command can only be used by players!"));
            return null;
        }
        return (Player) sender;
    }
    
    /**
     * Gets a player by name with error handling
     * @param sender The command sender
     * @param name The player name
     * @return The player instance, or null if not found
     */
    protected Player getTargetPlayer(CommandSender sender, String name) {
        Player target = plugin.getServer().getPlayer(name);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return null;
        }
        return target;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Default implementation - override in subclasses
        return new ArrayList<>();
    }
    
    /**
     * Registers this command with the plugin
     * @param commandName The name of the command to register
     */
    public void register(String commandName) {
        plugin.getCommand(commandName).setExecutor(this);
        plugin.getCommand(commandName).setTabCompleter(this);
    }
} 