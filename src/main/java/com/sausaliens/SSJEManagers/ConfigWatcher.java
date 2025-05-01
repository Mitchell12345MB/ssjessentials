package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigWatcher {
    private final SSJEssentials plugin;
    private final Map<File, Long> fileLastModified;
    private final File[] watchedFiles;
    private boolean reloadInProgress = false;
    private long lastReloadTime = 0;
    private static final long RELOAD_COOLDOWN = 5000; // 5 seconds cooldown between reloads

    public ConfigWatcher(SSJEssentials plugin) {
        this.plugin = plugin;
        this.fileLastModified = new HashMap<>();
        
        // List of config files to watch
        this.watchedFiles = new File[]{
            new File(plugin.getDataFolder(), "config.yml"),
            new File(plugin.getDataFolder(), "groups.yml"),
            new File(plugin.getDataFolder(), "spawn.yml"),
            new File(plugin.getDataFolder(), "automessages.yml")
        };

        // Initialize last modified times
        for (File file : watchedFiles) {
            if (file.exists()) {
                fileLastModified.put(file, file.lastModified());
            }
        }

        // Start the watcher task
        startWatcher();
    }

    private void startWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForChanges();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    private void checkForChanges() {
        // Skip if a reload is in progress or we're in the cooldown period
        if (reloadInProgress || System.currentTimeMillis() - lastReloadTime < RELOAD_COOLDOWN) {
            return;
        }

        boolean needsReload = false;
        Map<File, Long> newModifiedTimes = new HashMap<>();

        for (File file : watchedFiles) {
            if (!file.exists()) {
                continue;
            }

            Long lastModified = fileLastModified.get(file);
            long currentModified = file.lastModified();

            // Store current timestamps for update after reload
            newModifiedTimes.put(file, currentModified);

            if (lastModified == null || currentModified > lastModified) {
                needsReload = true;
                plugin.getLogger().info("Detected changes in " + file.getName());
            }
        }

        if (needsReload) {
            try {
                reloadInProgress = true;
                plugin.getLogger().info("Reloading configuration due to file changes...");
                plugin.reloadConfig();
                
                // Update all timestamps after successful reload
                fileLastModified.clear();
                for (File file : watchedFiles) {
                    if (file.exists()) {
                        // Use the NEW timestamps collected before reload
                        fileLastModified.put(file, file.lastModified());
                    }
                }
                
                lastReloadTime = System.currentTimeMillis();
                plugin.getLogger().info("Configuration reloaded successfully!");
            } catch (Exception e) {
                plugin.getLogger().warning("Error reloading configuration: " + e.getMessage());
            } finally {
                reloadInProgress = false;
            }
        }
    }
    
    /**
     * Updates the stored last modified time for all watched files.
     * Call this after any operation that might modify config files.
     */
    public void updateFileTimestamps() {
        for (File file : watchedFiles) {
            if (file.exists()) {
                fileLastModified.put(file, file.lastModified());
            }
        }
    }
} 