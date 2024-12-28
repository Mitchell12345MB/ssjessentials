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
        boolean needsReload = false;

        for (File file : watchedFiles) {
            if (!file.exists()) {
                continue;
            }

            Long lastModified = fileLastModified.get(file);
            long currentModified = file.lastModified();

            if (lastModified == null || currentModified > lastModified) {
                needsReload = true;
                fileLastModified.put(file, currentModified);
                plugin.getLogger().info("Detected changes in " + file.getName());
            }
        }

        if (needsReload) {
            try {
                plugin.getLogger().info("Reloading configuration due to file changes...");
                plugin.reloadConfig();
                plugin.getLogger().info("Configuration reloaded successfully!");
            } catch (Exception e) {
                plugin.getLogger().warning("Error reloading configuration: " + e.getMessage());
            }
        }
    }
} 