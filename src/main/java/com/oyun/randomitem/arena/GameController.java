package com.oyun.randomitem.arena;

import com.oyun.randomitem.RandomItemPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class GameController {

    // Oyuncunun envanterinin son (8.) slotuna konan, lobiye donus icin kullanilan esya.
    public static final String LEAVE_ITEM_NAME = "Lobiye Don";
    public static final int LEAVE_ITEM_SLOT = 8;

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
        clearInventoryAndArmor(p);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        giveLeaveItem(p);

        broadcast(arena, ChatColor.YELLOW + p.getDisplayName() + ChatColor.GRAY + " oyuna katildi. ("
                + arena.getPlayers().size() + "/" + arena.getMaxPlayers() + ")");

        if (arena.getState() == ArenaState.BEKLEMEDE && arena.getPlayers().size() >= arena.getMinPlayers()) {
            startCountdown(arena);
        }
    }

    public void leave(Player p, Arena arena) {
        arena.removePlayer(p);
        p.setGameMode(GameMode.SURVIVAL);
        clearInventoryAndArmor(p);

        Location lobby = plugin.getSettings().getLobbyLocation();
        if (lobby != null) {
            p.teleport(lobby);
        }

        broadcast(arena, ChatColor.YELLOW + p.getDisplayName() + ChatColor.GRAY + " oyundan ayrildi.");

        if (arena.getState() == ArenaState.BASLIYOR && arena.getPlayers().size() < arena.getMinPlayers()) {
            // Geri sayim sirasinda minimumun altina dusuldu, bekleme durumuna geri don
            arena.setState(ArenaState.BEKLEMEDE);
            broadcast(arena, ChatColor.RED + "Yeterli oyuncu kalmadi, geri sayim iptal edildi.");
        }

        if (arena.getState() == ArenaState.OYUNDA) {
            checkWinner(arena);
        }
    }

    // 10 saniyelik, ekranda (title) gosterilen geri sayim. Minimum oyuncu sayisina
    // ulasilinca baslar, maksimum beklenmez. Sure boyunca oyuncular hareket edemez
    // (PlayerListener'daki BASLIYOR durumu kontrolu ile).
    public void startCountdown(Arena arena) {
        arena.setState(ArenaState.BASLIYOR);
        broadcast(arena, ChatColor.GREEN + "Yeterli oyuncu var! Geri sayim basladi.");

        new BukkitRunnable() {
            int secondsLeft = 10;

            @Override
            public void run() {
                if (arena.getState() != ArenaState.BASLIYOR) {
                    cancel();
                    return;
                }
                if (secondsLeft <= 0) {
                    cancel();
                    sendTitleToArena(arena, ChatColor.GREEN + "" + ChatColor.BOLD + "BASLA!", "");
                    start(arena);
                    return;
                }

                // Sadece 10'da bir kere, sonra 9-8-7-6 sessiz gecilir, 5'ten itibaren
                // her saniye tekrar gosterilir (5-4-3-2-1).
                if (secondsLeft == 10 || secondsLeft <= 5) {
                    String title = secondsLeft <= 5
                            ? ChatColor.RED + "" + ChatColor.BOLD + secondsLeft
                            : ChatColor.YELLOW + "" + secondsLeft;
                    sendTitleToArena(arena, title, ChatColor.GRAY + "Oyun basliyor...");
                }
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
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
            clearInventoryAndArmor(p); // yatak dahil her sey temizlenir, oyun basladiginda yatak kaybolur
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

            // Oyun bitti ama herkesi hemen degil, 5 saniye sonra lobiye atiyoruz
            // (kazanma anini gormeleri icin kisa bir bekleme).
            arena.setState(ArenaState.BITTI);
            Bukkit.getScheduler().runTaskLater(plugin, () -> endGame(arena), 100L);
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
                clearInventoryAndArmor(p);
                p.setFoodLevel(20);
                p.setHealth(20.0);
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

    // Envanteri VE zirhlari temizler. inventory.clear() tek basina zirh slotlarina
    // dokunmaz, bu yuzden ayrica setArmorContents ile sifirlanmasi gerekiyor.
    private void clearInventoryAndArmor(Player p) {
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);
    }

    private void giveLeaveItem(Player p) {
        ItemStack bed = new ItemStack(Material.BED, 1);
        ItemMeta meta = bed.getItemMeta();
        meta.setDisplayName(ChatColor.RED + LEAVE_ITEM_NAME);
        bed.setItemMeta(meta);
        p.getInventory().setItem(LEAVE_ITEM_SLOT, bed);
    }

    private void sendTitleToArena(Arena arena, String title, String subtitle) {
        for (UUID id : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p == null) continue;
            try {
                p.sendTitle(title, subtitle);
            } catch (Throwable ignored) {
                // Bazi sunucu corelerinde title API sorun cikarabilir, bu geri sayimi durdurmasin.
            }
        }
    }

    private void broadcast(Arena arena, String message) {
        for (UUID id : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) p.sendMessage(message);
        }
    }
}
