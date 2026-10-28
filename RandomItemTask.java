package com.oyun.randomitem.tasks;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RandomItemTask extends BukkitRunnable {

    private final RandomItemPlugin plugin;
    private final Random random = new Random();

    public RandomItemTask(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<Material> placeable = plugin.getSettings().getPlaceableItems();
        List<Material> other = plugin.getSettings().getOtherItems();
        int weight = plugin.getSettings().getPlaceableWeight();

        if (placeable.isEmpty() && other.isEmpty()) return;

        // Agirlikli havuz: koyulabilir bloklar "weight" kadar tekrar eklenir, daha sik cikmasi icin
        List<Material> pool = new ArrayList<>();
        for (int i = 0; i < weight; i++) pool.addAll(placeable);
        pool.addAll(other);
        if (pool.isEmpty()) return;

        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getState() != ArenaState.OYUNDA) continue;

            for (UUID id : arena.getAlivePlayers()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null) continue;

                Material mat = pool.get(random.nextInt(pool.size()));
                p.getInventory().addItem(new ItemStack(mat, 1)); // her zaman 1 adet
                // Bilerek chat mesaji yok - esya sessizce envantere ekleniyor.
            }
        }
    }
}
