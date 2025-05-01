package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public class TabListManager {
    private final SSJEssentials plugin;
    private BukkitTask updateTask;
    private String header;
    private String footer;
    private boolean enabled;
    private int updateInterval;
    private Plugin cachedSSJPlugin;
    private long lastPluginCheck;
    private static final long PLUGIN_CHECK_INTERVAL = 5000; // Check every 5 seconds
    private static final Set<String> POSSIBLE_SSJ_NAMES = Set.of(
        "SuperSaiyan",
        "SuperSaiyan-1",
        "SuperSaiyan1",
        "SuperSaiyan (1)"
    );
    private static final int MIN_UPDATE_INTERVAL = 1;

    public TabListManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.lastPluginCheck = 0;
        loadConfig();
        if (enabled) {
            startUpdateTask();
        }
    }

    public void loadConfig() {
        try {
            enabled = plugin.getConfigs().getBoolean("tablist.enabled", true);
            header = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigs().getString("tablist.header", "&b&lWelcome to &e&lmc.saus-it.io &b&l| &e&lSSJPL"));
            footer = ChatColor.translateAlternateColorCodes('&', 
                plugin.getConfigs().getString("tablist.footer", "&7Online Players: &f%online%/%max%"));
            
            // Validate update interval
            updateInterval = Math.max(MIN_UPDATE_INTERVAL, 
                plugin.getConfigs().getInt("tablist.update-interval", 20));
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load TabList configuration: " + e.getMessage());
            // Set default values
            enabled = true;
            header = "&b&lWelcome to &e&lmc.saus-it.io &b&l| &e&lSSJPL";
            footer = "&7Online Players: &f%online%/%max%";
            updateInterval = 20;
        }
    }

    private Plugin checkSSJPlugin() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPluginCheck > PLUGIN_CHECK_INTERVAL) {
            for (String name : POSSIBLE_SSJ_NAMES) {
                Plugin found = Bukkit.getPluginManager().getPlugin(name);
                if (found != null) {
                    cachedSSJPlugin = found;
                    break;
                }
            }
            lastPluginCheck = currentTime;
        }
        return cachedSSJPlugin;
    }

    private String processFooter() {
        String processedFooter = footer
            .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));

        Plugin ssjPlugin = checkSSJPlugin();
        String ssjStatus = (ssjPlugin != null && ssjPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
        return processedFooter + " §8| §7SSJ Plugin: " + ssjStatus;
    }

    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String processedFooter = processFooter();
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    player.setPlayerListHeader(header);
                    player.setPlayerListFooter(processedFooter);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to update tablist for player " + 
                        player.getName() + ": " + e.getMessage());
                }
            }
        }, 0L, updateInterval);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            startUpdateTask();
        } else if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            // Clear tab list for all players
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    player.setPlayerListHeader("");
                    player.setPlayerListFooter("");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to clear tablist for player " + 
                        player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    public void updatePlayer(Player player) {
        if (!enabled) return;
        
        try {
            player.setPlayerListHeader(header);
            player.setPlayerListFooter(processFooter());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update tablist for player " + 
                player.getName() + ": " + e.getMessage());
        }
    }

    public void setHeader(String header) {
        this.header = ChatColor.translateAlternateColorCodes('&', header);
        plugin.getConfigs().set("tablist.header", header);
        plugin.getConfigs().saveConfigs();
        if (enabled) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayer(player);
            }
        }
    }

    public void setFooter(String footer) {
        this.footer = ChatColor.translateAlternateColorCodes('&', footer);
        plugin.getConfigs().set("tablist.footer", footer);
        plugin.getConfigs().saveConfigs();
        if (enabled) {
            String processedFooter = processFooter();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListFooter(processedFooter);
            }
        }
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
    }
} 