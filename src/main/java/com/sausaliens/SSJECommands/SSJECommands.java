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

public class SSJECommands implements CommandExecutor {

    private final SSJEssentials ssjEssentials;

    public SSJECommands(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
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
            case "god":
                return handleGod(player, args.length > 0 ? Bukkit.getPlayer(args[0]) : player);
            case "unban":
                return handleUnban(sender, args);
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
            case "kill":
                return handleKill(player, args);
            case "killall":
                return handleKillAll(player, args);
            case "ban":
                return handleBanPlayer(player, args);
            case "kick":
                return handleKickPlayer(player, args);
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
            sender.sendMessage("§cUsage: /nick <nickname> or /nick <player> <nickname>");
            return true;
        }

        Player target = sender;
        String nickname;

        if (args.length > 1) {
            if (!sender.hasPermission("ssjessentials.nick.others")) {
                sender.sendMessage("§cYou don't have permission to change other players' nicknames!");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
            nickname = args[1];
        } else {
            if (!sender.hasPermission("ssjessentials.nick")) {
                sender.sendMessage("§cYou don't have permission to change your nickname!");
                return true;
            }
            nickname = args[0];
        }

        int maxLength = ssjEssentials.getConfig().getInt("nickname.max-length", 16);
        if (nickname.length() > maxLength) {
            sender.sendMessage("§cNickname too long! Maximum length is " + maxLength + " characters.");
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

        String mode = args[0];
        Player target = sender;

        if (args.length > 1) {
            if (!sender.hasPermission("ssjessentials.gamemode.others")) {
                sender.sendMessage("§cYou don't have permission to change other players' gamemode!");
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage("§6§lBanned Players List:");
        sender.sendMessage("§7§m----------------------------------------");

        for (@SuppressWarnings("rawtypes") BanEntry entry : banEntries) {
            sender.sendMessage("§e" + entry.getTarget());
            sender.sendMessage("§7Reason: §f" + entry.getReason());
            sender.sendMessage("§7Banned by: §f" + entry.getSource());
            sender.sendMessage("§7Banned on: §f" + dateFormat.format(entry.getCreated()));
            
            if (entry.getExpiration() != null) {
                sender.sendMessage("§7Expires on: §f" + dateFormat.format(entry.getExpiration()));
            } else {
                sender.sendMessage("§7Expires: §cNever (Permanent)");
            }
            sender.sendMessage("§7§m----------------------------------------");
        }
        
        return true;
    }

    private boolean handleGod(Player sender, Player target) {
        if (!sender.hasPermission("ssjessentials.god" + (sender == target ? "" : ".others"))) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        SSJConfigs.PlayerData playerData = ssjEssentials.getConfigs().getPlayerData(target);
        boolean newGodState = !playerData.isGodMode();
        playerData.setGodMode(newGodState);
        ssjEssentials.getConfigs().savePlayerData(target);

        target.setInvulnerable(newGodState);
        
        target.sendMessage(newGodState ? "§aGod mode enabled!" : "§cGod mode disabled!");
        if (sender != target) {
            sender.sendMessage(newGodState ? 
                "§aEnabled god mode for " + target.getName() : 
                "§cDisabled god mode for " + target.getName());
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.unban")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unban <player>");
            return true;
        }

        String targetName = args[0];
        
        // Check if player exists in offline players
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer has never joined the server!");
            return true;
        }

        if (!Bukkit.getBanList(BanList.Type.NAME).isBanned(targetName)) {
            sender.sendMessage("§cPlayer is not banned!");
            return true;
        }

        Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
        sender.sendMessage("§aUnbanned player: " + targetName);
        
        if (ssjEssentials.getConfig().getBoolean("ban.broadcast", true)) {
            String unbannedBy = sender instanceof Player ? sender.getName() : "Console";
            Bukkit.broadcastMessage("§a" + targetName + " has been unbanned by " + unbannedBy);
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
                sender.sendMessage("§cYou don't have permission to teleport others to spawn!");
                return true;
            }
            teleportToSpawn(targetPlayer);
            sender.sendMessage("§aTeleported " + targetPlayer.getName() + " to spawn!");
            return true;
        }

        // If not a player, try to spawn entity
        if (!sender.hasPermission("ssjessentials.spawn.mob")) {
            sender.sendMessage("§cYou don't have permission to spawn mobs!");
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
                        sender.sendMessage("§cYou don't have permission to spawn mobs at other players!");
                        return true;
                    }
                    spawnLoc = targetLoc.getLocation();
                } else {
                    sender.sendMessage("§cTarget player not found!");
                    return true;
                }
            }

            for (int i = 0; i < amount; i++) {
                Entity entity = sender.getWorld().spawnEntity(spawnLoc, entityType);
                if (health > 0 && entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setHealth(health);
                }
            }
            
            String message = "§aSpawned " + amount + " " + entityType.toString().toLowerCase();
            if (health > 0) {
                message += " with " + health + " health";
            }
            if (args.length > 3) {
                message += " at " + args[3];
            }
            sender.sendMessage(message);
            
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid entity type, amount, or health value!");
            return true;
        }
        return true;
    }

    private void teleportToSpawn(Player player) {
        Location spawn = ssjEssentials.getSpawnConfig().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage("§cSpawn location has not been set!");
            return;
        }
        player.teleport(spawn);
        player.sendMessage("§aTeleported to spawn!");
    }

    private boolean handleSetSpawn(Player sender) {
        if (!sender.hasPermission("ssjessentials.setspawn")) {
            sender.sendMessage("§cYou don't have permission to set spawn!");
            return true;
        }

        ssjEssentials.getSpawnConfig().setSpawnLocation(sender.getLocation());
        sender.sendMessage("§aSpawn location set!");
        return true;
    }

    private boolean handleTeleportRequest(Player sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage("§cUsage: /tpr <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target == sender) {
            sender.sendMessage("§cYou cannot teleport to yourself!");
            return true;
        }

        ssjEssentials.getTeleportManager().createRequest(sender, target);
        sender.sendMessage("§aTeleport request sent to " + target.getName());
        target.sendMessage("§a" + sender.getName() + " wants to teleport to you.");
        target.sendMessage("§aType /tpraccept to accept.");
        return true;
    }

    private boolean handleTeleportAccept(Player sender) {
        if (!ssjEssentials.getTeleportManager().hasActiveRequest(sender)) {
            sender.sendMessage("§cYou have no pending teleport requests!");
            return true;
        }

        Player requester = ssjEssentials.getTeleportManager().getRequester(sender);
        if (requester == null || !requester.isOnline()) {
            sender.sendMessage("§cThe player who requested to teleport is no longer online!");
            ssjEssentials.getTeleportManager().removeRequest(sender);
            return true;
        }

        requester.teleport(sender.getLocation());
        requester.sendMessage("§aTeleported to " + sender.getName());
        sender.sendMessage("§a" + requester.getName() + " was teleported to you.");
        ssjEssentials.getTeleportManager().removeRequest(sender);
        return true;
    }

    private boolean handleTeleport(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.tp.staff")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /tp <player> [target/spawn/x y z]");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (args.length == 1) {
            // Teleport sender to player
            sender.teleport(player.getLocation());
            sender.sendMessage("§aTeleported to " + player.getName());
            return true;
        }

        if (args[1].equalsIgnoreCase("spawn")) {
            // Teleport player to spawn
            player.teleport(ssjEssentials.getSpawnConfig().getSpawnLocation());
            player.sendMessage("§aYou were teleported to spawn");
            sender.sendMessage("§aTeleported " + player.getName() + " to spawn");
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
                player.sendMessage("§aYou were teleported to " + x + " " + y + " " + z);
                sender.sendMessage("§aTeleported " + player.getName() + " to coordinates");
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid coordinates!");
                return true;
            }
        }

        // Handle player to player teleport
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cTarget player not found!");
            return true;
        }

        player.teleport(target.getLocation());
        player.sendMessage("§aYou were teleported to " + target.getName());
        sender.sendMessage("§aTeleported " + player.getName() + " to " + target.getName());
        return true;
    }

    private boolean handleKill(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kill")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kill <player> OR /kill <entity>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target != null) {
            target.setHealth(0);
            sender.sendMessage("§aKilled " + target.getName());
            return true;
        }

        // If no player found, try to kill entities (requires additional permission)
        if (!sender.hasPermission("ssjessentials.kill.entitys")) {
            sender.sendMessage("§cPlayer not found and you don't have permission to kill entities!");
            return true;
        }

        // Implementation for killing specific entity types can be added here
        sender.sendMessage("§cEntity killing not implemented yet!");
        return true;
    }

    private boolean handleKillAll(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kill.all")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("entities")) {
            if (!sender.hasPermission("ssjessentials.kill.entitys")) {
                sender.sendMessage("§cYou don't have permission to kill entities!");
                return true;
            }
            int count = 0;
            for (Entity entity : sender.getWorld().getEntities()) {
                if (!(entity instanceof Player)) {
                    entity.remove();
                    count++;
                }
            }
            sender.sendMessage("§aKilled " + count + " entities");
            return true;
        }

        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player != sender) {
                player.setHealth(0);
                count++;
            }
        }

        sender.sendMessage("§aKilled " + count + " players");
        return true;
    }

    @SuppressWarnings("deprecation")
    private boolean handleBanPlayer(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.ban") || !sender.hasPermission("ssjessentials.staff")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ban <player> [reason]");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : ssjEssentials.getConfig().getString("ban.default-reason", "No reason specified");

        // Ban the player using BanList
        @SuppressWarnings({ "rawtypes" })
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        banList.addBan(targetName, reason, (Date)null, sender.getName());

        // Kick the player if they are online
        if (target.isOnline()) {
            Player onlinePlayer = target.getPlayer();
            onlinePlayer.kickPlayer(reason);
        }

        if (ssjEssentials.getConfig().getBoolean("ban.broadcast", true)) {
            Bukkit.broadcastMessage("§c" + targetName + " has been banned by " + sender.getName() + " for: " + reason);
        }

        return true;
    }

    private boolean handleKickPlayer(Player sender, String[] args) {
        if (!sender.hasPermission("ssjessentials.kick") || !sender.hasPermission("ssjessentials.staff")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /kick <player> [reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : ssjEssentials.getConfig().getString("kick.default-reason", "No reason specified");

        // Kick the player
        target.kickPlayer(reason);

        if (ssjEssentials.getConfig().getBoolean("kick.broadcast", true)) {
            Bukkit.broadcastMessage("§c" + target.getName() + " has been kicked by " + sender.getName() + " for: " + reason);
        }

        return true;
    }
} 