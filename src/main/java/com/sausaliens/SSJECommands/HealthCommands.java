package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import com.sausaliens.SSJEssentials;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles health-related commands like heal, feed, and god
 */
public class HealthCommands extends BaseCommand {

    public HealthCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "heal":
                return handleHeal(sender, args);
            case "feed":
                return handleFeed(sender, args);
            case "god":
                return handleGod(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String perm = switch (command.getName().toLowerCase()) {
                case "heal" -> "ssjessentials.heal.others";
                case "feed" -> "ssjessentials.feed.others";
                case "god" -> "ssjessentials.god.others";
                default -> "";
            };
            
            if (!perm.isEmpty() && sender.hasPermission(perm)) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    private boolean handleHeal(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Self-heal
            Player player = getPlayerSender(sender);
            if (player == null) {
                sender.sendMessage(formatMessage("§cUsage: /heal <player>"));
                return true;
            }
            
            if (!hasPermission(player, "ssjessentials.heal")) {
                return true;
            }
            
            healPlayer(player);
            player.sendMessage(formatMessage("§aYou have been healed!"));
            return true;
        }

        // Heal others
        if (!hasPermission(sender, "ssjessentials.heal.others")) {
            return true;
        }

        Player target = getTargetPlayer(sender, args[0]);
        if (target == null) return true;

        healPlayer(target);
        target.sendMessage(formatMessage("§aYou have been healed by " + sender.getName() + "!"));
        sender.sendMessage(formatMessage("§aHealed " + target.getName() + "!"));
        return true;
    }

    private boolean handleFeed(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Self-feed
            Player player = getPlayerSender(sender);
            if (player == null) {
                sender.sendMessage(formatMessage("§cUsage: /feed <player>"));
                return true;
            }
            
            if (!hasPermission(player, "ssjessentials.feed")) {
                return true;
            }
            
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.sendMessage(formatMessage("§aYour hunger has been satisfied!"));
            return true;
        }

        // Feed others
        if (!hasPermission(sender, "ssjessentials.feed.others")) {
            return true;
        }

        Player target = getTargetPlayer(sender, args[0]);
        if (target == null) return true;

        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.sendMessage(formatMessage("§aYour hunger has been satisfied by " + sender.getName() + "!"));
        sender.sendMessage(formatMessage("§aFed " + target.getName() + "!"));
        return true;
    }

    private boolean handleGod(CommandSender sender, String[] args) {
        Player target;
        
        // Handle /god [player]
        if (args.length > 0) {
            if (!hasPermission(sender, "ssjessentials.god.others")) {
                return true;
            }
            target = getTargetPlayer(sender, args[0]);
            if (target == null) return true;
        } else {
            target = getPlayerSender(sender);
            if (target == null) {
                sender.sendMessage(formatMessage("§cUsage: /god <player>"));
                return true;
            }
            if (!hasPermission(target, "ssjessentials.god")) {
                return true;
            }
        }

        final Player finalTarget = target;
        plugin.getConfigs().getPlayerDataAsync(finalTarget).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + finalTarget.getName() + " in god command");
                return;
            }
            boolean newGodState = !playerData.isGodMode();
            playerData.setGodMode(newGodState);
            plugin.getConfigs().savePlayerData(finalTarget);
            finalTarget.setInvulnerable(newGodState);
            finalTarget.sendMessage(newGodState ? 
                formatMessage("§aGod mode enabled!") : 
                formatMessage("§cGod mode disabled!"));
            if (sender != finalTarget) {
                sender.sendMessage(newGodState ? 
                    formatMessage("§aEnabled god mode for " + finalTarget.getName()) : 
                    formatMessage("§cDisabled god mode for " + finalTarget.getName()));
            }
        });
        return true;
    }
    
    /**
     * Heals a player completely
     * @param player The player to heal
     */
    private void healPlayer(Player player) {
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
        player.setFireTicks(0);
        
        // Remove all potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
} 