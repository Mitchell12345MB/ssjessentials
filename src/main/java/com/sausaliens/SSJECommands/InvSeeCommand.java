package com.sausaliens.SSJECommands;

import com.sausaliens.SSJEssentials;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.UUID;

public class InvSeeCommand implements CommandExecutor, Listener {
    private final SSJEssentials plugin;
    private final HashMap<UUID, UUID> openInventories; // Viewer UUID -> Target UUID

    public InvSeeCommand(SSJEssentials plugin) {
        this.plugin = plugin;
        this.openInventories = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(formatMessage("§cThis command can only be used by players!"));
            return true;
        }

        if (!sender.hasPermission("ssjessentials.invsee")) {
            sender.sendMessage(formatMessage("§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(formatMessage("§cUsage: /invsee <player>"));
            return true;
        }

        Player viewer = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(formatMessage("§cPlayer not found!"));
            return true;
        }

        if (target == viewer) {
            sender.sendMessage(formatMessage("§cYou cannot view your own inventory!"));
            return true;
        }

        // Create inventory with enough space for main inventory (36) + armor (4) + off hand (1)
        Inventory inv = Bukkit.createInventory(null, 45, "§8" + target.getName() + "'s Inventory");
        
        // Copy main inventory contents (0-35)
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36 && i < contents.length; i++) {
            inv.setItem(i, contents[i]);
        }

        // Copy armor contents (36-39)
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            inv.setItem(36 + i, armor[i]);
        }

        // Copy off hand (40)
        inv.setItem(40, target.getInventory().getItemInOffHand());

        // Store the viewer-target relationship
        openInventories.put(viewer.getUniqueId(), target.getUniqueId());

        // Open the inventory for the viewer
        viewer.openInventory(inv);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) event.getWhoClicked();
        
        // Check if this is an invsee inventory
        if (!openInventories.containsKey(viewer.getUniqueId())) return;

        // Get the target player
        Player target = Bukkit.getPlayer(openInventories.get(viewer.getUniqueId()));
        if (target == null) {
            event.setCancelled(true);
            viewer.closeInventory();
            return;
        }

        // Update the target's inventory
        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                PlayerInventory targetInv = target.getInventory();
                Inventory viewerInv = event.getView().getTopInventory();

                // Update main inventory (0-35)
                for (int i = 0; i < 36; i++) {
                    targetInv.setItem(i, viewerInv.getItem(i));
                }

                // Update armor (36-39)
                ItemStack[] armor = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    armor[i] = viewerInv.getItem(36 + i);
                }
                targetInv.setArmorContents(armor);

                // Update off hand (40)
                targetInv.setItemInOffHand(viewerInv.getItem(40));

                target.updateInventory();
            });
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) event.getWhoClicked();
        
        // Check if this is an invsee inventory
        if (!openInventories.containsKey(viewer.getUniqueId())) return;

        // Get the target player
        Player target = Bukkit.getPlayer(openInventories.get(viewer.getUniqueId()));
        if (target == null) {
            event.setCancelled(true);
            viewer.closeInventory();
            return;
        }

        // Update the target's inventory
        Bukkit.getScheduler().runTask(plugin, () -> {
            PlayerInventory targetInv = target.getInventory();
            Inventory viewerInv = event.getView().getTopInventory();

            // Update main inventory (0-35)
            for (int i = 0; i < 36; i++) {
                targetInv.setItem(i, viewerInv.getItem(i));
            }

            // Update armor (36-39)
            ItemStack[] armor = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                armor[i] = viewerInv.getItem(36 + i);
            }
            targetInv.setArmorContents(armor);

            // Update off hand (40)
            targetInv.setItemInOffHand(viewerInv.getItem(40));

            target.updateInventory();
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player viewer = (Player) event.getPlayer();
        
        // Remove from tracking if this was an invsee inventory
        openInventories.remove(viewer.getUniqueId());
    }

    private String formatMessage(String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix", "&7[&bSSJ&7] "));
        return prefix + message;
    }
} 