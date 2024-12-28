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
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionEffect;

public class SSJECommands implements CommandExecutor {

    private final SSJEssentials ssjEssentials;

    public SSJECommands(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
    }

    public String formatMessage(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            ssjEssentials.getConfig().getString("prefix", "&7[&bSSJ&7] "));
        return prefix + ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("ssjereload")) {
            return handleReload(sender);
        }

        if (command.getName().equalsIgnoreCase("banlist")) {
            return handleBanCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("unban")) {
            return handleUnban(sender, args);
        }

        String cmd = command.getName().toLowerCase();
        
        // Commands that can be used by both console and players
        switch (cmd) {
            case "ban":
                return handleBanPlayer(sender, args);
            case "kick":
                return handleKickPlayer(sender, args);
            case "tempban":
                return handleTempban(sender, args);
            case "unban":
                return handleUnban(sender, args);
            case "banlist":
                return handleBanCommand(sender, args);
            case "/ban":
                return handleBanCommand(sender, args);
            case "god":
                return handleGod(sender, args);
            case "kill":
                return handleKill(sender, args);
            case "killall":
                return handleKillAll(sender, args);
            case "heal":
                return handleHeal(sender, args);
            case "feed":
                return handleFeed(sender, args);
            case "freeze":
                return handleFreeze(sender, args);
            case "nick":
                return handleNick(sender, args);
            case "gm":
                return handleGamemode(sender, args);
            case "editwarp":
                return handleEditWarp(sender, args);
            case "resetwarp":
                return handleResetWarp(sender, args);
            case "warpall":
                return handleWarpAll(sender, args);
            case "spawnall":
                return handleSpawnAll(sender);
        }
        
        // Player-only commands below this point
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("§cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;

        switch (cmd) {
            case "fly":
                return handleFly(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "vanish":
                return handleVanish(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "spawn":
                return handleSpawn(player, args);
            case "setspawn":
                return handleSetSpawn(player);
            case "tpr":
                return handleTeleportRequest(player, args);
            case "tpraccept":
                return handleTeleportAccept(player);
            case "tp":
                return handleTeleport(player, args);
            case "warp":
                return handleWarp(player, args);
            case "setwarp":
                return handleSetWarp(player, args);
            case "delwarp":
                return handleDelWarp(player, args);
            case "/warp":
                return handleWarpList(sender);
            case "home":
                return handleHome(player, args);
            case "sethome":
                return handleSetHome(player, args);
            case "delhome":
                return handleDelHome(player, args);
            case "/home":
                return handleHomeList(player);
            case "editwarp":
                return handleEditWarp(sender, args);
            case "resetwarp":
                return handleResetWarp(sender, args);
            case "warpall":
                return handleWarpAll(sender, args);
            case "spawnall":
                return handleSpawnAll(sender);
            default:
                return false;
        }
    }

    private boolean handleFly(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.fly" + (sender == target ? "" : ".others"))) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (target.getGameMode() == GameMode.CREATIVE || target.getGameMode() == GameMode.SPECTATOR) {
            sender.sendMessage(formatMessage("§cCannot toggle flight in " + target.getGameMode().toString().toLowerCase() + " mode!"));
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
        
        target.sendMessage(newFlyState ? formatMessage("§aFlight mode enabled!") : formatMessage("§cFlight mode disabled!"));
        if (sender != target) {
            sender.sendMessage(newFlyState ? 
                formatMessage("§aEnabled flight mode for " + target.getName()) : 
                formatMessage("§cDisabled flight mode for " + target.getName()));
        }
        return true;
    }

    private boolean handleVanish(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.vanish" + (sender == target ? "" : ".others"))) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
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
        
        target.sendMessage(newVanishState ? formatMessage("§aYou are now invisible!") : formatMessage("§cYou are now visible!"));
        if (sender != target) {
            sender.sendMessage(newVanishState ? 
                formatMessage("§aMade " + target.getName() + " invisible!") : 
                formatMessage("§cMade " + target.getName() + " visible!"));
        }
        return true;
    }

    private boolean handleHeal(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(formatMessage("§cUsage: /heal <player>"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("ssjessentials.heal")) {
                player.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
                return true;
            }
            healPlayer(player);
            player.sendMessage(formatMessage("§aYou have been healed!"));
            return true;
        }

        if (!sender.hasPermission("ssjessentials.heal.others")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to heal other players!"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        healPlayer(target);
        target.sendMessage(formatMessage("§aYou have been healed by " + sender.getName() + "!"));
        sender.sendMessage(formatMessage("§aHealed " + target.getName() + "!"));
        return true;
    }

    @SuppressWarnings("deprecation")
    private void healPlayer(Player player) {
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0f);
        player.setFireTicks(0);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    private boolean handleFeed(CommandSender sender, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(formatMessage("§cUsage: /feed <player>"));
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("ssjessentials.feed")) {
                player.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
                return true;
            }
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.sendMessage(formatMessage("§aYour hunger has been satisfied!"));
            return true;
        }

        if (!sender.hasPermission("ssjessentials.feed.others")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to feed other players!"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.sendMessage(formatMessage("§aYour hunger has been satisfied by " + sender.getName() + "!"));
        sender.sendMessage(formatMessage("§aFed " + target.getName() + "!"));
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.freeze" + (sender instanceof Player ? "" : ".others"))) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newFrozenState = !playerData.isFrozen();
        
        playerData.setFrozen(newFrozenState);
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage(newFrozenState ? formatMessage("§cYou have been frozen!") : formatMessage("§aYou have been unfrozen!"));
        if (sender != target) {
            sender.sendMessage(newFrozenState ? 
                formatMessage("§aFroze " + target.getName() + "!") : 
                formatMessage("§aUnfroze " + target.getName() + "!"));
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleTempban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tempban")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(formatMessage("§cUsage: /tempban <player> <duration> [reason]"));
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        String duration = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) 
                                      : ssjEssentials.getConfig().getString("tempban.default-reason");

        long durationInMillis = parseDuration(duration);
        if (durationInMillis <= 0) {
            sender.sendMessage(formatMessage("§cInvalid duration format!"));
            return true;
        }

        Date expiry = new Date(System.currentTimeMillis() + durationInMillis);
        Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName(), reason, expiry, sender.getName());
        
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(reason);
        }
        
        if (ssjEssentials.getConfig().getBoolean("tempban.broadcast")) {
            Bukkit.broadcastMessage(formatMessage("§c" + target.getName() + " has been temporarily banned for: " + reason));
        }
        
        return true;
    }

    public boolean handleNick(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /nick <nickname> or /nick <player> <nickname>"));
            return true;
        }

        Player target = sender instanceof Player ? (Player) sender : Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        String nickname;

        if (args.length > 1) {
            if (!sender.hasPermission("ssjessentials.nick.others")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to change other players' nicknames!"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(formatMessage("§cPlayer not found!"));
                return true;
            }
            nickname = args[1];
        } else {
            if (!sender.hasPermission("ssjessentials.nick")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to change your nickname!"));
                return true;
            }
            nickname = args[0];
        }

        // Handle "none" option to remove nickname
        if (nickname.equalsIgnoreCase("none")) {
            // Save nickname in player data
            SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
            playerData.setNickname(null);
            ssjEssentials.getConfigs().savePlayerData(target);

            target.setDisplayName(target.getName());
            // Update tab list name with group prefix
            ssjEssentials.getGroupManager().updatePlayerTabName(target);
            
            target.sendMessage(formatMessage("§aYour nickname has been removed."));
            if (sender != target) {
                sender.sendMessage(formatMessage("§aRemoved " + target.getName() + "'s nickname."));
            }
            return true;
        }

        int maxLength = ssjEssentials.getConfig().getInt("nickname.max-length", 16);
        if (nickname.length() > maxLength) {
            sender.sendMessage(formatMessage("§cNickname too long! Maximum length is " + maxLength + " characters."));
            return true;
        }

        if (ssjEssentials.getConfig().getBoolean("nickname.allow-colors", true)) {
            nickname = ChatColor.translateAlternateColorCodes('&', nickname);
        }

        // Save nickname in player data
        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        playerData.setNickname(nickname);
        ssjEssentials.getConfigs().savePlayerData(target);

        target.setDisplayName(nickname);
        // Update tab list name with group prefix and new nickname
        ssjEssentials.getGroupManager().updatePlayerTabName(target);
        
        target.sendMessage(formatMessage("§aYour nickname has been changed to: " + nickname));
        if (sender != target) {
            sender.sendMessage(formatMessage("§aChanged " + target.getName() + "'s nickname to: " + nickname));
        }
        
        return true;
    }

    private boolean handleGamemode(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /gm <0/1/2/3> [player]"));
            return true;
        }

        String mode = args[0];
        Player target = sender instanceof Player ? (Player) sender : Bukkit.getPlayer(args[1]);

        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        if (args.length > 1) {
            if (!sender.hasPermission("ssjessentials.gamemode.others")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to change other players' gamemode!"));
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(formatMessage("§cPlayer not found!"));
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
        
        // Save the gamemode in player data
        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        playerData.setGameMode(gameMode);
        ssjEssentials.getConfigs().savePlayerData(target);
        
        target.sendMessage(formatMessage("§aGamemode set to " + gameMode.toString().toLowerCase()));
        if (sender != target) {
            sender.sendMessage(formatMessage("§aSet " + target.getName() + "'s gamemode to " + gameMode.toString().toLowerCase()));
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
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        try {
            ssjEssentials.reloadConfig();
            sender.sendMessage(formatMessage("§aConfiguration reloaded successfully!"));
            return true;
        } catch (Exception e) {
            sender.sendMessage(formatMessage("§cError reloading configuration: " + e.getMessage()));
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public boolean handleBanCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.banlist")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        @SuppressWarnings("rawtypes")
        Set<BanEntry> banEntries = Bukkit.getBanList(BanList.Type.NAME).getBanEntries();
        
        if (banEntries.isEmpty()) {
            sender.sendMessage(formatMessage("§aThere are no banned players."));
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage(formatMessage("§6§lBanned Players List:"));
        sender.sendMessage(formatMessage("§7§m----------------------------------------"));

        for (@SuppressWarnings("rawtypes") BanEntry entry : banEntries) {
            sender.sendMessage(formatMessage("§e" + entry.getTarget()));
            sender.sendMessage(formatMessage("§7Reason: §f" + entry.getReason()));
            sender.sendMessage(formatMessage("§7Banned by: §f" + entry.getSource()));
            sender.sendMessage(formatMessage("§7Banned on: §f" + dateFormat.format(entry.getCreated())));
            
            if (entry.getExpiration() != null) {
                sender.sendMessage(formatMessage("§7Expires on: §f" + dateFormat.format(entry.getExpiration())));
            } else {
                sender.sendMessage(formatMessage("§7Expires: §cNever (Permanent)"));
            }
            sender.sendMessage(formatMessage("§7§m----------------------------------------"));
        }
        
        return true;
    }

    private boolean handleGod(CommandSender sender, String[] args) {
        // If sender is not a player and no target is specified
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage(formatMessage("§cUsage: /god <player>"));
            return true;
        }

        // Get the target player - either the specified player or the sender
        Player target;
        if (args.length == 0) {
            target = (Player) sender;
            if (!sender.hasPermission("ssjessentials.god")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
                return true;
            }
        } else {
            if (!sender.hasPermission("ssjessentials.god.others")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to toggle god mode for others!"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(formatMessage("§cPlayer not found!"));
                return true;
            }
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newGodState = !playerData.isGodMode();
        playerData.setGodMode(newGodState);
        ssjEssentials.getConfigs().savePlayerData(target);

        target.setInvulnerable(newGodState);
        
        target.sendMessage(newGodState ? formatMessage("§aGod mode enabled!") : formatMessage("§cGod mode disabled!"));
        if (sender != target) {
            sender.sendMessage(newGodState ? 
                formatMessage("§aEnabled god mode for " + target.getName()) : 
                formatMessage("§cDisabled god mode for " + target.getName()));
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.unban")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /unban <player>"));
            return true;
        }

        String targetName = args[0];
        
        // Check if player exists in offline players
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(formatMessage("§cPlayer has never joined the server!"));
            return true;
        }

        if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(targetName)) {
            sender.sendMessage(formatMessage("§cPlayer is not banned!"));
            return true;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        sender.sendMessage(formatMessage("§aUnbanned player: " + targetName));
        
        if (ssjEssentials.getConfig().getBoolean("ban.broadcast", true)) {
            String unbannedBy = sender instanceof Player ? sender.getName() : "Console";
            Bukkit.broadcastMessage(formatMessage("§a" + targetName + " has been unbanned by " + unbannedBy));
        }
        
        return true;
    }

    private boolean handleSpawn(Player sender, String[] args) {
        if (args.length == 0) {
            // Teleport sender to spawn
            teleportToSpawn(sender);
            return true;
        }

        // Try to find player first
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer != null) {
            if (!sender.hasPermission("ssjessentials.spawntp.others")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to teleport others to spawn!"));
                return true;
            }
            teleportToSpawn(targetPlayer);
            sender.sendMessage(formatMessage("§aTeleported " + targetPlayer.getName() + " to spawn!"));
            return true;
        }

        // If not a player, try to spawn entity
        if (!sender.hasPermission("ssjessentials.spawn.mob")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to spawn mobs!"));
            return true;
        }

        try {
            EntityType entityType = EntityType.valueOf(args[0].toUpperCase());
            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            double health = args.length > 2 ? Double.parseDouble(args[2]) : -1;
            
            Location spawnLoc = sender.getLocation();
            if (args.length > 3) {
                Player targetLoc = Bukkit.getPlayer(args[3]);
                if (targetLoc != null) {
                    if (!sender.hasPermission("ssjessentials.spawn.mob.tp")) {
                        sender.sendMessage(formatMessage("§cYou don't have permission to spawn mobs at other players!"));
                        return true;
                    }
                    spawnLoc = targetLoc.getLocation();
                } else {
                    sender.sendMessage(formatMessage("§cTarget player not found!"));
                    return true;
                }
            }

            for (int i = 0; i < amount; i++) {
                Entity entity = sender.getWorld().spawnEntity(spawnLoc, entityType);
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
        Location spawn = ssjEssentials.getSpawnConfig().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(formatMessage("§cSpawn location has not been set!"));
            return;
        }
        player.teleport(spawn);
        player.sendMessage(formatMessage("§aTeleported to spawn!"));
    }

    private boolean handleSetSpawn(Player sender) {
        if (!sender.hasPermission("ssjessentials.setspawn")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to set spawn!"));
            return true;
        }

        ssjEssentials.getSpawnConfig().setSpawnLocation(sender.getLocation());
        sender.sendMessage(formatMessage("��aSpawn location set!"));
        return true;
    }

    private boolean handleTeleportRequest(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(formatMessage("§cUsage: /tpr <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        if (target == sender) {
            sender.sendMessage(formatMessage("§cYou cannot teleport to yourself!"));
            return true;
        }

        ssjEssentials.getTeleportManager().createRequest(sender, target);
        sender.sendMessage(formatMessage("§aTeleport request sent to " + target.getName()));
        target.sendMessage(formatMessage(sender.getName() + " wants to teleport to you."));
        target.sendMessage(formatMessage("Type /tpraccept to accept."));
        return true;
    }

    private boolean handleTeleportAccept(Player sender) {
        if (!ssjEssentials.getTeleportManager().hasActiveRequest(sender)) {
            sender.sendMessage(formatMessage("§cYou have no pending teleport requests!"));
            return true;
        }

        Player requester = ssjEssentials.getTeleportManager().getRequester(sender);
        if (requester == null || !requester.isOnline()) {
            sender.sendMessage(formatMessage("§cThe player who requested to teleport is no longer online!"));
            ssjEssentials.getTeleportManager().removeRequest(sender);
            return true;
        }

        requester.teleport(sender.getLocation());
        requester.sendMessage(formatMessage("§aTeleported to " + sender.getName()));
        sender.sendMessage(formatMessage(requester.getName() + " was teleported to you."));
        ssjEssentials.getTeleportManager().removeRequest(sender);
        return true;
    }

    private boolean handleTeleport(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tp.staff")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /tp <player> [target/spawn/x y z]"));
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        if (args.length == 1) {
            // Teleport sender to player
            sender.teleport(player.getLocation());
            sender.sendMessage(formatMessage("§aTeleported to " + player.getName()));
            return true;
        }

        if (args[1].equalsIgnoreCase("spawn")) {
            // Teleport player to spawn
            player.teleport(ssjEssentials.getSpawnConfig().getSpawnLocation());
            player.sendMessage(formatMessage("§aYou were teleported to spawn"));
            sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to spawn"));
            return true;
        }

        if (args.length == 4) {
            // Handle coordinates
            try {
                double x = Double.parseDouble(args[1]);
                double y = Double.parseDouble(args[2]);
                double z = Double.parseDouble(args[3]);
                Location loc = new Location(player.getWorld(), x, y, z);
                player.teleport(loc);
                player.sendMessage(formatMessage("§aYou were teleported to " + x + " " + y + " " + z));
                sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to coordinates"));
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(formatMessage("§cInvalid coordinates!"));
                return true;
            }
        }

        // Handle player to player teleport
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cTarget player not found!"));
            return true;
        }

        player.teleport(target.getLocation());
        player.sendMessage(formatMessage("§aYou were teleported to " + target.getName()));
        sender.sendMessage(formatMessage("§aTeleported " + player.getName() + " to " + target.getName()));
        return true;
    }

    private boolean handleKill(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kill")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /kill <player> OR /kill <entity>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            target.setHealth(0);
            sender.sendMessage(formatMessage("§aKilled " + target.getName()));
            return true;
        }

        // If no player found, try to kill entities (requires additional permission)
        if (!sender.hasPermission("ssjessentials.kill.entitys")) {
            sender.sendMessage(formatMessage("§cPlayer not found and you don't have permission to kill entities!"));
            return true;
        }

        // Implementation for killing specific entity types can be added here
        sender.sendMessage(formatMessage("§cEntity killing not implemented yet!"));
        return true;
    }

    private boolean handleKillAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kill.all")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("entities")) {
            if (!sender.hasPermission("ssjessentials.kill.entitys")) {
                sender.sendMessage(formatMessage("§cYou don't have permission to kill entities!"));
                return true;
            }
            int count = 0;
            for (Entity entity : ((Entity) sender).getWorld().getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                    count++;
                }
            }
            sender.sendMessage(formatMessage("§aKilled " + count + " entities"));
            return true;
        }

        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sender) {
                player.setHealth(0);
                count++;
            }
        }

        sender.sendMessage(formatMessage("§aKilled " + count + " players"));
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleBanPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.ban") || !sender.hasPermission("ssjessentials.staff")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /ban <player> [reason]"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : ssjEssentials.getConfig().getString("ban.default-reason", "No reason specified");

        Bukkit.getBanList(BanList.Type.NAME).addBan(targetName, reason, null, sender.getName());

        if (target.isOnline()) {
            target.getPlayer().kickPlayer(reason);
        }

        if (ssjEssentials.getConfig().getBoolean("ban.broadcast", true)) {
            Bukkit.broadcastMessage(formatMessage("§c" + targetName + " has been banned by " + sender.getName() + " for: " + reason));
        }

        return true;
    }

    private boolean handleKickPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kick") || !sender.hasPermission("ssjessentials.staff")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /kick <player> [reason]"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : ssjEssentials.getConfig().getString("kick.default-reason", "No reason specified");

        target.kickPlayer(reason);

        if (ssjEssentials.getConfig().getBoolean("kick.broadcast", true)) {
            Bukkit.broadcastMessage(formatMessage("§c" + target.getName() + " has been kicked by " + sender.getName() + " for: " + reason));
        }

        return true;
    }

    private boolean handleWarp(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.warp")) {
            player.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /warp <name>"));
            return true;
        }

        String warpName = args[0];
        Location warpLocation = ssjEssentials.getLocationManager().getWarp(warpName);
        
        if (warpLocation == null) {
            player.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }

        player.teleport(warpLocation);
        player.sendMessage(formatMessage("§aTeleported to warp: " + warpName));
        return true;
    }

    private boolean handleSetWarp(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.setwarp")) {
            player.sendMessage(formatMessage("§cYou don't have permission to set warps!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /setwarp <name> [description]"));
            return true;
        }

        String warpName = args[0];
        String description = args.length > 1 ? 
            String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : 
            "No description";

        ssjEssentials.getLocationManager().setWarp(warpName, player.getLocation(), description);
        player.sendMessage(formatMessage("§aWarp '" + warpName + "' has been set!"));
        return true;
    }

    private boolean handleDelWarp(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.setwarp.del")) {
            player.sendMessage(formatMessage("§cYou don't have permission to delete warps!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /delwarp <name>"));
            return true;
        }

        String warpName = args[0];
        if (ssjEssentials.getLocationManager().getWarp(warpName) == null) {
            player.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }

        ssjEssentials.getLocationManager().deleteWarp(warpName);
        player.sendMessage(formatMessage("§aWarp '" + warpName + "' has been deleted!"));
        return true;
    }

    public boolean handleWarpList(CommandSender sender) {
        if (!sender.hasPermission("ssjessentials.warp.list")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to list warps!"));
            return true;
        }

        Set<String> warps = ssjEssentials.getLocationManager().getWarps();
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

    private boolean handleHome(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.home")) {
            player.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /home <name>"));
            return true;
        }

        String homeName = args[0];
        Location homeLocation = ssjEssentials.getLocationManager().getHome(player, homeName);
        
        if (homeLocation == null) {
            player.sendMessage(formatMessage("§cHome not found!"));
            return true;
        }

        player.teleport(homeLocation);
        player.sendMessage(formatMessage("§aTeleported to home: " + homeName));
        return true;
    }

    private boolean handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.sethome")) {
            player.sendMessage(formatMessage("§cYou don't have permission to set homes!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /sethome <name>"));
            return true;
        }

        String homeName = args[0];
        int maxHomes = ssjEssentials.getLocationManager().getMaxHomes(player);
        Set<String> homes = ssjEssentials.getLocationManager().getHomes(player);
        
        if (homes.size() >= maxHomes && !homes.contains(homeName)) {
            player.sendMessage(formatMessage("§cYou have reached your maximum number of homes! (" + maxHomes + ")"));
            return true;
        }

        ssjEssentials.getLocationManager().setHome(player, homeName, player.getLocation());
        player.sendMessage(formatMessage("§aHome '" + homeName + "' has been set! (" + (homes.size() + 1) + "/" + maxHomes + ")"));
        return true;
    }

    private boolean handleDelHome(Player player, String[] args) {
        if (!player.hasPermission("ssjessentials.sethome.del")) {
            player.sendMessage(formatMessage("§cYou don't have permission to delete homes!"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(formatMessage("§cUsage: /delhome <name>"));
            return true;
        }

        String homeName = args[0];
        if (ssjEssentials.getLocationManager().getHome(player, homeName) == null) {
            player.sendMessage(formatMessage("§cHome not found!"));
            return true;
        }

        ssjEssentials.getLocationManager().deleteHome(player, homeName);
        player.sendMessage(formatMessage("§aHome '" + homeName + "' has been deleted!"));
        return true;
    }

    public boolean handleHomeList(Player player) {
        if (!player.hasPermission("ssjessentials.home.list")) {
            player.sendMessage(formatMessage("§cYou don't have permission to list homes!"));
            return true;
        }

        Set<String> homes = ssjEssentials.getLocationManager().getHomes(player);
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

    private boolean handleEditWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.setwarp.edit")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to edit warps!"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(formatMessage("§cUsage: /editwarp <current name> <new name>"));
            return true;
        }

        String currentName = args[0];
        String newName = args[1];
        Location warpLocation = ssjEssentials.getLocationManager().getWarp(currentName);
        
        if (warpLocation == null) {
            sender.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }

        ssjEssentials.getLocationManager().setWarp(newName, warpLocation, "");
        ssjEssentials.getLocationManager().deleteWarp(currentName);
        sender.sendMessage(formatMessage("§aWarp renamed from '" + currentName + "' to '" + newName + "'"));
        return true;
    }

    private boolean handleResetWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.setwarp.reset")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to reset warps!"));
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("§cThis command can only be used by players!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /resetwarp <name>"));
            return true;
        }

        String warpName = args[0];
        if (ssjEssentials.getLocationManager().getWarp(warpName) == null) {
            sender.sendMessage(formatMessage("§cWarp not found!"));
            return true;
        }

        Player player = (Player) sender;
        ssjEssentials.getLocationManager().setWarp(warpName, player.getLocation(), "");
        sender.sendMessage(formatMessage("§aWarp '" + warpName + "' has been reset to your location!"));
        return true;
    }

    private boolean handleWarpAll(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.warp.all")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to warp all players!"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /warpall <warp name>"));
            return true;
        }

        String warpName = args[0];
        Location warpLocation = ssjEssentials.getLocationManager().getWarp(warpName);
        
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

    private boolean handleSpawnAll(CommandSender sender) {
        if (!sender.hasPermission("ssjessentials.spawn.all")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to spawn all players!"));
            return true;
        }

        Location spawnLocation = ssjEssentials.getSpawnConfig().getSpawnLocation();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(spawnLocation);
            player.sendMessage(formatMessage("§aYou have been teleported to spawn"));
        }
        
        sender.sendMessage(formatMessage("§aAll players have been teleported to spawn"));
        return true;
    }
} 