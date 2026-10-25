package com.oyun.randomitem.listeners;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.arena.ArenaState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

// Oyun sirasinda oyuncularin koydugu bloklari (ve dokulen su/lav gibi sivilari)
// ilgili arenanin placedBlocks listesine kaydeder, boylece oyun bitince temizlenebilir.
public class BlockTrackingListener implements Listener {

    private final RandomItemPlugin plugin;

    public BlockTrackingListener(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    private Arena findArena(Player p) {
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getPlayers().contains(p.getUniqueId())) return arena;
        }
        return null;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Arena arena = findArena(e.getPlayer());
        if (arena == null || arena.getState() != ArenaState.OYUNDA) return;
        arena.getPlacedBlocks().add(e.getBlock().getLocation());
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Arena arena = findArena(e.getPlayer());
        if (arena == null || arena.getState() != ArenaState.OYUNDA) return;
        // Suyun/lavin dokuldugu blok konumu (relative'in bir bloguncesi, genelde block)
        arena.getPlacedBlocks().add(e.getBlockClicked().getRelative(e.getBlockFace()).getLocation());
    }
}
