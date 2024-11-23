package com.sausaliens.SSJEPlayerData;

import java.util.UUID;

public class SSJEPlayerData {
    private final UUID playerUUID;
    private boolean isFlying;

    public SSJEPlayerData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.isFlying = false; // default value
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    // Optional: Add methods to save and load data if persistence is needed
}
