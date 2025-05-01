package com.sausaliens.SSJECommands;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEManagers.BanHistoryManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Handles moderation-related commands like ban, kick, tempban, and unban
 */
public class ModerationCommands extends BaseCommand {

    public ModerationCommands(SSJEssentials plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "ban":
                return handleBanPlayer(sender, args);
            case "unban":
                return handleUnban(sender, args);
            case "kick":
                return handleKickPlayer(sender, args);
            case "tempban":
                return handleTempban(sender, args);
            case "banlist":
                if (args.length > 0 && args[0].startsWith("s:")) {
                    return handleBanListSearch(sender, args);
                }
                return handleBanList(sender, args);
            case "/ban":
                if (args.length > 0 && args[0].equals("history")) {
                    if (args.length > 1 && args[1].startsWith("s:")) {
                        return handleBanHistorySearch(sender, args);
                    }
                    return handleBanHistory(sender, args);
                }
                if (args.length > 0 && args[0].startsWith("s:")) {
                    return handleBanListSearch(sender, args);
                }
                return handleBanList(sender, args);
            case "freeze":
                return handleFreeze(sender, args);
            default:
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "kick":
            case "freeze":
                if (args.length == 1) {
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
            case "ban":
            case "tempban":
                if (args.length == 1) {
                    // Suggest player names including offline players
                    List<String> names = new ArrayList<>();
                    
                    // Add online players
                    names.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
                    
                    // Add recently offline players
                    Arrays.stream(Bukkit.getOfflinePlayers())
                        .filter(player -> player.hasPlayedBefore() && !player.isBanned())
                        .limit(50) // Limit to avoid huge lists
                        .forEach(player -> {
                            if (player.getName() != null) {
                                names.add(player.getName());
                            }
                        });
                    
                    return names.stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
                }
                break;
            case "unban":
                if (args.length == 1) {
                    // Suggest banned players
                    List<String> bannedPlayers = new ArrayList<>();
                    
                    // Using raw types to avoid type compatibility issues
                    
                    BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
                    // Use parameterized type to match BanList<PlayerProfile>
                    Set<BanEntry<PlayerProfile>> entries = banList.getEntries();
                    
                    for (BanEntry<PlayerProfile> entry : entries) {
                        PlayerProfile targetProfile = entry.getBanTarget();
                        String targetName = targetProfile.getName();
                        if (targetName != null && targetName.toLowerCase().startsWith(args[0].toLowerCase())) {
                            bannedPlayers.add(targetName);
                        }
                    }
                    
                    return bannedPlayers;
                }
                break;
            case "/ban":
                if (args.length == 1) {
                    List<String> options = new ArrayList<>();
                    if ("history".startsWith(args[0].toLowerCase())) {
                        options.add("history");
                    }
                    if ("s:".startsWith(args[0].toLowerCase())) {
                        options.add("s:");
                    }
                    return options;
                } else if (args.length == 2 && args[0].equals("history")) {
                    if ("s:".startsWith(args[1].toLowerCase())) {
                        return Arrays.asList("s:");
                    }
                }
                break;
            case "banlist":
                if (args.length == 1) {
                    if ("s:".startsWith(args[0].toLowerCase())) {
                        return Arrays.asList("s:");
                    }
                }
                break;
        }
        return new ArrayList<>();
    }

    /**
     * Gets an OfflinePlayer by name more safely than getOfflinePlayer(String)
     * First checks online players, then checks if an offline player with that name has played before
     * 
     * @param name The player name to look for
     * @return The OfflinePlayer if found and has played before, or null
     */
    private OfflinePlayer getSafeOfflinePlayer(String name) {
        // First check online players (most efficient)
        Player onlinePlayer = Bukkit.getPlayerExact(name);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }
        
