package com.sausaliens.SSJEListeners;

import com.sausaliens.SSJEssentials;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarpSignListener implements Listener {
    private final SSJEssentials plugin;
    private final Pattern warpPattern = Pattern.compile("\\[(.*?)\\]");

    public WarpSignListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String firstLine = event.getLine(0);

        if (firstLine == null) return;

        // Check if the first line contains [warpname]
        Matcher matcher = warpPattern.matcher(firstLine);
        if (matcher.find()) {
            // Check permission
            if (!player.hasPermission("ssjessentials.warpsign")) {
                player.sendMessage(formatMessage("§cYou don't have permission to create warp signs!"));
                event.setCancelled(true);
                return;
            }

            String warpName = matcher.group(1);
            Location warpLocation = plugin.getLocationManager().getWarp(warpName);

            if (warpLocation == null) {
                player.sendMessage(formatMessage("§cWarp '" + warpName + "' does not exist!"));
                event.setCancelled(true);
                return;
            }

            // Format the sign
            event.setLine(0, "§1§l[Warp]");
            event.setLine(1, "§9" + warpName);
            event.setLine(2, "§8Click to");
            event.setLine(3, "§8teleport");

            player.sendMessage(formatMessage("§aWarp sign created for '" + warpName + "'!"));
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String firstLine = ChatColor.stripColor(sign.getLine(0));
        String secondLine = ChatColor.stripColor(sign.getLine(1));

        if (firstLine.equals("[Warp]")) {
            event.setCancelled(true);
            String warpName = secondLine;
            Location warpLocation = plugin.getLocationManager().getWarp(warpName);

            if (warpLocation == null) {
                // Warp doesn't exist anymore, break the sign and drop it
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.OAK_SIGN));
                return;
            }

            Player player = event.getPlayer();
            if (!player.hasPermission("ssjessentials.warp")) {
                player.sendMessage(formatMessage("§cYou don't have permission to use warps!"));
                return;
            }

            // Teleport the player
            player.teleport(warpLocation);
            player.sendMessage(formatMessage("§aTeleported to warp: " + warpName));
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof Sign)) return;

        Sign sign = (Sign) block.getState();
        String firstLine = ChatColor.stripColor(sign.getLine(0));

        if (firstLine.equals("[Warp]")) {
            Player player = event.getPlayer();
            if (!player.hasPermission("ssjessentials.warpsign")) {
                player.sendMessage(formatMessage("§cYou don't have permission to break warp signs!"));
                event.setCancelled(true);
            }
        }
    }

    private String formatMessage(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] "));
        return prefix + message;
    }
} 