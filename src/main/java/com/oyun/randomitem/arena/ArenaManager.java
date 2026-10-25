package com.oyun.randomitem.arena;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ArenaManager {

    private final RandomItemPlugin plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final File file;
    private FileConfiguration cfg;

    public ArenaManager(RandomItemPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public Arena create(String name) {
        Arena arena = new Arena(name, plugin.getSettings().getMaxPlayers());
        arenas.put(name.toLowerCase(), arena);
        save();
        return arena;
    }

    public void delete(String name) {
        arenas.remove(name.toLowerCase());
        save();
    }

    public Arena get(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public Map<String, Arena> getArenas() { return arenas; }

    public void load() {
        plugin.getDataFolder().mkdirs();
        if (!file.exists()) {
            cfg = new YamlConfiguration();
            return;
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.getConfigurationSection("arenas") == null) return;

        for (String key : cfg.getConfigurationSection("arenas").getKeys(false)) {
            String base = "arenas." + key;
            int maxPlayers = cfg.getInt(base + ".maxplayers", plugin.getSettings().getMaxPlayers());

            Arena arena = new Arena(key, maxPlayers);
            arena.setWorldName(cfg.getString(base + ".world"));

            for (int i = 1; i <= maxPlayers; i++) {
                Location loc = LocationUtil.deserialize(cfg.getString(base + ".spawn" + i));
                if (loc != null) arena.setSpawnPoint(i, loc);
            }

            Location npcLoc = LocationUtil.deserialize(cfg.getString(base + ".npc"));
            if (npcLoc != null) arena.setNpcLocation(npcLoc);

            String npcIdStr = cfg.getString(base + ".npcId");
            if (npcIdStr != null) {
                try {
                    arena.setNpcEntityId(UUID.fromString(npcIdStr));
                } catch (IllegalArgumentException ignored) {
                    // kayitli id bozuksa yok sayilir
                }
            }

            arenas.put(key.toLowerCase(), arena);
        }
    }

    public void save() {
        cfg = new YamlConfiguration();
        for (Arena arena : arenas.values()) {
            String base = "arenas." + arena.getName();

            cfg.set(base + ".maxplayers", arena.getMaxPlayers());

            if (arena.getWorldName() != null) {
                cfg.set(base + ".world", arena.getWorldName());
            }

            Location[] spawns = arena.getSpawnPoints();
            for (int i = 0; i < spawns.length; i++) {
                if (spawns[i] != null) {
                    cfg.set(base + ".spawn" + (i + 1), LocationUtil.serialize(spawns[i]));
                }
            }

            if (arena.getNpcLocation() != null) {
                cfg.set(base + ".npc", LocationUtil.serialize(arena.getNpcLocation()));
            }
            if (arena.getNpcEntityId() != null) {
                cfg.set(base + ".npcId", arena.getNpcEntityId().toString());
            }
        }

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("arenas.yml kaydedilemedi: " + e.getMessage());
        }
    }
}
