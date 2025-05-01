package com.sausaliens.SSJECommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles administrative commands like reload
 */
public class AdminCommands extends BaseCommand {

    public AdminCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ssjereload")) {
            return handleReload(sender);
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No tab completion for reload command
        return new ArrayList<>();
    }

    private boolean handleReload(CommandSender sender) {
        if (!hasPermission(sender, "ssjessentials.reload")) {
            return true;
        }

        try {
            plugin.reloadConfig();
            sender.sendMessage(formatMessage("§aConfiguration reloaded successfully!"));
            return true;
        } catch (Exception e) {
            sender.sendMessage(formatMessage("§cError reloading configuration: " + e.getMessage()));
            return false;
        }
    }
} 