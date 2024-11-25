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
    }
} 