package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEManagers.TeleportManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles teleport-related commands like tp, tpr, tpraccept, and spawn
 */
public class TeleportCommands extends BaseCommand {

    public TeleportCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "tp":
                return handleTeleport(sender, args);
            case "tpr":
                return handleTeleportRequest(sender, args);
            case "tpraccept":
                return handleTeleportAccept(sender, args);
            case "spawn":
                return handleSpawn(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "tp":
                return handleTeleportTabComplete(sender, args);
            case "tpr":
                return handleTeleportRequestTabComplete(sender, args);
            case "spawn":
                return handleSpawnTabComplete(sender, args);
            default:
                return new ArrayList<>();
        }
    }
    
    private List<String> handleTeleportTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tp.staff")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 2) {
            List<String> options = new ArrayList<>();
            options.add("spawn");
            
            // Add player names
            options.addAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList()));
                
            return options;
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleTeleportRequestTabComplete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tpr")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Don't suggest the sender's name
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> !name.equalsIgnoreCase(sender.getName()))
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
    
    private List<String> handleSpawnTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("ssjessentials.spawn.others")) {
            // Suggest player names for /spawn <player>
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        } else if (args.length == 1 && sender.hasPermission("ssjessentials.spawn.mob")) {
            // Suggest entity types for /spawn <entity>
            return Arrays.stream(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .map(EntityType::toString)
                .map(String::toLowerCase)
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.tp.staff")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /tp <player> [target/spawn/x y z]"));
            return true;
        }

        Player player = getTargetPlayer(sender, args[0]);
        if (player == null) return true;

        if (args.length == 1) {
            // Teleport sender to player
            Player senderPlayer = getPlayerSender(sender);
            if (senderPlayer == null) return true;
            
            // Use delayed teleport
            if (plugin.getTeleportManager().initiateDelayedTeleport(
                    senderPlayer, 
                    player.getLocation(), 
                    TeleportManager.TeleportType.TP)) {
                return true;
            }
            
            senderPlayer.sendMessage(formatMessage("§aTeleported to " + player.getName()));
            return true;
        }

        if (args[1].equalsIgnoreCase("spawn")) {
            // Teleport player to spawn
            Location spawnLoc = plugin.getSpawnConfig().getSpawnLocation();
            if (spawnLoc == null) {
                sender.sendMessage(formatMessage("§cSpawn location has not been set!"));
                return true;
            }
            
            // Use delayed teleport
            if (plugin.getTeleportManager().initiateDelayedTeleport(
                    player, 
                    spawnLoc, 
                    TeleportManager.TeleportType.TP)) {
                // Teleport was initiated with delay
                if (sender != player) {
                    sender.sendMessage(formatMessage("§aInitiated teleport for " + player.getName() + " to spawn"));
                }
                return true;
            }
            
            player.sendMessage(formatMessage("§aYou were teleported to spawn"));
            if (sender != player) {
                sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to spawn"));
            }
            return true;
        }

        if (args.length == 4) {
            // Handle coordinates
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                Location loc = new Location(player.getWorld(), x, y, z);
                
                // Use delayed teleport
                if (plugin.getTeleportManager().initiateDelayedTeleport(
                        player, 
                        loc, 
                        TeleportManager.TeleportType.TP)) {
                    // Teleport was initiated with delay
                    if (sender != player) {
                        sender.sendMessage(formatMessage("§aInitiated teleport for " + player.getName() + " to coordinates"));
                    }
                    return true;
                }
                
                player.sendMessage(formatMessage("§aYou were teleported to " + x + " " + y + " " + z));
                if (sender != player) {
                    sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to coordinates"));
                }
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(formatMessage("§cInvalid coordinates!"));
                return true;
            }
        }

        // Handle player to player teleport
        Player target = getTargetPlayer(sender, args[1]);
        if (target == null) return true;

        // Use delayed teleport
        if (plugin.getTeleportManager().initiateDelayedTeleport(
                player, 
                target.getLocation(), 
                TeleportManager.TeleportType.TP)) {
            // Teleport was initiated with delay
            if (sender != player) {
                sender.sendMessage(formatMessage("§aInitiated teleport for " + player.getName() + " to " + target.getName()));
            }
            return true;
        }

        player.sendMessage(formatMessage("§aYou were teleported to " + target.getName()));
        if (sender != player) {
            sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to " + target.getName()));
        }
        return true;
    }

    private boolean handleTeleportRequest(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.tpr")) {
            return true;
        }
        
        if (args.length != 1) {
            player.sendMessage(formatMessage("§cUsage: /tpr <player>"));
            return true;
        }

        Player target = getTargetPlayer(player, args[0]);
        if (target == null) return true;

        if (target == player) {
            player.sendMessage(formatMessage("§cYou cannot teleport to yourself!"));
            return true;
        }
        
        // Check if sender is on cooldown
        if (plugin.getTeleportManager().isOnCooldown(player)) {
            int remainingSeconds = plugin.getTeleportManager().getRemainingCooldown(player);
            player.sendMessage(formatMessage("§cYou need to wait " + remainingSeconds + 
                " seconds before sending another teleport request!"));
            return true;
        }

        if (!plugin.getTeleportManager().createRequest(player, target)) {
            // This should never happen since we already checked cooldown, but just in case
            player.sendMessage(formatMessage("§cCannot send teleport request at this time. Try again later."));
            return true;
        }
        
        player.sendMessage(formatMessage("§aTeleport request sent to " + target.getName()));
        target.sendMessage(formatMessage("§e" + player.getName() + " §awants to teleport to you."));
        target.sendMessage(formatMessage("§aType §e/tpraccept §ato accept."));
        return true;
    }

    private boolean handleTeleportAccept(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.tpr.accept")) {
            return true;
        }
        
        if (!plugin.getTeleportManager().hasActiveRequest(player)) {
            player.sendMessage(formatMessage("§cYou have no pending teleport requests!"));
            return true;
        }

        Player requester = plugin.getTeleportManager().getRequester(player);
        if (requester == null || !requester.isOnline()) {
            player.sendMessage(formatMessage("§cThe player who requested to teleport is no longer online!"));
            plugin.getTeleportManager().removeRequest(player);
            return true;
        }

        // Store the requester's previous location for /back command if it exists
        if (plugin.getServer().getPluginManager().isPluginEnabled("SSJEssentials")) {
            try {
                Class<?> backCommandClass = Class.forName("com.sausaliens.SSJECommands.BackCommand");
                if (backCommandClass != null) {
                    // Try to store previous location
                    java.lang.reflect.Method storeMethod = backCommandClass.getDeclaredMethod("storePreviousLocation", Player.class, Location.class);
                    storeMethod.invoke(null, requester, requester.getLocation());
                }
            } catch (Exception e) {
                // Silently ignore if BackCommand is not available
            }
        }

        // Remove the request first
        plugin.getTeleportManager().removeRequest(player);
        
        // Use delayed teleport for TPR
        if (plugin.getTeleportManager().initiateDelayedTeleport(
                requester, 
                player.getLocation(), 
                TeleportManager.TeleportType.TPR)) {
            // Teleport was initiated with delay
            player.sendMessage(formatMessage("§aInitiated teleport for " + requester.getName() + " to your location"));
            return true;
        }

        // Teleport requester to target
        requester.teleport(player.getLocation());
        requester.sendMessage(formatMessage("§aTeleported to " + player.getName()));
        player.sendMessage(formatMessage("§a" + requester.getName() + " has been teleported to you"));
        return true;
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Teleport sender to spawn
            Player player = getPlayerSender(sender);
            if (player == null) return true;
            
            if (!hasPermission(player, "ssjessentials.spawn")) {
                return true;
            }
            
            teleportToSpawn(player);
            return true;
        }

        // Try to find player first
        Player targetPlayer = getTargetPlayer(sender, args[0]);
        if (targetPlayer != null) {
            if (!hasPermission(sender, "ssjessentials.spawn.others")) {
                return true;
            }
            
            teleportToSpawn(targetPlayer);
            sender.sendMessage(formatMessage("§aTeleported " + targetPlayer.getName() + " to spawn!"));
            return true;
        }

        // If not a player, try to spawn entity
        if (!hasPermission(sender, "ssjessentials.spawn.mob")) {
            return true;
        }

        Player player = getPlayerSender(sender);
        if (player == null) return true;

        try {
            EntityType entityType = EntityType.valueOf(args[0].toUpperCase());
            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            double health = args.length > 2 ? Double.parseDouble(args[2]) : -1;
            
            Location spawnLoc = player.getLocation();
            if (args.length > 3) {
                Player targetLoc = getTargetPlayer(sender, args[3]);
                if (targetLoc != null) {
                    if (!hasPermission(sender, "ssjessentials.spawn.mob.tp")) {
                        return true;
                    }
                    spawnLoc = targetLoc.getLocation();
                } else {
                    sender.sendMessage(formatMessage("§cTarget player not found!"));
                    return true;
                }
            }

            for (int i = 0; i < amount; i++) {
                Entity entity = player.getWorld().spawnEntity(spawnLoc, entityType);
                if (health > 0 && entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setHealth(health);
                }
            }
            
            String message = formatMessage("§aSpawned " + amount + " " + entityType.toString().toLowerCase());
            if (health > 0) {
                message += " with " + health + " health";
            }
            if (args.length > 3) {
                message += " at " + args[3];
            }
            sender.sendMessage(message);
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage(formatMessage("§cInvalid entity type, amount, or health value!"));
            return true;
        }
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.setspawn")) {
            return true;
        }

        plugin.getSpawnConfig().setSpawnLocation(player.getLocation());
        player.sendMessage(formatMessage("§aSpawn location set!"));
        return true;
    }

    private void teleportToSpawn(Player player) {
        Location spawnLoc = plugin.getSpawnConfig().getSpawnLocation();
        if (spawnLoc == null) {
            spawnLoc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
        }
        
        // Use delayed teleport for SPAWN
        if (plugin.getTeleportManager().initiateDelayedTeleport(
                player, 
                spawnLoc, 
                TeleportManager.TeleportType.SPAWN)) {
            // Teleport was initiated with delay
            return;
        }
        
        player.teleport(spawnLoc);
        player.sendMessage(formatMessage("§aTeleported to spawn"));
    }
} 