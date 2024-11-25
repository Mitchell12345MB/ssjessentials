package com.sausaliens.SSJECommands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEConfig.SSJConfigs;
import java.util.Arrays;
import java.util.Date;
import org.bukkit.ChatColor;
import java.util.Set;
import org.bukkit.BanList;
import org.bukkit.BanEntry;
import java.text.SimpleDateFormat;

public class SSJECommands implements CommandExecutor {

    private final SSJEssentials ssjEssentials;

    public SSJECommands(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("reload")) {
            return handleReload(sender);
        }

        if (command.getName().equalsIgnoreCase("banlist")) {
            return handleBanCommand(sender, args);
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "fly":
                return handleFly(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "vanish":
                return handleVanish(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "heal":
                return handleHeal(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "feed":
                return handleFeed(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "freeze":
                return handleFreeze(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "tempban":
                return handleTempban(player, args);
            case "nick":
                return handleNick(player, args);
            case "gm":
                return handleGamemode(player, args);
            case "//ban":
                return handleBanCommand(sender, args);
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

    private boolean handleTempban(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tempban")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tempban <player> <duration> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String duration = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) 
                                      : ssjEssentials.getConfig().getString("tempban.default-reason");

        // Parse duration (you might want to add more sophisticated duration parsing)
        long durationInMillis = parseDuration(duration);
        if (durationInMillis <= 0) {
            sender.sendMessage("§cInvalid duration format!");
            return true;
        }

        Date expiry = new Date(System.currentTimeMillis() + durationInMillis);
        target.ban(reason, expiry, sender.getName(), true);
        
        if (ssjEssentials.getConfig().getBoolean("tempban.broadcast")) {
            Bukkit.broadcastMessage("§c" + target.getName() + " has been temporarily banned for: " + reason);
        }
        
        return true;
    }

    private boolean handleNick(Player sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /nick [player] <nickname>");
            return true;
        }

        Player target;
        String nickname;

        if (args.length > 1 && sender.hasPermission("ssjessentials.nick.others")) {
            target = Bukkit.getPlayer(args[0]);
            nickname = args[1];
        } else {
            target = sender;
            nickname = args[0];
        }

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (!sender.hasPermission("ssjessentials.nick" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        int maxLength = ssjEssentials.getConfig().getInt("nickname.max-length", 16);
        if (nickname.length() > maxLength) {
            sender.sendMessage("§cNickname too long! Maximum length is " + maxLength + " characters.");
            return true;
        }

        if (ssjEssentials.getConfig().getBoolean("nickname.allow-colors", true)) {
            nickname = ChatColor.translateAlternateColorCodes('&', nickname);
        }

        target.setDisplayName(nickname);
        target.setPlayerListName(nickname);
        
        target.sendMessage("§aYour nickname has been changed to: " + nickname);
        if (sender != target) {
            sender.sendMessage("§aChanged " + target.getName() + "'s nickname to: " + nickname);
        }
        
        return true;
    }

    private boolean handleGamemode(Player sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /gm <0/1/2/3> [player]");
            return true;
        }

        Player target;
        String mode = args[0];

        if (args.length > 1 && sender.hasPermission("ssjessentials.gamemode.others")) {
            target = Bukkit.getPlayer(args[1]);
        } else {
            target = sender;
        }

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (!sender.hasPermission("ssjessentials.gamemode" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
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
                sender.sendMessage("§cInvalid gamemode! Use 0/1/2/3");
                return true;
        }

        target.setGameMode(gameMode);
        
        // Save the gamemode in player data
        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        playerData.setGameMode(gameMode);
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage("§aGamemode set to " + gameMode.toString().toLowerCase());
        if (sender != target) {
            sender.sendMessage("§aSet " + target.getName() + "'s gamemode to " + gameMode.toString().toLowerCase());
        }
        
        return true;
    }

    private long parseDuration(String duration) {
        try {
            char unit = duration.charAt(duration.length() - 1);
            int amount = Integer.parseInt(duration.substring(0, duration.length() - 1));
            
            return switch (Character.toLowerCase(unit)) {
                case 'm' -> amount * 60L * 1000L;         // minutes
                case 'h' -> amount * 60L * 60L * 1000L;   // hours
                case 'd' -> amount * 24L * 60L * 60L * 1000L; // days
                default -> -1L;
            };
        } catch (Exception e) {
            return -1L;
        }
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("ssjessentials.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        try {
            ssjEssentials.reloadConfig();
            sender.sendMessage("§aConfiguration reloaded successfully!");
            return true;
        } catch (Exception e) {
            sender.sendMessage("§cError reloading configuration: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean handleBanCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.banlist")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        @SuppressWarnings("rawtypes")
        Set<BanEntry> banEntries = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();
        
        if (banEntries.isEmpty()) {
            sender.sendMessage("§aThere are no banned players.");
            return true;
        }

        sender.sendMessage("§6Banned Players:");
        for (@SuppressWarnings("rawtypes") BanEntry entry : banEntries) {
            String expiry = entry.getExpiration() != null ? 
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.getExpiration()) : 
                "Permanent";
                
            sender.sendMessage(String.format("§e- %s §7(Until: %s)", 
                entry.getTarget(), 
                expiry));
        }

        return true;
    }
} 