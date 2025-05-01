package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEManagers.TeleportManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles all location-related commands: spawn, home, warp
 */
public class LocationCommands extends BaseCommand {

    public LocationCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        switch (cmd) {
            case "spawn":
                return handleSpawn(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "spawnall":
                return handleSpawnAll(sender);
            case "warp":
                return handleWarp(sender, args);
            case "setwarp":
                return handleSetWarp(sender, args);
            case "delwarp":
                return handleDelWarp(sender, args);
            case "/warp":
                return handleWarpList(sender);
            case "editwarp":
                return handleEditWarp(sender, args);
            case "resetwarp":
                return handleResetWarp(sender, args);
            case "warpall":
                return handleWarpAll(sender, args);
            case "home":
                return handleHome(sender, args);
            case "sethome":
                return handleSetHome(sender, args);
            case "delhome":
                return handleDelHome(sender, args);
            case "/home":
                return handleHomeList(sender);
            case "edithome":
                return handleEditHome(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            
            switch (cmd) {
                case "warp":
                case "delwarp":
                case "editwarp":
                case "resetwarp":
                    // Check if has any of the warp-related permissions
                    if (hasAnyPermission(sender, 
                        "ssjessentials.warp", 
                        "ssjessentials.setwarp.del", 
                        "ssjessentials.setwarp.edit",
                        "ssjessentials.setwarp.reset")) {
                        
                        Set<String> warps = plugin.getLocationManager().getWarps();
                        if (warps != null) {
                            completions.addAll(warps.stream()
                                .filter(warp -> warp.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList()));
                        }
                    }
                    break;
                case "home":
                case "delhome":
                case "edithome":
                    if (sender instanceof Player && 
                        hasAnyPermission(sender,
                            "ssjessentials.home", 
                            "ssjessentials.sethome.del",
                            "ssjessentials.sethome.edit")) {
                            
                        Player player = (Player) sender;
                        Set<String> homes = plugin.getLocationManager().getHomes(player);
                        if (homes != null) {
                            completions.addAll(homes.stream()
                                .filter(home -> home.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList()));
                        }
                    }
                    break;
                case "spawn":
                    if (sender.hasPermission("ssjessentials.spawntp.others")) {
                        // Add online players
                        completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList()));
                        
                        // Add entity types for /spawn <entitytype>
                        if (sender.hasPermission("ssjessentials.spawn.mob")) {
                            for (EntityType type : EntityType.values()) {
                                if (type.isSpawnable() && type.isAlive() && 
                                    type.name().toLowerCase().startsWith(args[0].toLowerCase())) {
                                    completions.add(type.name().toLowerCase());
                                }
                            }
                        }
                    }
                    break;
                case "warpall":
                    if (sender.hasPermission("ssjessentials.warp.all")) {
                        Set<String> warps = plugin.getLocationManager().getWarps();
                        if (warps != null) {
                            completions.addAll(warps.stream()
                                .filter(warp -> warp.toLowerCase().startsWith(args[0].toLowerCase()))
                                .collect(Collectors.toList()));
                        }
                    }
                    break;
            }
            
            return completions;
        } else if (args.length == 2) {
            // For spawn <entitytype> <amount>
            if (cmd.equals("spawn") && sender.hasPermission("ssjessentials.spawn.mob")) {
                try {
                    EntityType.valueOf(args[0].toUpperCase());
                    // Suggest some common spawn amounts
                    List<String> amounts = Arrays.asList("1", "5", "10", "20", "50", "100");
                    return amounts.stream()
                        .filter(amount -> amount.startsWith(args[1]))
                        .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    // Not a valid entity type, don't provide suggestions
                }
            }
            
            // For editwarp <old> <new> or edithome <old> <new>
            if ((cmd.equals("editwarp") && sender.hasPermission("ssjessentials.setwarp.edit")) ||
                (cmd.equals("edithome") && sender instanceof Player && 
                 sender.hasPermission("ssjessentials.sethome.edit"))) {
                // Open suggestions for new name
                return new ArrayList<>();
            }
        } else if (args.length == 3) {
            // For spawn <entitytype> <amount> <health>
            if (cmd.equals("spawn") && sender.hasPermission("ssjessentials.spawn.mob")) {
                try {
                    EntityType.valueOf(args[0].toUpperCase());
                    Integer.parseInt(args[1]); // Check if amount is a number
                    // Suggest some common health values
                    List<String> healthValues = Arrays.asList("1", "5", "10", "20", "50", "100");
                    return healthValues.stream()
                        .filter(health -> health.startsWith(args[2]))
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    // Not valid params, don't provide suggestions
                }
            }
        } else if (args.length == 4) {
            // For spawn <entitytype> <amount> <health> <player>
            if (cmd.equals("spawn") && 
                hasAllPermissions(sender, 
                    "ssjessentials.spawn.mob",
                    "ssjessentials.spawn.mob.tp")) {
                try {
                    EntityType.valueOf(args[0].toUpperCase());
                    Integer.parseInt(args[1]); // Check if amount is a number
                    Double.parseDouble(args[2]); // Check if health is a number
                    // Suggest online players
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    // Not valid params, don't provide suggestions
                }
            }
        }
        
