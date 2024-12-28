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

public class ServerListPingListener implements Listener {
    private final SSJEssentials plugin;
    private Plugin cachedSSJPlugin;
    private List<WrappedGameProfile> cachedPlayerList;
    private boolean needsUpdate;

    public ServerListPingListener(SSJEssentials plugin) {
        this.plugin = plugin;
        this.needsUpdate = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        registerPingListener();
        updateCache(); // Initial cache update
    }

    private void updateCache() {
        // Create our custom hover lines
        List<String> hoverLines = new ArrayList<>();
        hoverLines.add("§b§lSauS-IT.io §b§l| §e§lSSJPL");
        hoverLines.add("§b§l§m--------------------------------");
        
        // Add online players count
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int maxPlayers = Bukkit.getMaxPlayers();
        hoverLines.add("§7Players Online: §f" + onlinePlayers + "§7/§f" + maxPlayers);
        
        // Check SSJ Plugin status
        cachedSSJPlugin = Bukkit.getPluginManager().getPlugin("SuperSaiyan");
        if (cachedSSJPlugin == null) {
            cachedSSJPlugin = Bukkit.getPluginManager().getPlugin("SuperSaiyan-1");
        }
        String ssjStatus = (cachedSSJPlugin != null && cachedSSJPlugin.isEnabled()) ? "§aWorking" : "§cUpdating";
        hoverLines.add("§7SSJ Plugin: " + ssjStatus);
        
        hoverLines.add("§b§l§m--------------------------------");

        // Convert hover lines to player samples
        cachedPlayerList = new ArrayList<>();
        for (String line : hoverLines) {
            cachedPlayerList.add(new WrappedGameProfile(UUID.randomUUID(), line));
        }

        needsUpdate = false;
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
        ProtocolLibrary.getProtocolManager().addPacketListener(
            new PacketAdapter(plugin, PacketType.Status.Server.SERVER_INFO) {
                @Override
                public void onPacketSending(PacketEvent event) {
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
                }
            }
        );
    }

    public void unregister() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }

    public void forceUpdate() {
        needsUpdate = true;
    }
} 