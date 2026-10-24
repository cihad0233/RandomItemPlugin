package com.oyun.randomitem;

import com.oyun.randomitem.arena.ArenaManager;
import com.oyun.randomitem.arena.GameController;
import com.oyun.randomitem.commands.RandomItemCommand;
import com.oyun.randomitem.listeners.MenuListener;
import com.oyun.randomitem.listeners.NPCListener;
import com.oyun.randomitem.listeners.PlayerListener;
import com.oyun.randomitem.tasks.RandomItemTask;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomItemPlugin extends JavaPlugin {

    private static RandomItemPlugin instance;
    private ArenaManager arenaManager;
    private GameController gameController;

    @Override
    public void onEnable() {
        instance = this;
        getDataFolder().mkdirs();

        this.arenaManager = new ArenaManager(this);
        this.gameController = new GameController(this);

        getCommand("randomitem").setExecutor(new RandomItemCommand(this));

        getServer().getPluginManager().registerEvents(new NPCListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Oyundaki oyunculara periyodik olarak (4 saniyede bir) rastgele esya dagitan gorev
        new RandomItemTask(this).runTaskTimer(this, 100L, 80L);

        getLogger().info("RandomItemPlugin aktif edildi.");
    }

    @Override
    public void onDisable() {
        if (arenaManager != null) arenaManager.save();
        getLogger().info("RandomItemPlugin devre disi birakildi.");
    }

    public static RandomItemPlugin getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public GameController getGameController() { return gameController; }
}
