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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.*;
import java.util.stream.Collectors;

public class BackCommand implements CommandExecutor, TabCompleter, Listener {
    private final SSJEssentials plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public BackCommand(SSJEssentials plugin) {
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

        Player player = (Player) sender;

        // Handle /back <player>
        if (args.length > 0) {
            if (!player.hasPermission("ssjessentials.back.other")) {
                player.sendMessage(formatMessage("§cYou don't have permission to teleport other players to their previous location!"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(formatMessage("§cPlayer not found!"));
                return true;
            }

            Location lastLocation = lastLocations.get(target.getUniqueId());
            if (lastLocation == null) {
                player.sendMessage(formatMessage("§cNo previous location found for " + target.getName() + "!"));
                return true;
            }

            target.teleport(lastLocation);
            target.sendMessage(formatMessage("§aYou have been teleported to your previous location by " + player.getName() + "!"));
            player.sendMessage(formatMessage("§aTeleported " + target.getName() + " to their previous location!"));
            return true;
        }

        // Handle /back
        if (!player.hasPermission("ssjessentials.back")) {
            player.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        Location lastLocation = lastLocations.get(player.getUniqueId());
        if (lastLocation == null) {
            player.sendMessage(formatMessage("§cNo previous location found!"));
            return true;
        }

        player.teleport(lastLocation);
        player.sendMessage(formatMessage("§aTeleported to your previous location!"));
        return true;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        lastLocations.put(event.getEntity().getUniqueId(), event.getEntity().getLocation());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Don't store the location if it's a back command teleport
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND) {
            lastLocations.put(event.getPlayer().getUniqueId(), event.getFrom());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("ssjessentials.back.other")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 