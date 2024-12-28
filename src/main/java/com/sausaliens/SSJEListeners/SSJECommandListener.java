package com.sausaliens.SSJEListeners;

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
            plugin.getCommandExecutor().handleBanCommand(event.getPlayer(), new String[0]);
        }

        // home list
        if (message.equals("//home list")) {
            event.setCancelled(true);
            plugin.getCommandExecutor().handleHomeList(event.getPlayer());
        }

        // warp list
        if (message.equals("//warp list")) {
            event.setCancelled(true);
            plugin.getCommandExecutor().handleWarpList(event.getPlayer());
        }

        // nick none
        if (message.startsWith("//nick none")) {
            event.setCancelled(true);
            String[] args = message.substring(2).trim().split("\\s+");
            plugin.getCommandExecutor().handleNick(event.getPlayer(), args);
        }
    }
} 