        // If player not online, check offline players who have played before
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name) && offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer;
            }
        }
        
        // If we can't find the player in our history, return null
        return null;
    }

    private boolean handleBanPlayer(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.ban")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /ban <player> [reason]"));
            return true;
        }

        String targetName = args[0];
        OfflinePlayer target = getSafeOfflinePlayer(targetName);

        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found! They must have connected to the server at least once."));
            return true;
        }

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : plugin.getConfig().getString("ban.default-reason", "No reason specified");

        // Get the player's profile for banning
        PlayerProfile profile = target.getPlayerProfile();
        
        // Use BanList.Type.PROFILE which expects PlayerProfile
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        
        // Add the ban with explicit parameter types to resolve ambiguity
        banList.addBan(profile, reason, (java.util.Date)null, sender.getName());
        
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(reason);
        }

        if (plugin.getConfig().getBoolean("ban.broadcast", true)) {
            Bukkit.broadcastMessage(formatMessage("§c" + targetName + " has been banned by " + 
                sender.getName() + " for: " + reason));
        }

        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.unban")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /unban <player>"));
            return true;
        }

        String targetName = args[0];
        
        // Try to find the player in the known player database
        OfflinePlayer target = getSafeOfflinePlayer(targetName);
        
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found! They must have connected to the server at least once."));
            return true;
        }

        // Get the player's profile for unbanning
        PlayerProfile profile = target.getPlayerProfile();
        
        // Use BanList.Type.PROFILE which expects PlayerProfile
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        
        // Check if banned - use the profile object directly instead of the name
        if (!banList.isBanned(profile)) {
            sender.sendMessage(formatMessage("§cPlayer is not banned!"));
            return true;
        }

        // Record unban history before removal
        String unbannedBy = sender instanceof Player ? sender.getName() : "Console";
        plugin.getBanHistoryManager().recordUnban(profile, unbannedBy);

        // Unban the player - use the profile object directly
        banList.pardon(profile);
        sender.sendMessage(formatMessage("§aUnbanned player: " + targetName));
        
        if (plugin.getConfig().getBoolean("ban.broadcast", true)) {
            Bukkit.broadcastMessage(formatMessage("§a" + targetName + " has been unbanned by " + unbannedBy));
        }
        
        return true;
    }

    private boolean handleKickPlayer(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.kick")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /kick <player> [reason]"));
            return true;
        }

        Player target = getTargetPlayer(sender, args[0]);
        if (target == null) return true;

        String reason = args.length > 1 
            ? String.join(" ", Arrays.copyOfRange(args, 1, args.length))
            : plugin.getConfig().getString("kick.default-reason", "No reason specified");

        target.kickPlayer(reason);

        if (plugin.getConfig().getBoolean("kick.broadcast", true)) {
            Bukkit.broadcastMessage(formatMessage("§c" + target.getName() + 
                " has been kicked by " + sender.getName() + " for: " + reason));
        }

        return true;
    }

    private boolean handleTempban(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.tempban")) {
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(formatMessage("§cUsage: /tempban <player> <duration> [reason]"));
            return true;
        }

        OfflinePlayer target = getSafeOfflinePlayer(args[0]);
        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found! They must have connected to the server at least once."));
            return true;
        }

        String duration = args[1];
        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) 
                                      : plugin.getConfig().getString("tempban.default-reason");

        long durationInMillis = parseDuration(duration);
        if (durationInMillis <= 0) {
            sender.sendMessage(formatMessage("§cInvalid duration format!"));
            return true;
        }

        Date expiry = new Date(System.currentTimeMillis() + durationInMillis);
        
        // Get the player's profile for temp banning
        PlayerProfile profile = target.getPlayerProfile();
        
        // Use BanList.Type.PROFILE which expects PlayerProfile
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        
        // Add the temporary ban with player name
        banList.addBan(profile, reason, expiry, sender.getName());
        
        if (target.isOnline()) {
            target.getPlayer().kickPlayer(reason);
        }
        
        if (plugin.getConfig().getBoolean("tempban.broadcast")) {
            Bukkit.broadcastMessage(formatMessage("§c" + target.getName() + 
                " has been temporarily banned for: " + reason));
        }
        
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean handleBanList(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.banlist")) {
            return true;
        }

        // Use BanList.Type.PROFILE which expects PlayerProfile
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        
        // Create a type-safe set and cast each element individually
        Set<BanEntry<PlayerProfile>> typeSafeBanEntries = new java.util.HashSet<>();
        
        for (Object entry : banList.getEntries()) {
            typeSafeBanEntries.add((BanEntry<PlayerProfile>) entry);
        }
        
        if (typeSafeBanEntries.isEmpty()) {
            sender.sendMessage(formatMessage("§aThere are no banned players."));
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage(formatMessage("§6§lBanned Players List:"));
        sender.sendMessage(formatMessage("§7§m----------------------------------------"));

        for (BanEntry<PlayerProfile> entry : typeSafeBanEntries) {
            
            // Try multiple ways to get the player name
            String playerName = getPlayerNameFromBanEntry(entry);
            
            // Display the ban entry information with proper formatting
            sender.sendMessage(formatMessage("§e" + playerName));
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

    @SuppressWarnings("unchecked")
    private boolean handleBanListSearch(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.banlist")) {
            return true;
        }

        if (args.length < 1 || !args[0].startsWith("s:")) {
            sender.sendMessage(formatMessage("§cUsage: //ban list s:<search term>"));
            return true;
        }

        // Extract search term (remove s: prefix)
        String searchTerm = args[0].substring(2);
        
        if (searchTerm.isEmpty()) {
            sender.sendMessage(formatMessage("§cPlease provide a search term after 's:'"));
            return true;
        }

        // Use BanList.Type.PROFILE which expects PlayerProfile
        BanList<PlayerProfile> banList = Bukkit.getBanList(BanList.Type.PROFILE);
        
        // Create a type-safe set and cast each element individually
        Set<BanEntry<PlayerProfile>> typeSafeBanEntries = new java.util.HashSet<>();
        
        for (Object entry : banList.getEntries()) {
            typeSafeBanEntries.add((BanEntry<PlayerProfile>) entry);
        }
        
        // Filter entries that match the search term
        List<BanEntry<PlayerProfile>> matchingEntries = typeSafeBanEntries.stream()
            .filter(entry -> {
                String playerName = getPlayerNameFromBanEntry(entry);
                return playerName.toLowerCase().contains(searchTerm.toLowerCase());
            })
            .collect(Collectors.toList());
        
        if (matchingEntries.isEmpty()) {
            sender.sendMessage(formatMessage("§cNo banned players found matching: " + searchTerm));
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage(formatMessage("§6§lBanned Players Matching '" + searchTerm + "':"));
        sender.sendMessage(formatMessage("§7§m----------------------------------------"));

        for (BanEntry<PlayerProfile> entry : matchingEntries) {
            // Try multiple ways to get the player name
            String playerName = getPlayerNameFromBanEntry(entry);
            
            // Display the ban entry information with proper formatting
            sender.sendMessage(formatMessage("§e" + playerName));
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

    private boolean handleBanHistory(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.banhistory")) {
            return true;
        }

        List<BanHistoryManager.UnbanHistoryEntry> historyEntries = plugin.getBanHistoryManager().getAllUnbanHistory();
        
        if (historyEntries.isEmpty()) {
            sender.sendMessage(formatMessage("§aThere are no previously unbanned players."));
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage(formatMessage("§6§lUnbanned Players History:"));
        sender.sendMessage(formatMessage("§7§m----------------------------------------"));

        for (BanHistoryManager.UnbanHistoryEntry entry : historyEntries) {
            sender.sendMessage(plugin.getBanHistoryManager().formatUnbanHistoryEntry(entry, dateFormat));
        }
        
        return true;
    }

    private boolean handleBanHistorySearch(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.banhistory")) {
            return true;
        }

        if (args.length < 2 || !args[1].startsWith("s:")) {
            sender.sendMessage(formatMessage("§cUsage: //ban history s:<search term>"));
            return true;
        }

        // Extract search term (remove s: prefix)
        String searchTerm = args[1].substring(2);
        
        if (searchTerm.isEmpty()) {
            sender.sendMessage(formatMessage("§cPlease provide a search term after 's:'"));
            return true;
        }

        List<BanHistoryManager.UnbanHistoryEntry> matchingEntries = plugin.getBanHistoryManager().searchUnbanHistory(searchTerm);
        
        if (matchingEntries.isEmpty()) {
            sender.sendMessage(formatMessage("§cNo unbanned players found matching: " + searchTerm));
            return true;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        sender.sendMessage(formatMessage("§6§lUnbanned Players Matching '" + searchTerm + "':"));
        sender.sendMessage(formatMessage("§7§m----------------------------------------"));

        for (BanHistoryManager.UnbanHistoryEntry entry : matchingEntries) {
            sender.sendMessage(plugin.getBanHistoryManager().formatUnbanHistoryEntry(entry, dateFormat));
        }
        
        return true;
    }

    private boolean handleFreeze(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "ssjessentials.freeze")) {
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(formatMessage("§cUsage: /freeze <player>"));
            return true;
        }

        Player target = getTargetPlayer(sender, args[0]);
        if (target == null) return true;

        // Check for permission to freeze others
        if (sender != target && !hasPermission(sender, "ssjessentials.freeze.others")) {
            return true;
        }

        // Toggle frozen state
        com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(target);
        boolean newFrozenState = !playerData.isFrozen();
        
        playerData.setFrozen(newFrozenState);
        plugin.getConfigs().savePlayerData(target);
        
        // Send messages
        target.sendMessage(newFrozenState ? 
            formatMessage("§cYou have been frozen!") : 
            formatMessage("§aYou have been unfrozen!"));
            
        if (sender != target) {
            sender.sendMessage(newFrozenState ? 
                formatMessage("§aFroze " + target.getName() + "!") : 
                formatMessage("§aUnfroze " + target.getName() + "!"));
        }
        
        return true;
    }

    /**
     * Parses a duration string into milliseconds
     * @param duration The duration string (e.g. "1d", "2h", "30m")
     * @return The duration in milliseconds, or -1 if invalid
     */
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

    /**
     * Tries multiple approaches to extract a player name from a ban entry
     * @param entry The ban entry
     * @return The player name, with fallbacks if not found
     */
    private String getPlayerNameFromBanEntry(BanEntry<PlayerProfile> entry) {
        // Get the profile
        PlayerProfile profile = entry.getBanTarget();
        if (profile == null) {
            plugin.getLogger().warning("Encountered null player profile in ban list");
            return "Unknown (Null Profile)";
        }
        
        // First try the regular getName() method
        String playerName = profile.getName();
        if (playerName != null && !playerName.isEmpty()) {
            return playerName;
        }
        
        // Try getting name from UUID if possible
        UUID uuid = profile.getUniqueId();
        if (uuid != null) {
            // Try to find this player in the offline players
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (uuid.equals(offlinePlayer.getUniqueId()) && offlinePlayer.getName() != null) {
                    return offlinePlayer.getName();
                }
            }
            
            // If we couldn't find the player, at least return the UUID
            return "Player-" + uuid.toString().substring(0, 8);
        }
        
        // Last resort - try to get information from the ban entry itself
        // Some implementations might store the name in a custom property or in the toString()
        String entryString = entry.toString();
        if (entryString.contains("name=") || entryString.contains("player=")) {
            // Try to extract the name from the string representation
            // This is a fallback that may work with some implementations
            try {
                int nameStart = Math.max(entryString.indexOf("name="), entryString.indexOf("player="));
                if (nameStart > 0) {
                    nameStart = entryString.indexOf("=", nameStart) + 1;
                    int nameEnd = entryString.indexOf(",", nameStart);
                    if (nameEnd < 0) nameEnd = entryString.indexOf("}", nameStart);
                    if (nameEnd > nameStart) {
                        String extractedName = entryString.substring(nameStart, nameEnd).trim();
                        // Remove any quotes or braces
                        extractedName = extractedName.replaceAll("[\"'{}\\[\\]]", "");
                        if (!extractedName.isEmpty()) {
                            return extractedName;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore any parsing errors in this fallback approach
            }
        }
        
        // If all else fails, return a generic name
        return "Unknown Player";
    }
} 