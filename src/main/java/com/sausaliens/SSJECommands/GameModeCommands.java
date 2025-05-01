package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles gamemode-related commands like gm and god
 */
public class GameModeCommands extends BaseCommand {

    public GameModeCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "gm":
                return handleGamemode(sender, args);
            case "god":
                return handleGod(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "gm":
                if (args.length == 1) {
                    // Suggest gamemodes: 0, 1, 2, 3
                    List<String> gamemodes = Arrays.asList("0", "1", "2", "3");
                    return gamemodes.stream()
                        .filter(gm -> gm.startsWith(args[0]))
                        .collect(Collectors.toList());
                } else if (args.length == 2 && sender.hasPermission("ssjessentials.gamemode.others")) {
                    // Suggest online players
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
            case "god":
                if (args.length == 1 && sender.hasPermission("ssjessentials.god.others")) {
                    // Suggest online players
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
        }
        return new ArrayList<>();
    }

    private boolean handleGamemode(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /gm <0/1/2/3> [player]"));
            return true;
        }

        String mode = args[0];
        Player target;

        if (args.length > 1) {
            if (!hasPermission(sender, "ssjessentials.gamemode.others")) {
                return true;
            }
            target = getTargetPlayer(sender, args[1]);
            if (target == null) return true;
        } else {
            target = getPlayerSender(sender);
            if (target == null) return true;
            if (!hasPermission(sender, "ssjessentials.gamemode")) {
                return true;
            }
        }

        GameMode gameMode;
        switch (mode) {
            case "0":
                gameMode = GameMode.SURVIVAL;
                break;
            case "1":
                gameMode = GameMode.CREATIVE;
                break;
            case "2":
                gameMode = GameMode.ADVENTURE;
                break;
            case "3":
                gameMode = GameMode.SPECTATOR;
                break;
            default:
                sender.sendMessage(formatMessage("§cInvalid gamemode! Use 0/1/2/3"));
                return true;
        }

        target.setGameMode(gameMode);
        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in gamemode command");
                return;
            }
            playerData.setGameMode(gameMode);
            plugin.getConfigs().savePlayerData(target);
            target.sendMessage(formatMessage("§aGamemode set to " + gameMode.toString().toLowerCase()));
            if (sender != target) {
                sender.sendMessage(formatMessage("§aSet " + target.getName() + "'s gamemode to " + gameMode.toString().toLowerCase()));
            }
        });
        return true;
    }

    private boolean handleGod(CommandSender sender, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(formatMessage("§cUsage: /god <player>"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            target = (Player) sender;
            if (!hasPermission(target, "ssjessentials.god")) {
                return true;
            }
        } else {
            if (!hasPermission(sender, "ssjessentials.god.others")) {
                return true;
            }
            target = getTargetPlayer(sender, args[0]);
            if (target == null) return true;
        }

        plugin.getConfigs().getPlayerDataAsync(target).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + target.getName() + " in god command");
                return;
            }
            boolean newGodState = !playerData.isGodMode();
            playerData.setGodMode(newGodState);
            plugin.getConfigs().savePlayerData(target);
            target.setInvulnerable(newGodState);
            target.sendMessage(newGodState ? formatMessage("§aGod mode enabled!") : formatMessage("§cGod mode disabled!"));
            if (sender != target) {
                sender.sendMessage(newGodState ? 
                    formatMessage("§aEnabled god mode for " + target.getName()) : 
                    formatMessage("§cDisabled god mode for " + target.getName()));
            }
        });
        return true;
    }
} 