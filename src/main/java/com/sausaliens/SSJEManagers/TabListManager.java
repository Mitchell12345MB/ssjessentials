package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

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

    public TabListManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.lastPluginCheck = 0;
        loadConfig();
        if (enabled) {
            startUpdateTask();
        }
    }

    public void loadConfig() {
        enabled = plugin.getConfig().getBoolean("tablist.enabled", true);
        header = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("tablist.header", "&b&lWelcome to &e&lmc.saus-it.io &b&l| &e&lSSJPL"));
        footer = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("tablist.footer", "&7Online Players: &f%online%/%max%"));
        updateInterval = plugin.getConfig().getInt("tablist.update-interval", 20);
    }

    private Plugin checkSSJPlugin() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPluginCheck > PLUGIN_CHECK_INTERVAL) {
            String[] possibleNames = {
                "SuperSaiyan",
                "SuperSaiyan-1",
                "SuperSaiyan1",
                "SuperSaiyan (1)"
            };

            for (String name : possibleNames) {
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

    private void startUpdateTask() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String processedFooter = footer
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));

            Plugin ssjPlugin = checkSSJPlugin();
            String ssjStatus = (ssjPlugin != null && ssjPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
            processedFooter += " §8| §7SSJ Plugin: " + ssjStatus;

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListHeader(header);
                player.setPlayerListFooter(processedFooter);
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
                player.setPlayerListHeader("");
                player.setPlayerListFooter("");
            }
        }
    }

    public void updatePlayer(Player player) {
        if (!enabled) return;
        
        String processedFooter = footer
            .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
            .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));

        Plugin ssjPlugin = checkSSJPlugin();
        String ssjStatus = (ssjPlugin != null && ssjPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
        processedFooter += " §8| §7SSJ Plugin: " + ssjStatus;

        player.setPlayerListHeader(header);
        player.setPlayerListFooter(processedFooter);
    }

    public void setHeader(String header) {
        this.header = ChatColor.translateAlternateColorCodes('&', header);
        plugin.getConfig().set("tablist.header", header);
        plugin.saveConfig();
        if (enabled) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.setPlayerListHeader(this.header);
            }
        }
    }

    public void setFooter(String footer) {
        this.footer = ChatColor.translateAlternateColorCodes('&', footer);
        plugin.getConfig().set("tablist.footer", footer);
        plugin.saveConfig();
        if (enabled) {
            String processedFooter = this.footer
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max%", String.valueOf(Bukkit.getMaxPlayers()));

            Plugin ssjPlugin = checkSSJPlugin();
            String ssjStatus = (ssjPlugin != null && ssjPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
            processedFooter += " §8| §7SSJ Plugin: " + ssjStatus;

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