package com.oyun.randomitem.listeners;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final RandomItemPlugin plugin;

    public PlayerListener(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    private Arena findArena(Player p) {
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getPlayers().contains(p.getUniqueId())) return arena;
        }
        return null;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Arena arena = findArena(p);
        if (arena == null) return;

        e.setDeathMessage(null);
        e.getDrops().clear();

        plugin.getGameController().eliminatePlayer(p, arena);

        Bukkit.getScheduler().runTask(plugin, () -> {
            p.spigot().respawn();
            p.setGameMode(GameMode.SPECTATOR);

            Location specLoc = plugin.getSettings().getSpectatorLocation();
            if (specLoc == null) specLoc = plugin.getSettings().getLobbyLocation();
            if (specLoc != null) p.teleport(specLoc);

            // Kazanan kontrolu simdi (izleyici atamasindan SONRA) yapiliyor, boylece
            // oyun bu kisinin olumuyle bitiyorsa endGame() onu dogru sekilde lobiye tasir.
            plugin.getGameController().checkWinner(arena);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Arena arena = findArena(p);
        if (arena != null) {
            plugin.getGameController().leave(p, arena);
        }
    }
}
