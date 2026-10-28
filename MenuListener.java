package com.oyun.randomitem.listeners;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.menu.ArenaMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

    private final RandomItemPlugin plugin;

    public MenuListener(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        if (!ArenaMenu.TITLE.equals(e.getView().getTitle())) return;

        // Menudeki esyalarin envantere alinmasini/surklenmesini engelle
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String arenaName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        Arena arena = plugin.getArenaManager().get(arenaName);
        if (arena == null) return;

        Player p = (Player) e.getWhoClicked();
        plugin.getGameController().join(p, arena);
    }
}
