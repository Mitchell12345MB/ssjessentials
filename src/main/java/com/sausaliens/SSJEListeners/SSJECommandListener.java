package com.sausaliens.SSJEListeners;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.sausaliens.SSJEssentials;

public class SSJECommandListener implements Listener {
    private final SSJEssentials plugin;

    public SSJECommandListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        
        if (message.equals("//ban list")) {
            event.setCancelled(true);
            PluginCommand cmd = plugin.getCommand("banlist");
            if (cmd != null) {
                plugin.getModerationCommands().onCommand(event.getPlayer(), cmd, "banlist", new String[0]);
            }
        }
        
        // Ban history command
        if (message.equals("//ban history")) {
            event.setCancelled(true);
            PluginCommand cmd = plugin.getCommand("/ban");
            if (cmd != null) {
                plugin.getModerationCommands().onCommand(event.getPlayer(), cmd, "/ban", new String[]{"history"});
            }
        }
        
        // Ban history search command
        if (message.startsWith("//ban history s:")) {
            event.setCancelled(true);
            String searchTerm = message.substring("//ban history s:".length());
            PluginCommand cmd = plugin.getCommand("/ban");
            if (cmd != null) {
                plugin.getModerationCommands().onCommand(event.getPlayer(), cmd, "/ban", new String[]{"history", "s:" + searchTerm});
            }
        }
        
        // Ban list search command
        if (message.startsWith("//ban list s:")) {
            event.setCancelled(true);
            String searchTerm = message.substring("//ban list s:".length());
            PluginCommand cmd = plugin.getCommand("banlist");
            if (cmd != null) {
                plugin.getModerationCommands().onCommand(event.getPlayer(), cmd, "banlist", new String[]{"s:" + searchTerm});
            }
        }

        // home list
        if (message.equals("//home list")) {
            event.setCancelled(true);
            PluginCommand cmd = plugin.getCommand("/home");
            if (cmd != null) {
                plugin.getLocationCommands().onCommand(event.getPlayer(), cmd, "/home", new String[0]);
            }
        }

        // warp list
        if (message.equals("//warp list")) {
            event.setCancelled(true);
            PluginCommand cmd = plugin.getCommand("/warp");
            if (cmd != null) {
                plugin.getLocationCommands().onCommand(event.getPlayer(), cmd, "/warp", new String[0]);
            }
        }

        // nick none
        if (message.startsWith("//nick none")) {
            event.setCancelled(true);
            String[] args = message.substring(2).trim().split("\\s+");
            plugin.getNicknameCommands().handleNick(event.getPlayer(), args);
        }
    }
} 