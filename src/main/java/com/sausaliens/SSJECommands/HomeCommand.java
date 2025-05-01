package com.sausaliens.SSJECommands;

// import com.sausaliens.SSJEPlayerData.PlayerData; // Removed legacy PlayerData import
import com.sausaliens.SSJEssentials;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final SSJEssentials plugin;

    public HomeCommand(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command!"));
            return true;
        }

        final Player player = (Player) sender;
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in home command");
                player.sendMessage(plugin.formatMessage("&cCould not load your player data. Try again later."));
                return;
            }
            // Handle different command variations
            if (label.equalsIgnoreCase("sethome")) {
                handleSetHome(player, playerData, args);
            } else if (label.equalsIgnoreCase("delhome")) {
                handleDeleteHome(player, playerData, args);
            } else if (label.equalsIgnoreCase("home")) {
                handleHome(player, playerData, args);
            } else if (label.equalsIgnoreCase("homes")) {
                handleListHomes(player, playerData);
            }
        });
        return true;
    }

    /**
     * Handle the sethome command
     * @param player The player
     * @param playerData The player's data
     * @param args Command arguments
     * @return true if the command was successful
     */
    private boolean handleSetHome(Player player, com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData, String[] args) {
        String homeName = "home";
        
        if (args.length > 0) {
            homeName = args[0].toLowerCase();
        }
        
        // Check if player has permission for multiple homes
        int maxHomes = getMaxHomes(player);
        if (playerData.getHomes().size() >= maxHomes && !playerData.getHomes().containsKey(homeName)) {
            player.sendMessage(plugin.formatMessage("&cYou have reached your maximum number of homes (&e" + maxHomes + "&c)!"));
            return true;
        }
        
        // Set home
        playerData.setHome(homeName, player.getLocation());
        player.sendMessage(plugin.formatMessage("&aHome &e" + homeName + " &aset!"));
        
        return true;
    }
    
    /**
     * Handle the delhome command
     * @param player The player
     * @param playerData The player's data
     * @param args Command arguments
     * @return true if the command was successful
     */
    private boolean handleDeleteHome(Player player, com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData, String[] args) {
        if (args.length == 0) {
            player.sendMessage(plugin.formatMessage("&cUsage: /delhome <name>"));
            return true;
        }
        
        String homeName = args[0].toLowerCase();
        
        if (!playerData.deleteHome(homeName)) {
            player.sendMessage(plugin.formatMessage("&cYou don't have a home named &e" + homeName + "&c!"));
            return true;
        }
        
        player.sendMessage(plugin.formatMessage("&aHome &e" + homeName + " &adeleted!"));
        return true;
    }
    
    /**
     * Handle the home command
     * @param player The player
     * @param playerData The player's data
     * @param args Command arguments
     * @return true if the command was successful
     */
    private boolean handleHome(Player player, com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData, String[] args) {
        String homeName = "home";
        
        if (args.length > 0) {
            homeName = args[0].toLowerCase();
        }
        
        // Check if home exists
        Location home = playerData.getHome(homeName);
        if (home == null) {
            player.sendMessage(plugin.formatMessage("&cYou don't have a home named &e" + homeName + "&c!"));
            return true;
        }
        
        // Check cooldown
        int cooldown = plugin.getConfig().getInt("teleport.cooldowns.home", 60);
        if (playerData.hasTeleportCooldown(cooldown)) {
            int remaining = playerData.getRemainingCooldown(cooldown);
            player.sendMessage(plugin.formatMessage("&cYou must wait &e" + remaining + " &cseconds before teleporting again!"));
            return true;
        }
        
        // Teleport player
        plugin.getTeleportManager().teleport(player, home, "home");
        return true;
    }
    
    /**
     * Handle the homes command
     * @param player The player
     * @param playerData The player's data
     * @return true if the command was successful
     */
    private boolean handleListHomes(Player player, com.sausaliens.SSJEConfig.SSJConfigs.PlayerData playerData) {
        if (playerData.getHomes().isEmpty()) {
            player.sendMessage(plugin.formatMessage("&cYou don't have any homes!"));
            return true;
        }
        
        player.sendMessage(plugin.formatMessage("&aYour homes:"));
        for (String homeName : playerData.getHomes().keySet()) {
            Location home = playerData.getHome(homeName);
            player.sendMessage(plugin.formatMessage("&e" + homeName + " &7- &f" + 
                    home.getWorld().getName() + " (" + 
                    home.getBlockX() + ", " + 
                    home.getBlockY() + ", " + 
                    home.getBlockZ() + ")"));
        }
        
        return true;
    }
    
    /**
     * Get the maximum number of homes a player can have
     * @param player The player
     * @return The maximum number of homes
     */
    private int getMaxHomes(Player player) {
        // Default max homes is 1
        int maxHomes = 1;
        
        // Check for higher permissions
        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("ssjessentials.homes." + i)) {
                maxHomes = i;
                break;
            }
        }
        
        return maxHomes;
    }
} 