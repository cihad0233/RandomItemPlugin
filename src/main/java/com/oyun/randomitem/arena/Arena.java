package com.oyun.randomitem.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Arena {

    private final String name;
    private String worldName;
    private Location[] spawnPoints;
    private Location npcLocation;
    private UUID npcEntityId;
    private int maxPlayers;

    private ArenaState state = ArenaState.BEKLEMEDE;
    private final LinkedHashSet<UUID> players = new LinkedHashSet<>();
    private final Set<UUID> alivePlayers = new HashSet<>();

    // Oyun sirasinda oyuncularin koydugu bloklarin konumlari - oyun bitince temizlenir
    private final Set<Location> placedBlocks = new HashSet<>();

    public Arena(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = Math.max(2, maxPlayers);
        this.spawnPoints = new Location[this.maxPlayers];
    }

    public String getName() { return name; }

    public String getWorldName() { return worldName; }
    public void setWorldName(String worldName) { this.worldName = worldName; }

    public Location[] getSpawnPoints() { return spawnPoints; }

    public void setSpawnPoint(int index, Location loc) {
        if (index < 1 || index > maxPlayers) {
            throw new IllegalArgumentException("Spawn index 1-" + maxPlayers + " araliginda olmali");
        }
        spawnPoints[index - 1] = loc;
        if (loc != null && loc.getWorld() != null) {
            worldName = loc.getWorld().getName();
        }
    }

    public boolean hasAllSpawns() {
        for (Location l : spawnPoints) if (l == null) return false;
        return true;
    }

    public Location getNpcLocation() { return npcLocation; }
    public void setNpcLocation(Location npcLocation) { this.npcLocation = npcLocation; }

    public UUID getNpcEntityId() { return npcEntityId; }
    public void setNpcEntityId(UUID npcEntityId) { this.npcEntityId = npcEntityId; }

    public int getMaxPlayers() { return maxPlayers; }

    public ArenaState getState() { return state; }
    public void setState(ArenaState state) { this.state = state; }

    public LinkedHashSet<UUID> getPlayers() { return players; }
    public Set<UUID> getAlivePlayers() { return alivePlayers; }

    public boolean isFull() { return players.size() >= maxPlayers; }

    public boolean isJoinable() {
        return state == ArenaState.BEKLEMEDE && !isFull() && hasAllSpawns();
    }

    public void addPlayer(Player p) {
        players.add(p.getUniqueId());
        alivePlayers.add(p.getUniqueId());
    }

    public void removePlayer(Player p) {
        players.remove(p.getUniqueId());
        alivePlayers.remove(p.getUniqueId());
    }

    // Oyuncunun katilma sirasina gore hangi spawn noktasina denk geldigini dondurur
    public int getSpawnIndexFor(Player p) {
        int i = 0;
        for (UUID id : players) {
            if (id.equals(p.getUniqueId())) return i;
            i++;
        }
        return -1;
    }

    public Set<Location> getPlacedBlocks() { return placedBlocks; }

    public void reset() {
        players.clear();
        alivePlayers.clear();
        state = ArenaState.BEKLEMEDE;
    }
}
