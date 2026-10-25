package com.oyun.randomitem.arena;

import com.oyun.randomitem.RandomItemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GameController {

    private final RandomItemPlugin plugin;

    public GameController(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    public void join(Player p, Arena arena) {
        if (arena.getState() == ArenaState.OYUNDA || arena.getState() == ArenaState.BASLIYOR) {
            p.sendMessage(ChatColor.RED + "Bu harita su an oynaniyor, girilemez!");
            return;
        }
        if (arena.isFull()) {
            p.sendMessage(ChatColor.RED + "Bu harita dolu! (" + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")");
            return;
        }
        if (!arena.hasAllSpawns()) {
            p.sendMessage(ChatColor.RED + "Bu harita henuz hazir degil, spawn noktalari eksik.");
            return;
        }
        if (arena.getPlayers().contains(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "Zaten bu haritadasin.");
            return;
        }

        arena.addPlayer(p);
        p.closeInventory();

        int index = arena.getSpawnIndexFor(p);
        Location spawn = arena.getSpawnPoints()[index];
        p.teleport(spawn);
        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);

        broadcast(arena, ChatColor.YELLOW + p.getDisplayName() + ChatColor.GRAY + " oyuna katildi. ("
                + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")");

        if (arena.isFull()) {
            startCountdown(arena, 5);
        }
    }

    public void leave(Player p, Arena arena) {
        arena.removePlayer(p);
        p.setGameMode(GameMode.SURVIVAL);

        Location lobby = plugin.getSettings().getLobbyLocation();
        if (lobby != null) {
            p.teleport(lobby);
        }

        broadcast(arena, ChatColor.YELLOW + p.getDisplayName() + ChatColor.GRAY + " oyundan ayrildi.");
        if (arena.getState() == ArenaState.OYUNDA) {
            checkWinner(arena);
        }
    }

    public void startCountdown(Arena arena, int seconds) {
        arena.setState(ArenaState.BASLIYOR);
        broadcast(arena, ChatColor.GREEN + "Harita doldu! Oyun " + seconds + " saniye icinde basliyor.");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (arena.getState() == ArenaState.BASLIYOR) {
                start(arena);
            }
        }, seconds * 20L);
    }

    public void start(Arena arena) {
        arena.setState(ArenaState.OYUNDA);
        for (UUID id : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            int idx = arena.getSpawnIndexFor(p);
            p.teleport(arena.getSpawnPoints()[idx]);
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(20.0);
            p.setFoodLevel(20);
            p.getInventory().clear();
        }
        broadcast(arena, ChatColor.GOLD + "Oyun basladi! Rastgele esyalar dagitilacak, son hayatta kalan kazanir!");
    }

    // Sadece elenme kaydini tutar, kazanan kontrolunu yapmaz - bu PlayerListener
    // tarafindan oyuncu izleyici moduna/konumuna atandiktan SONRA (bir sonraki tick'te)
    // cagrilir. Boylece endGame() o oyuncuyu izleyici atamasindan sonra lobiye tasiyabilir.
    public void eliminatePlayer(Player p, Arena arena) {
        arena.getAlivePlayers().remove(p.getUniqueId());
        broadcast(arena, ChatColor.RED + p.getDisplayName() + ChatColor.RED + " elendi! ("
                + arena.getAlivePlayers().size() + " oyuncu kaldi)");
    }

    public void checkWinner(Arena arena) {
        if (arena.getState() != ArenaState.OYUNDA) return;
        if (arena.getAlivePlayers().size() <= 1) {
            String winnerName = "Kimse";
            if (arena.getAlivePlayers().size() == 1) {
                UUID winnerId = arena.getAlivePlayers().iterator().next();
                Player winner = Bukkit.getPlayer(winnerId);
                if (winner != null) winnerName = winner.getDisplayName();
            }
            broadcast(arena, ChatColor.GOLD + "Oyun bitti! Kazanan: " + winnerName);
            endGame(arena);
        }
    }

    public void endGame(Arena arena) {
        Location lobby = plugin.getSettings().getLobbyLocation();
        if (lobby == null) {
            lobby = Bukkit.getWorlds().get(0).getSpawnLocation();
        }

        for (UUID id : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                p.teleport(lobby);
                p.setGameMode(GameMode.SURVIVAL); // izleyici modundaysa da buradan cikarir
            }
        }

        // Oyun sirasinda koyulan tum bloklari temizle (haritayi bir sonraki mac icin sifirlar)
        for (Location loc : arena.getPlacedBlocks()) {
            if (loc.getWorld() != null) {
                loc.getBlock().setType(Material.AIR);
            }
        }
        arena.getPlacedBlocks().clear();

        arena.reset();
    }

    private void broadcast(Arena arena, String message) {
        for (UUID id : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage(message);
        }
    }
}
