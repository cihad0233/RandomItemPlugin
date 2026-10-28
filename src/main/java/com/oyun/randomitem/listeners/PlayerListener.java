package com.oyun.randomitem.listeners;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.arena.ArenaState;
import com.oyun.randomitem.arena.GameController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

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

    // Haritadan asagi (void'e) dusup ilk hasari alinca - gercekten olmesini beklemeden
    // aninda izleyici moduna al. Boylece oyuncu can azalirken bekletilmez.
    @EventHandler
    public void onVoidDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        Player p = (Player) e.getEntity();
        Arena arena = findArena(p);
        if (arena == null || arena.getState() != ArenaState.OYUNDA) return;
        if (!arena.getAlivePlayers().contains(p.getUniqueId())) return; // zaten elendi

        e.setCancelled(true);
        p.setHealth(20.0);
        p.setFallDistance(0f);

        eliminate(p, arena, false);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Arena arena = findArena(p);
        if (arena == null) return;
        if (!arena.getAlivePlayers().contains(p.getUniqueId())) return; // void ile zaten elendi

        e.setDeathMessage(null);
        e.getDrops().clear();

        eliminate(p, arena, true);
    }

    // Olum/elenme sonrasi ortak akis: izleyici moduna gecir, izleyici konumuna isinlat,
    // sonra (ayni tick icinde degil, bu task bittikten sonra) kazanan kontrolu yap.
    private void eliminate(Player p, Arena arena, boolean needsRespawn) {
        plugin.getGameController().eliminatePlayer(p, arena);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (needsRespawn) {
                p.spigot().respawn();
            }
            p.setGameMode(GameMode.SPECTATOR);

            Location specLoc = plugin.getSettings().getSpectatorLocation();
            if (specLoc == null) specLoc = plugin.getSettings().getLobbyLocation();
            if (specLoc != null) p.teleport(specLoc);

            // Kazanan kontrolu izleyici atamasindan SONRA yapiliyor (race onlemek icin).
            plugin.getGameController().checkWinner(arena);
        });
    }

    // Geri sayim sirasinda (BASLIYOR durumu) oyuncularin yerinden kipirdamasini engeller,
    // ama etrafina bakmasina (yaw/pitch) izin verir.
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null || e.getFrom() == null) return;

        Player p = e.getPlayer();
        Arena arena = findArena(p);
        if (arena == null || arena.getState() != ArenaState.BASLIYOR) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        boolean moved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        if (!moved) return;

        Location frozen = from.clone();
        frozen.setYaw(to.getYaw());
        frozen.setPitch(to.getPitch());
        e.setTo(frozen);
    }

    // Envanterin son slotundaki kirmizi yatagi kullanınca oyundan ayril (lobiye don).
    @EventHandler
    public void onLeaveItemUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.BED) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        if (!ChatColor.stripColor(item.getItemMeta().getDisplayName()).equals(GameController.LEAVE_ITEM_NAME)) return;

        Player p = e.getPlayer();
        Arena arena = findArena(p);
        if (arena == null) return;

        e.setCancelled(true); // gercek yatagin yere konmasini engelle
        plugin.getGameController().leave(p, arena);
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
