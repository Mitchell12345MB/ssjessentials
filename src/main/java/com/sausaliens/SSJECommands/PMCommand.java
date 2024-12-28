package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PMCommand implements CommandExecutor, TabCompleter {
    private final SSJEssentials plugin;

    public PMCommand(SSJEssentials plugin) {
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

        if (!sender.hasPermission("ssjessentials.pm")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(formatMessage("§cUsage: /pm <player> <message>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        // Build the message from the remaining arguments
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }

        // Format and send the messages
        Player player = (Player) sender;
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', message.toString().trim());
        
        // Message to the sender
        player.sendMessage(formatMessage("§7[§aYou §7→ §a" + target.getName() + "§7] " + formattedMessage));
        
        // Message to the recipient
        target.sendMessage(formatMessage("§7[§a" + player.getName() + " §7→ §aYou§7] " + formattedMessage));
        
        // Play sound for the recipient
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

        // Broadcast to staff with SocialSpy enabled
        SocialSpyCommand.broadcastToSpies(plugin, formattedMessage, player, target);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 