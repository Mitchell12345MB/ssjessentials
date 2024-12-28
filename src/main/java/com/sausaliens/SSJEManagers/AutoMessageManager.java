package com.sausaliens.SSJEManagers;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoMessageManager {
    private final SSJEssentials plugin;
    private final File configFile;
    private FileConfiguration config;
    private final List<String> messages;
    private int currentIndex;
    private BukkitTask messageTask;
    private final Random random;
    private boolean wasServerEmpty;
    private boolean runWhileEmpty;
    private boolean displayOnUnEmpty;
    private long lastMessageTime;

    public AutoMessageManager(SSJEssentials plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "automessages.yml");
        this.messages = new ArrayList<>();
        this.currentIndex = 0;
        this.random = new Random();
        this.wasServerEmpty = Bukkit.getOnlinePlayers().isEmpty();
        this.lastMessageTime = 0;
        loadConfig();
        startMessageTask();
    }

    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("automessages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        runWhileEmpty = config.getBoolean("run_while_server_is_empty", false);
        displayOnUnEmpty = config.getBoolean("display_message_when_server_becomes_un_empty", true);
        reloadMessages();
    }

    public void reloadMessages() {
        messages.clear();
        messages.addAll(config.getStringList("messages"));
        restartMessageTask();
    }

    private void startMessageTask() {
        int interval = config.getInt("interval", 300) * 1000; // Convert seconds directly to milliseconds
        boolean randomize = config.getBoolean("random", true);

        if (messageTask != null) {
            messageTask.cancel();
        }

        messageTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            boolean isServerEmpty = Bukkit.getOnlinePlayers().isEmpty();
            long currentTime = System.currentTimeMillis();
            
            // Handle server becoming un-empty
            if (wasServerEmpty && !isServerEmpty && displayOnUnEmpty && !messages.isEmpty()) {
                // Only send a message if we haven't sent one recently (within last 5 seconds)
                if (currentTime - lastMessageTime > 5000) {
                    if (randomize) {
                        broadcastMessage(messages.get(random.nextInt(messages.size())));
                    } else {
                        broadcastMessage(messages.get(currentIndex));
                        currentIndex = (currentIndex + 1) % messages.size();
                    }
                    lastMessageTime = currentTime;
                }
            }
            
            // Update server empty status
            wasServerEmpty = isServerEmpty;
            
            // Don't broadcast if server is empty and runWhileEmpty is false
            if (isServerEmpty && !runWhileEmpty) {
                return;
            }

            // Only send message if enough time has passed since last message
            if (currentTime - lastMessageTime >= interval) { // Now using milliseconds directly
                if (!messages.isEmpty()) {
                    if (randomize) {
                        broadcastMessage(messages.get(random.nextInt(messages.size())));
                    } else {
                        broadcastMessage(messages.get(currentIndex));
                        currentIndex = (currentIndex + 1) % messages.size();
                    }
                    lastMessageTime = currentTime;
                }
            }
        }, 0L, 20L); // Check every second
    }

    public void restartMessageTask() {
        if (messageTask != null) {
            messageTask.cancel();
        }
        startMessageTask();
    }

    private void broadcastMessage(String message) {
        // Get the prefix from config and translate color codes
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] "));
            
        // Replace color codes and new lines
        message = ChatColor.translateAlternateColorCodes('&', message.replace("\\n", "\n"));
        
        // Add prefix to each line of the message
        String[] lines = message.split("\n");
        for (String line : lines) {
            Bukkit.broadcastMessage(prefix + line);
        }
    }

    public boolean addMessage(String message) {
        messages.add(message);
        config.set("messages", messages);
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto message: " + e.getMessage());
            return false;
        }
    }

    public boolean removeMessage(int index) {
        if (index < 0 || index >= messages.size()) {
            return false;
        }
        messages.remove(index);
        config.set("messages", messages);
        try {
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto messages: " + e.getMessage());
            return false;
        }
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void setInterval(int seconds) {
        config.set("interval", seconds);
        try {
            config.save(configFile);
            restartMessageTask();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto message interval: " + e.getMessage());
        }
    }

    public void setRandom(boolean random) {
        config.set("random", random);
        try {
            config.save(configFile);
            restartMessageTask();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto message random setting: " + e.getMessage());
        }
    }

    public void setRunWhileEmpty(boolean runWhileEmpty) {
        this.runWhileEmpty = runWhileEmpty;
        config.set("run_while_server_is_empty", runWhileEmpty);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto message empty server setting: " + e.getMessage());
        }
    }

    public void setDisplayOnUnEmpty(boolean displayOnUnEmpty) {
        this.displayOnUnEmpty = displayOnUnEmpty;
        config.set("display_message_when_server_becomes_un_empty", displayOnUnEmpty);
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save auto message un-empty server setting: " + e.getMessage());
        }
    }
} 