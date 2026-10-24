package com.oyun.randomitem.tasks;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.arena.ArenaState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
import java.util.UUID;

public class RandomItemTask extends BukkitRunnable {

    // Oyunculara rastgele dagitilabilecek esyalarin havuzu.
    // Buraya istediginiz kadar Material ekleyip cikarabilirsiniz.
    private static final Material[] ITEM_POOL = new Material[] {
            Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.BOW, Material.ARROW,
            Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.IRON_HELMET,
            Material.GOLDEN_APPLE, Material.APPLE, Material.COOKED_BEEF, Material.ENDER_PEARL,
            Material.POTION, Material.TNT, Material.FLINT_AND_STEEL,
            Material.WATER_BUCKET, Material.LAVA_BUCKET, Material.IRON_INGOT,
            Material.WEB, Material.FISHING_ROD
    };

    private final RandomItemPlugin plugin;
    private final Random random = new Random();

    public RandomItemTask(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getState() != ArenaState.OYUNDA) continue;

            for (UUID id : arena.getAlivePlayers()) {
                Player p = Bukkit.getPlayer(id);
                if (p == null) continue;

                ItemStack item = randomItem();
                p.getInventory().addItem(item);
                p.sendMessage("§aYeni rastgele esya aldin: §e" + formatName(item.getType()));
            }
        }
    }

    private ItemStack randomItem() {
        Material mat = ITEM_POOL[random.nextInt(ITEM_POOL.length)];
        int amount = 1;
        if (mat == Material.ARROW || mat == Material.WEB) {
            amount = 1 + random.nextInt(4);
        }
        return new ItemStack(mat, amount);
    }

    private String formatName(Material mat) {
        String name = mat.name().toLowerCase().replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
