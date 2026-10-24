package com.oyun.randomitem.listeners;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.menu.ArenaMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCListener implements Listener {

    private final RandomItemPlugin plugin;

    public NPCListener(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNpcClick(PlayerInteractEntityEvent e) {
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getNpcEntityId() == null) continue;
            if (arena.getNpcEntityId().equals(e.getRightClicked().getUniqueId())) {
                Player p = e.getPlayer();
                p.openInventory(ArenaMenu.build(plugin));
                return;
            }
        }
    }
}
