package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles movement-related commands like fly and vanish
 */
public class MovementCommands extends BaseCommand {

    public MovementCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "fly":
                return handleFly(sender, args);
            case "vanish":
                return handleVanish(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && (command.getName().equalsIgnoreCase("fly") || 
                                 command.getName().equalsIgnoreCase("vanish"))) {
            String perm = command.getName().equalsIgnoreCase("fly") ? 
                         "ssjessentials.fly.others" : "ssjessentials.vanish.others";
                         
            if (sender.hasPermission(perm)) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private boolean handleFly(CommandSender sender, String[] args) {
        Player target;
        
        // Handle /fly [player]
        if (args.length > 0) {
            if (!hasPermission(sender, "ssjessentials.fly.others")) {
                return true;
            }
            target = getTargetPlayer(sender, args[0]);
            if (target == null) return true;
        } else {
            target = getPlayerSender(sender);
            if (target == null) return true;
            if (!hasPermission(target, "ssjessentials.fly")) {
                return true;
            }
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(formatMessage("§cCannot toggle flight in " + 
                target.getGameMode().toString().toLowerCase() + " mode!"));
            return true;
        }

        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in fly command");
                return;
            }
            boolean newFlyState = !playerData.isFlying();
            playerData.setFlying(newFlyState);
            plugin.getConfigs().savePlayerData(target);
            target.setAllowFlight(newFlyState);
            if (!newFlyState) {
                target.setFlying(false);
            }
            target.sendMessage(newFlyState ? 
                formatMessage("§aFlight mode enabled!") : 
                formatMessage("§cFlight mode disabled!"));
            if (sender != target) {
                sender.sendMessage(newFlyState ? 
                    formatMessage("§aEnabled flight mode for " + target.getName()) : 
                    formatMessage("§cDisabled flight mode for " + target.getName()));
            }
        });
        return true;
    }
    
    private boolean handleVanish(CommandSender sender, String[] args) {
        Player target;
        
        // Handle /vanish [player]
        if (args.length > 0) {
            // Check if sender has permission to change other players' visibility
            if (!hasPermission(sender, "ssjessentials.vanish.others")) {
                return true;
            }
            
            target = getTargetPlayer(sender, args[0]);
            if (target == null) return true;
        } else {
            // Handle /vanish
            target = getPlayerSender(sender);
            if (target == null) return true;
            
            if (!hasPermission(target, "ssjessentials.vanish")) {
                return true;
            }
        }

        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in vanish command");
                return;
            }
            boolean newVanishState = !playerData.isVanished();
            
            // Show or hide the player for all online players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer != target) {
                    if (newVanishState) {
                        onlinePlayer.hidePlayer(plugin, target);
                    } else {
                        onlinePlayer.showPlayer(plugin, target);
                    }
                }
            }
            
            // Update player data
            target.setInvisible(newVanishState);
            playerData.setVanished(newVanishState);
            plugin.getConfigs().savePlayerData(target);
            
            // Send messages
            target.sendMessage(newVanishState ? 
                formatMessage("§aYou are now invisible!") : 
                formatMessage("§cYou are now visible!"));
            
            if (sender != target) {
                sender.sendMessage(newVanishState ? 
                    formatMessage("§aMade " + target.getName() + " invisible!") : 
                    formatMessage("§cMade " + target.getName() + " visible!"));
            }
        });
        return true;
    }
} 