package com.sausaliens.SSJEListeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import com.sausaliens.SSJEssentials;

import org.bukkit.plugin.Plugin;

public class PlayerFlightListener implements Listener {
    private final SSJEssentials plugin;

    public PlayerFlightListener(SSJEssentials plugin) {
        this.plugin = plugin;
    }

    private boolean isSuperSaiyanFlight(Player player) {
        Plugin superSaiyan = plugin.getServer().getPluginManager().getPlugin("SuperSaiyan");
        if (superSaiyan != null && superSaiyan.isEnabled()) {
            try {
                Object ssjPCM = superSaiyan.getClass().getMethod("getSSJPCM").invoke(superSaiyan);
                return (boolean) ssjPCM.getClass().getMethod("hasSkill", Player.class, String.class)
                    .invoke(ssjPCM, player, "Fly");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check SuperSaiyan flight status: " + e.getMessage());
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // If player has SSJ flight skill and SSJ flight is active, don't interfere
        if (isSuperSaiyanFlight(player)) {
            return;
        }
        
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in flight toggle");
                return;
            }
            if (!playerData.isFlying()) {
                event.setCancelled(true);
                player.setAllowFlight(false);
                player.setFlying(false);
            } else {
                event.setCancelled(false);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode gameMode = event.getNewGameMode();
        
        if (gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR) {
            return;
        }
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in gamemode change");
                return;
            }
            if (playerData.isFlying()) {
                player.setAllowFlight(true);
            } else {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getConfigs().getPlayerDataAsync(player).thenAccept(playerData -> {
            if (playerData == null) {
                plugin.getLogger().warning("PlayerData is null for " + player.getName() + " in join event");
                return;
            }
            // Restore gamemode
            player.setGameMode(playerData.getGameMode());
            // Restore flight status
            if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
                if (playerData.isFlying()) {
                    player.setAllowFlight(true);
                } else {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
            }
        });
    }
}