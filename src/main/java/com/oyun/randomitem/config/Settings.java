package com.oyun.randomitem.config;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

// Tum ayarlar config.yml uzerinden okunur/yazilir, boylece plugin'i yeniden
// derlemeden sadece dosyayi duzenleyip /reload yaparak her sey degistirilebilir.
public class Settings {

    private final RandomItemPlugin plugin;

    public Settings(RandomItemPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public int getMaxPlayers() {
        return Math.max(2, cfg().getInt("max-players", 4));
    }

    public long getItemIntervalTicks() {
        int seconds = Math.max(1, cfg().getInt("item-interval-seconds", 15));
        return seconds * 20L;
    }

    public int getMenuStartSlot() {
        int slot = cfg().getInt("menu-start-slot", 20);
        if (slot < 0) slot = 0;
        if (slot > 53) slot = 53;
        return slot;
    }

    public int getPlaceableWeight() {
        return Math.max(1, cfg().getInt("placeable-weight", 3));
    }

    public List<Material> getPlaceableItems() {
        return parseMaterialList("items.placeable");
    }

    public List<Material> getOtherItems() {
        return parseMaterialList("items.other");
    }

    private List<Material> parseMaterialList(String path) {
        List<Material> result = new ArrayList<>();
        for (String name : cfg().getStringList(path)) {
            Material mat = Material.matchMaterial(name);
            if (mat != null) {
                result.add(mat);
            } else {
                plugin.getLogger().log(Level.WARNING, "config.yml icinde gecersiz esya ismi: " + name);
            }
        }
        return result;
    }

    public Location getLobbyLocation() {
        return LocationUtil.deserialize(cfg().getString("lobby"));
    }

    public void setLobbyLocation(Location loc) {
        cfg().set("lobby", LocationUtil.serialize(loc));
        plugin.saveConfig();
    }

    public Location getSpectatorLocation() {
        return LocationUtil.deserialize(cfg().getString("spectator-spawn"));
    }

    public void setSpectatorLocation(Location loc) {
        cfg().set("spectator-spawn", LocationUtil.serialize(loc));
        plugin.saveConfig();
    }
}
