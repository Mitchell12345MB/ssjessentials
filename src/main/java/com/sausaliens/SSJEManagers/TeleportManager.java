package com.sausaliens.SSJEManagers;

import org.bukkit.entity.Player;
import java.util.*;
import com.sausaliens.SSJEssentials;

public class TeleportManager {
    private final Map<UUID, UUID> teleportRequests = new HashMap<>();
    private final Map<UUID, Long> requestTimestamps = new HashMap<>();
    private final long requestTimeout;

    @SuppressWarnings("unused")
    private final SSJEssentials ssjEssentials;

    public TeleportManager(SSJEssentials ssjEssentials) {
        this.ssjEssentials = ssjEssentials;
        this.requestTimeout = ssjEssentials.getConfig().getLong("teleport.request-timeout", 60000);
    }

    public void createRequest(Player requester, Player target) {
        teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
        requestTimestamps.put(target.getUniqueId(), System.currentTimeMillis());
    }

    public boolean hasActiveRequest(Player target) {
        UUID targetUUID = target.getUniqueId();
        if (!teleportRequests.containsKey(targetUUID)) return false;
        
        long timestamp = requestTimestamps.get(targetUUID);
        if (System.currentTimeMillis() - timestamp > requestTimeout) {
            teleportRequests.remove(targetUUID);
            requestTimestamps.remove(targetUUID);
            return false;
        }
        return true;
    }

    public Player getRequester(Player target) {
        return target.getServer().getPlayer(teleportRequests.get(target.getUniqueId()));
    }

    public void removeRequest(Player target) {
        teleportRequests.remove(target.getUniqueId());
        requestTimestamps.remove(target.getUniqueId());
    }
} 