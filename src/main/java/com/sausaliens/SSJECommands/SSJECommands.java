package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEConfig.SSJConfigs;

public class SSJECommands implements CommandExecutor {

    private final SSJEssentials ssjEssentials;

    public SSJECommands(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;

        if (args.length > 0 && target == null) {
            player.sendMessage("§cPlayer not found!");
            return true;
        }

        switch (cmd) {
            case "fly":
                return handleFly(player, target);
            case "vanish":
                return handleVanish(player, target);
            case "heal":
                return handleHeal(player, target);
            case "feed":
                return handleFeed(player, target);
            case "freeze":
                return handleFreeze(player, target);
            default:
                return false;
        }
    }

    private boolean handleFly(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.fly" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage("§cCannot toggle flight in " + target.getGameMode().toString().toLowerCase() + " mode!");
            return true;
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newFlyState = !playerData.isFlying();
        playerData.setFlying(newFlyState);
        
        target.setAllowFlight(newFlyState);
        if (!newFlyState) {
            target.setFlying(false);
        }
        
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage(newFlyState ? "§aFlight mode enabled!" : "§cFlight mode disabled!");
        if (sender != target) {
            sender.sendMessage(newFlyState ? 
                "§aEnabled flight mode for " + target.getName() : 
                "§cDisabled flight mode for " + target.getName());
        }
        return true;
    }

    private boolean handleVanish(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.vanish" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newVanishState = !playerData.isVanished();
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer != target) {
                if (newVanishState) {
                    onlinePlayer.hidePlayer(ssjEssentials, target);
                } else {
                    onlinePlayer.showPlayer(ssjEssentials, target);
                }
            }
        }
        
        target.setInvisible(newVanishState);
        playerData.setVanished(newVanishState);
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage(newVanishState ? "§aYou are now invisible!" : "§cYou are now visible!");
        if (sender != target) {
            sender.sendMessage(newVanishState ? 
                "§aMade " + target.getName() + " invisible!" : 
                "§cMade " + target.getName() + " visible!");
        }
        return true;
    }

    private boolean handleHeal(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.heal" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        double maxHealth = target.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        target.setHealth(maxHealth);
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setExhaustion(0f);
        
        target.sendMessage("§aYou have been fully healed!");
        if (sender != target) {
            sender.sendMessage("§aHealed " + target.getName() + "!");
        }
        return true;
    }

    private boolean handleFeed(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.feed" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setExhaustion(0f);
        
        target.sendMessage("§aYour hunger has been satisfied!");
        if (sender != target) {
            sender.sendMessage("§aFed " + target.getName() + "!");
        }
        return true;
    }

    private boolean handleFreeze(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.freeze" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newFrozenState = !playerData.isFrozen();
        
        playerData.setFrozen(newFrozenState);
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage(newFrozenState ? "§cYou have been frozen!" : "§aYou have been unfrozen!");
        if (sender != target) {
            sender.sendMessage(newFrozenState ? 
                "§aFroze " + target.getName() + "!" : 
                "§aUnfroze " + target.getName() + "!");
        }
        return true;
    }
} 