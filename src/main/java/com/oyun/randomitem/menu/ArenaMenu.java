package com.oyun.randomitem.menu;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaMenu {

    // Not: Minecraft envanterlerinde gercek bir "9x9" boyutu yoktur; bir sandik
    // GUI'si en fazla 9x6 (54 slot) olabilir. Bu yuzden harita menusu 54 slotluk
    // (6 satir, her satirda 9 harita) bir GUI olarak hazirlandi.
    public static final String TITLE = ChatColor.DARK_GRAY + "Haritalar";
    public static final int SIZE = 54;

    public static Inventory build(RandomItemPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, SIZE, TITLE);

        int slot = plugin.getSettings().getMenuStartSlot();
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (slot >= SIZE) break;
            inv.setItem(slot, buildItem(arena));
            slot++;
        }
        return inv;
    }

    private static ItemStack buildItem(Arena arena) {
        // BASLIYOR (geri sayim) sirasinda hala katilinabilir, sadece oyun gercekten
        // basladiktan (OYUNDA) veya bittikten (BITTI) sonra kilitlenir.
        boolean kilitli = arena.getState() == ArenaState.OYUNDA
                || arena.getState() == ArenaState.BITTI
                || arena.isFull()
                || !arena.hasAllSpawns();

        ItemStack item = kilitli
                ? new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14) // kirmizi cam
                : new ItemStack(Material.PAPER);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + arena.getName());

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.WHITE + "Durum: " + durumMetni(arena));
        lore.add(ChatColor.WHITE + "Oyuncular: " + ChatColor.YELLOW
                + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
        lore.add("");

        if (kilitli) {
            lore.add(ChatColor.RED + "Su an girilemiyor.");
        } else {
            lore.add(ChatColor.GRAY + "\u2022 Oynamak icin tikla");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String durumMetni(Arena arena) {
        switch (arena.getState()) {
            case BASLIYOR:
                return ChatColor.YELLOW + "BASLIYOR";
            case OYUNDA:
                return ChatColor.RED + "OYNANIYOR";
            case BITTI:
                return ChatColor.RED + "BITIYOR";
            case BEKLEMEDE:
            default:
                return ChatColor.AQUA + "BEKLEMEDE";
        }
    }
}