        return new ArrayList<>();
    }

    private boolean handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(formatMessage("§cUsage: /spawn <player> or /spawn <entitytype> [amount] [health] [player]"));
            return true;
        }
        
        if (args.length == 0) {
            // No args, teleport sender to spawn
            Player player = (Player) sender;
            if (!hasPermission(player, "ssjessentials.spawn", "§cYou don't have permission to teleport to spawn!")) {
                return true;
            }
            
            teleportToSpawn(player);
            return true;
        }
        
        // Try to find player first
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer != null) {
            if (!hasPermission(sender, "ssjessentials.spawntp.others", "§cYou don't have permission to teleport others to spawn!")) {
                return true;
            }
            teleportToSpawn(targetPlayer);
            sender.sendMessage(formatMessage("§aTeleported " + targetPlayer.getName() + " to spawn!"));
            return true;
        }
        
        // If not a player, try to spawn entity
        if (!hasPermission(sender, "ssjessentials.spawn.mob", "§cYou don't have permission to spawn mobs!")) {
            return true;
        }
        
        try {
            EntityType entityType = EntityType.valueOf(args[0].toUpperCase());
            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            double health = args.length > 2 ? Double.parseDouble(args[2]) : -1;
            
            Location spawnLoc;
            if (sender instanceof Player) {
                spawnLoc = ((Player) sender).getLocation();
            } else {
                // Console needs to specify a player
                if (args.length < 4) {
                    sender.sendMessage(formatMessage("§cYou must specify a player to spawn entities at when using console!"));
                    return true;
                }
                spawnLoc = null; // Will be set below
            }
            
            if (args.length > 3) {
                Player targetLoc = Bukkit.getPlayer(args[3]);
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
            
            if (spawnLoc == null) {
                sender.sendMessage(formatMessage("§cA valid spawn location is required!"));
                return true;
            }
            
            for (int i = 0; i < amount; i++) {
                Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, entityType);
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
    
    private void teleportToSpawn(Player player) {
        Location spawn = plugin.getSpawnConfig().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(formatMessage("§cSpawn location has not been set!"));
            return;
        }
        player.teleport(spawn);
        player.sendMessage(formatMessage("§aTeleported to spawn!"));
    }
    
    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.setspawn", "§cYou don't have permission to set spawn!")) {
            return true;
        }
        
        plugin.getSpawnConfig().setSpawnLocation(player.getLocation());
        player.sendMessage(formatMessage("§aSpawn location set!"));
        return true;
    }
    
    private boolean handleSpawnAll(CommandSender sender) {
        if (!hasPermission(sender, "ssjessentials.spawn.all", "§cYou don't have permission to teleport all players to spawn!")) {
            return true;
        }
        
        Location spawnLocation = plugin.getSpawnConfig().getSpawnLocation();
        if (spawnLocation == null) {
            sender.sendMessage(formatMessage("§cSpawn location has not been set!"));
            return true;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawnLocation);
            player.sendMessage(formatMessage("§aYou have been teleported to spawn"));
        }
        
        sender.sendMessage(formatMessage("§aAll players have been teleported to spawn"));
        return true;
    }
    
    private boolean handleWarp(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.warp", "§cYou don't have permission to use warps!")) {
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /warp <name>"));
            return true;
        }

        String warpName = args[0].toLowerCase();
        Location warpLocation = plugin.getLocationManager().getWarp(warpName);
        
        if (warpLocation == null) {
            player.sendMessage(formatMessage("§cWarp '" + warpName + "' not found!"));
            return true;
        }

        // Try to teleport using delayed teleport
        if (plugin.getTeleportManager().initiateDelayedTeleport(
                player, 
                warpLocation, 
                TeleportManager.TeleportType.WARP)) {
            // Teleport was initiated with delay
            return true;
        }
        
        // Immediate teleport if no delay
        player.teleport(warpLocation);
        player.sendMessage(formatMessage("§aTeleported to warp '" + warpName + "'"));
        return true;
    }
    
    private boolean handleSetWarp(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.setwarp", "§cYou don't have permission to set warps!")) {
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /setwarp <name>"));
            return true;
        }
        
        String warpName = args[0].toLowerCase();
        
        // Check if warp already exists
        if (plugin.getLocationManager().warpExists(warpName)) {
            player.sendMessage(formatMessage("§cA warp with that name already exists! Use /editwarp to modify it."));
            return true;
        }
        
        plugin.getLocationManager().setWarp(warpName, player.getLocation(), "");
        player.sendMessage(formatMessage("§aWarp '" + warpName + "' has been set!"));
        return true;
    }
    
    private boolean handleDelWarp(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.setwarp.del", "§cYou don't have permission to delete warps!")) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /delwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        if (plugin.getLocationManager().getWarp(warpName) == null) {
            sender.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }
        
        plugin.getLocationManager().deleteWarp(warpName);
        sender.sendMessage(formatMessage("§aWarp '" + warpName + "' has been deleted!"));
        return true;
    }
    
    private boolean handleWarpList(CommandSender sender) {
        if (!hasPermission(sender, "ssjessentials.warp.list", "§cYou don't have permission to list warps!")) {
            return true;
        }
        
        Set<String> warps = plugin.getLocationManager().getWarps();
        if (warps == null || warps.isEmpty()) {
            sender.sendMessage(formatMessage("§cNo warps have been set!"));
            return true;
        }
        
        sender.sendMessage(formatMessage("§aAvailable warps:"));
        for (String warp : warps) {
            sender.sendMessage(formatMessage("§7- §f" + warp));
        }
        return true;
    }
    
    private boolean handleEditWarp(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.setwarp.edit", "§cYou don't have permission to edit warps!")) {
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(formatMessage("§cUsage: /editwarp <current name> <new name>"));
            return true;
        }
        
        String currentName = args[0];
        String newName = args[1];
        Location warpLocation = plugin.getLocationManager().getWarp(currentName);
        
        if (warpLocation == null) {
            sender.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }
        
        // Get the description from the old warp
        String description = ""; // Default empty description
        
        plugin.getLocationManager().setWarp(newName, warpLocation, description);
        plugin.getLocationManager().deleteWarp(currentName);
        sender.sendMessage(formatMessage("§aWarp renamed from '" + currentName + "' to '" + newName + "'"));
        return true;
    }
    
    private boolean handleResetWarp(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.setwarp.reset", "§cYou don't have permission to reset warps!")) {
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /resetwarp <name>"));
            return true;
        }
        
        String warpName = args[0];
        if (plugin.getLocationManager().getWarp(warpName) == null) {
            player.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }
        
        plugin.getLocationManager().setWarp(warpName, player.getLocation(), "");
        player.sendMessage(formatMessage("§aWarp '" + warpName + "' has been reset to your location!"));
        return true;
    }
    
    private boolean handleWarpAll(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.warp.all", "§cYou don't have permission to teleport all players to a warp!")) {
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /warpall <warp name>"));
            return true;
        }
        
        String warpName = args[0];
        Location warpLocation = plugin.getLocationManager().getWarp(warpName);
        
        if (warpLocation == null) {
            sender.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(warpLocation);
            player.sendMessage(formatMessage("§aYou have been teleported to warp: " + warpName));
        }
        
        sender.sendMessage(formatMessage("§aAll players have been teleported to warp: " + warpName));
        return true;
    }
    
    private boolean handleHome(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.home", "§cYou don't have permission to teleport to homes!")) {
            return true;
        }
        
        String homeName = "default";
        if (args.length > 0) {
            homeName = args[0].toLowerCase();
        }
        
        Location homeLocation = plugin.getLocationManager().getHome(player, homeName);
        if (homeLocation == null) {
            if (homeName.equals("default")) {
                player.sendMessage(formatMessage("§cYou haven't set a home yet! Use /sethome to set one."));
            } else {
                player.sendMessage(formatMessage("§cHome '" + homeName + "' not found!"));
            }
            return true;
        }
        
        // Try to teleport using delayed teleport
        if (plugin.getTeleportManager().initiateDelayedTeleport(
                player, 
                homeLocation, 
                TeleportManager.TeleportType.HOME)) {
            // Teleport was initiated with delay
            return true;
        }
        
        // Immediate teleport if no delay
        player.teleport(homeLocation);
        player.sendMessage(formatMessage("§aTeleported to home" + (homeName.equals("default") ? "" : " '" + homeName + "'") + "!"));
        return true;
    }
    
    private boolean handleSetHome(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.sethome", "§cYou don't have permission to set homes!")) {
            return true;
        }
        
        String homeName = "default";
        if (args.length > 0) {
            homeName = args[0].toLowerCase();
        }
        
        // Check for home limit
        int maxHomes = getHomeLimit(player);
        int currentHomes = plugin.getLocationManager().getHomeCount(player);
        
        // If this is a new home (not an overwrite), check against the limit
        if (currentHomes >= maxHomes && !plugin.getLocationManager().homeExists(player, homeName)) {
            player.sendMessage(formatMessage("§cYou've reached your limit of " + maxHomes + " homes!"));
            return true;
        }
        
        // Set the home
        plugin.getLocationManager().setHome(player, homeName, player.getLocation());
        
        // Message based on whether it's the default home
        if (homeName.equals("default")) {
            player.sendMessage(formatMessage("§aHome set!"));
        } else {
            player.sendMessage(formatMessage("§aHome '" + homeName + "' set!"));
        }
        
        return true;
    }
    
    private boolean handleDelHome(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.sethome.del", "§cYou don't have permission to delete homes!")) {
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /delhome <name>"));
            return true;
        }
        
        String homeName = args[0];
        if (plugin.getLocationManager().getHome(player, homeName) == null) {
            player.sendMessage(formatMessage("§cHome not found!"));
            return true;
        }
        
        plugin.getLocationManager().deleteHome(player, homeName);
        player.sendMessage(formatMessage("§aHome '" + homeName + "' has been deleted!"));
        return true;
    }
    
    private boolean handleHomeList(CommandSender sender) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.home.list", "§cYou don't have permission to list homes!")) {
            return true;
        }
        
        Set<String> homes = plugin.getLocationManager().getHomes(player);
        if (homes == null || homes.isEmpty()) {
            player.sendMessage(formatMessage("§cYou haven't set any homes!"));
            return true;
        }
        
        player.sendMessage(formatMessage("§aYour homes:"));
        for (String home : homes) {
            player.sendMessage(formatMessage("§7- §f" + home));
        }
        return true;
    }
    
    private boolean handleEditHome(CommandSender sender, String[] args) {
        Player player = getPlayerSender(sender);
        if (player == null) return true;
        
        if (!hasPermission(player, "ssjessentials.sethome.edit", "§cYou don't have permission to edit homes!")) {
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(formatMessage("§cUsage: /edithome <current name> <new name>"));
            return true;
        }
        
        String currentName = args[0];
        String newName = args[1];
        
        if (newName.length() > 16) {
            player.sendMessage(formatMessage("§cHome name cannot be longer than 16 characters!"));
            return true;
        }
        
        Location homeLocation = plugin.getLocationManager().getHome(player, currentName);
        
        if (homeLocation == null) {
            player.sendMessage(formatMessage("§cHome not found!"));
            return true;
        }
        
        boolean success = plugin.getLocationManager().setHome(player, newName, homeLocation);
        if (success) {
            plugin.getLocationManager().deleteHome(player, currentName);
            player.sendMessage(formatMessage("§aHome renamed from '" + currentName + "' to '" + newName + "'"));
        } else {
            player.sendMessage(formatMessage("§cFailed to rename home. Please try again."));
        }
        
        return true;
    }

    /**
     * Determine the maximum number of homes a player can have based on permissions
     * @param player The player to check
     * @return The maximum number of homes allowed
     */
    private int getHomeLimit(Player player) {
        // Check for specific home limit permissions (from highest to lowest)
        for (int i = 50; i >= 1; i--) {
            if (player.hasPermission("ssjessentials.homes." + i)) {
                return i;
            }
        }
        
        // Default is 1 home
        return 1;
    }
} 