package com.sausaliens.SSJEListeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

public class ServerListPingListener implements Listener {
    private final SSJEssentials plugin;
    private Plugin cachedSSJPlugin;
    private List<WrappedGameProfile> cachedPlayerList;
    private boolean needsUpdate;
    private static final Set<String> POSSIBLE_SSJ_NAMES = Set.of(
        "SuperSaiyan",
        "SuperSaiyan-1",
        "SuperSaiyan1",
        "SuperSaiyan (1)"
    );
    private static final String DEFAULT_HEADER = "§b§lSauS-IT.io §b§l| §e§lSSJPL";
    private static final String DIVIDER = "§b§l§m--------------------------------";
    private long lastPluginCheck;
    private static final long PLUGIN_CHECK_INTERVAL = 5000; // Check every 5 seconds

    public ServerListPingListener(SSJEssentials plugin) {
        this.plugin = plugin;
        this.needsUpdate = true;
        this.lastPluginCheck = 0;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerPingListener();
        updateCache(); // Initial cache update
    }

    private void updateCache() {
        try {
            // Create our custom hover lines
            List<String> hoverLines = new ArrayList<>();
            hoverLines.add(plugin.getConfig().getString("serverlist.header", DEFAULT_HEADER));
            hoverLines.add(DIVIDER);
            
            // Add online players count
            int onlinePlayers = Bukkit.getOnlinePlayers().size();
            int maxPlayers = Bukkit.getMaxPlayers();
            hoverLines.add("§7Players Online: §f" + onlinePlayers + "§7/§f" + maxPlayers);
            
            // Check SSJ Plugin status
            checkAndUpdateSSJPlugin();
            String ssjStatus = (cachedSSJPlugin != null && cachedSSJPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
            hoverLines.add("§7SSJ Plugin: " + ssjStatus);
            
            hoverLines.add(DIVIDER);

            // Convert hover lines to player samples
            cachedPlayerList = new ArrayList<>();
            for (String line : hoverLines) {
                cachedPlayerList.add(new WrappedGameProfile(UUID.randomUUID(), line));
            }

            needsUpdate = false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update server list cache: " + e.getMessage());
            // Create a basic fallback cache if update fails
            createFallbackCache();
        }
    }

    private void createFallbackCache() {
        List<String> fallbackLines = new ArrayList<>();
        fallbackLines.add(DEFAULT_HEADER);
        fallbackLines.add(DIVIDER);
        fallbackLines.add("§7Server Information Unavailable");
        fallbackLines.add(DIVIDER);

        cachedPlayerList = new ArrayList<>();
        for (String line : fallbackLines) {
            cachedPlayerList.add(new WrappedGameProfile(UUID.randomUUID(), line));
        }
    }

    private void checkAndUpdateSSJPlugin() {
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
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        needsUpdate = true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        needsUpdate = true;
    }

    private void registerPingListener() {
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, PacketType.Status.Server.SERVER_INFO) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        try {
                            if (needsUpdate) {
                                updateCache();
                            }

                            PacketContainer packet = event.getPacket();
                            WrappedServerPing ping = packet.getServerPings().read(0);

                            // Set the cached player samples
                            ping.setPlayers(cachedPlayerList);
                            
                            // Update player count
                            ping.setPlayersOnline(Bukkit.getOnlinePlayers().size());
                            ping.setPlayersMaximum(Bukkit.getMaxPlayers());

                            // Write the modified ping back to the packet
                            packet.getServerPings().write(0, ping);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error processing server ping packet: " + e.getMessage());
                        }
                    }
                }
            );
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register ping listener: " + e.getMessage());
        }
    }

    public void unregister() {
        try {
            ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
        } catch (Exception e) {
            plugin.getLogger().warning("Error unregistering ping listener: " + e.getMessage());
        }
    }

    public void forceUpdate() {
        needsUpdate = true;
    }
} 