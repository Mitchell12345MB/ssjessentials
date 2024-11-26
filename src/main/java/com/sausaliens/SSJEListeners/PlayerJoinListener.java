package com.sausaliens.SSJEListeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.sausaliens.SSJEssentials;
import com.sausaliens.SSJEConfig.SSJConfigs;

public class PlayerJoinListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerJoinListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SSJConfigs.PlayerData playerData = plugin.getConfigs().getPlayerData(event.getPlayer());
        if (playerData.getNickname() != null) {
            event.getPlayer().setDisplayName(playerData.getNickname());
            event.getPlayer().setPlayerListName(playerData.getNickname());
        }
    }
} 