package com.oyun.randomitem.commands;

import com.oyun.randomitem.RandomItemPlugin;
import com.oyun.randomitem.arena.Arena;
import com.oyun.randomitem.menu.ArenaMenu;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RandomItemCommand implements CommandExecutor {

    private final RandomItemPlugin plugin;

    // Her yetkilinin uzerinde calistigi "secili harita" - /ri create ile otomatik,
    // /ri select ile elle secilir. Boylece setspawn/setnpc'de her seferinde
    // harita ismini tekrar yazmaya gerek kalmaz.
    private final Map<UUID, String> selectedArena = new HashMap<>();
    private static final UUID CONSOLE_KEY = new UUID(0, 0);

    public RandomItemCommand(RandomItemPlugin plugin) {
        this.plugin = plugin;
    }

    private UUID keyFor(CommandSender sender) {
        return (sender instanceof Player) ? ((Player) sender).getUniqueId() : CONSOLE_KEY;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "select":
                return handleSelect(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "setnpc":
                return handleSetNpc(sender, args);
            case "list":
                return handleList(sender);
            case "menu":
                return handleMenu(sender);
            case "forcestart":
                return handleForceStart(sender, args);
            case "leave":
                return handleLeave(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) { sender.sendMessage(err("Kullanim: /ri create <isim>")); return true; }

        String name = args[1];
        if (plugin.getArenaManager().get(name) != null) {
            sender.sendMessage(err("Bu isimde bir harita zaten var."));
            return true;
        }
        plugin.getArenaManager().create(name);
        selectedArena.put(keyFor(sender), name);
        sender.sendMessage(ok("'" + name + "' haritasi olusturuldu ve secili harita yapildi."));
        sender.sendMessage(ChatColor.GRAY + "Simdi her dogma noktasina gidip " + ChatColor.YELLOW
                + "/ri setspawn <1-5>" + ChatColor.GRAY + " yaz (harita ismini tekrar yazmana gerek yok).");
        return true;
    }

    private boolean handleSelect(CommandSender sender, String[] args) {
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) { sender.sendMessage(err("Kullanim: /ri select <isim>")); return true; }

        Arena arena = plugin.getArenaManager().get(args[1]);
        if (arena == null) { sender.sendMessage(err("Harita bulunamadi.")); return true; }

        selectedArena.put(keyFor(sender), arena.getName());
        sender.sendMessage(ok("'" + arena.getName() + "' artik secili harita. setspawn/setnpc komutlarinda ismi tekrar yazmana gerek yok."));
        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) { sender.sendMessage(err("Kullanim: /ri delete <isim>")); return true; }

        if (plugin.getArenaManager().get(args[1]) == null) {
            sender.sendMessage(err("Harita bulunamadi."));
            return true;
        }
        plugin.getArenaManager().delete(args[1]);
        sender.sendMessage(ok("Harita silindi."));
        return true;
    }

    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(err("Bu komut sadece oyun icinde kullanilabilir.")); return true; }
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) {
            sender.sendMessage(err("Kullanim: /ri setspawn <1-5>  (once /ri create veya /ri select ile bir harita sec)"));
            sender.sendMessage(err("ya da:    /ri setspawn <isim> <1-5>"));
            return true;
        }

        Arena arena;
        String indexArg;

        if (args.length >= 3) {
            // /ri setspawn <isim> <1-5> - eski/acik format
            arena = plugin.getArenaManager().get(args[1]);
            if (arena == null) { sender.sendMessage(err("Harita bulunamadi: " + args[1])); return true; }
            indexArg = args[2];
            selectedArena.put(keyFor(sender), arena.getName());
        } else {
            // /ri setspawn <1-5> - secili haritayi kullan
            String selected = selectedArena.get(keyFor(sender));
            if (selected == null || plugin.getArenaManager().get(selected) == null) {
                sender.sendMessage(err("Secili bir haritan yok. Once /ri create <isim> veya /ri select <isim> kullan."));
                return true;
            }
            arena = plugin.getArenaManager().get(selected);
            indexArg = args[1];
        }

        int index;
        try {
            index = Integer.parseInt(indexArg);
        } catch (NumberFormatException ex) {
            sender.sendMessage(err("Dogma noktasi numarasi 1 ile 5 arasinda bir sayi olmali."));
            return true;
        }
        if (index < 1 || index > 5) {
            sender.sendMessage(err("Dogma noktasi numarasi 1 ile 5 arasinda olmali."));
            return true;
        }

        Player p = (Player) sender;
        arena.setSpawnPoint(index, p.getLocation());
        plugin.getArenaManager().save();

        int doluSayisi = 0;
        for (int i = 1; i <= 5; i++) if (arena.getSpawnPoints()[i - 1] != null) doluSayisi++;

        sender.sendMessage(ok(index + ". dogma noktasi ayarlandi. (" + arena.getName() + ") - "
                + doluSayisi + "/5 tamamlandi"));

        if (arena.hasAllSpawns()) {
            sender.sendMessage(ChatColor.GOLD + "Tum dogma noktalari tamam! Simdi /ri setnpc " + arena.getName() + " ile menu NPC'sini koyabilirsin.");
        }
        return true;
    }

    private boolean handleSetNpc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(err("Bu komut sadece oyun icinde kullanilabilir.")); return true; }
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }

        Arena arena;
        if (args.length >= 2) {
            arena = plugin.getArenaManager().get(args[1]);
            if (arena == null) { sender.sendMessage(err("Harita bulunamadi: " + args[1])); return true; }
            selectedArena.put(keyFor(sender), arena.getName());
        } else {
            String selected = selectedArena.get(keyFor(sender));
            if (selected == null || plugin.getArenaManager().get(selected) == null) {
                sender.sendMessage(err("Secili bir haritan yok. Kullanim: /ri setnpc <isim>  ya da once /ri select <isim> yap."));
                return true;
            }
            arena = plugin.getArenaManager().get(selected);
        }

        if (!arena.hasAllSpawns()) {
            sender.sendMessage(ChatColor.YELLOW + "Uyari: '" + arena.getName() + "' haritasinin dogma noktalari henuz tamam degil. "
                    + "Harita menude hazir olmayacak sekilde gorunecek, oyuncular giremeyecek.");
        }

        Player p = (Player) sender;
        Location loc = p.getLocation();

        Entity entity = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        ArmorStand stand = (ArmorStand) entity;
        stand.setCustomName(ChatColor.AQUA + "Haritalar" + ChatColor.GRAY + " [" + arena.getName() + "]");
        stand.setCustomNameVisible(true);
        stand.setGravity(false);
        stand.setBasePlate(true);
        stand.setCanPickupItems(false);

        arena.setNpcLocation(loc);
        arena.setNpcEntityId(stand.getUniqueId());
        plugin.getArenaManager().save();
        sender.sendMessage(ok("NPC olusturuldu. Oyuncular tikladiginda harita menusu acilacak."));
        return true;
    }

    private boolean handleList(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- Haritalar ---");
        if (plugin.getArenaManager().getArenas().isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "Henuz harita yok.");
            return true;
        }
        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            sender.sendMessage(ChatColor.YELLOW + arena.getName() + ChatColor.GRAY
                    + " | Durum: " + arena.getState()
                    + " | Oyuncular: " + arena.getPlayers().size() + "/" + arena.getMaxPlayers());
        }
        return true;
    }

    private boolean handleMenu(CommandSender sender) {
        if (!(sender instanceof Player)) { sender.sendMessage(err("Bu komut sadece oyun icinde kullanilabilir.")); return true; }
        ((Player) sender).openInventory(ArenaMenu.build(plugin));
        return true;
    }

    private boolean handleForceStart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) { sender.sendMessage(err("Kullanim: /ri forcestart <isim>")); return true; }

        Arena arena = plugin.getArenaManager().get(args[1]);
        if (arena == null) { sender.sendMessage(err("Harita bulunamadi.")); return true; }

        if (!arena.hasAllSpawns()) {
            sender.sendMessage(err("Bu haritanin 5 dogma noktasi da ayarlanmamis, oyun baslatilamaz."));
            sender.sendMessage(err("Once /ri select " + arena.getName() + " ve /ri setspawn <1-5> ile tamamla."));
            return true;
        }
        if (arena.getPlayers().isEmpty()) {
            sender.sendMessage(err("Haritada hic oyuncu yok, baslatmanin anlami yok."));
            return true;
        }

        plugin.getGameController().start(arena);
        sender.sendMessage(ok("Oyun zorla baslatildi."));
        return true;
    }

    private boolean handleLeave(CommandSender sender) {
        if (!(sender instanceof Player)) { sender.sendMessage(err("Bu komut sadece oyun icinde kullanilabilir.")); return true; }
        Player p = (Player) sender;

        for (Arena arena : plugin.getArenaManager().getArenas().values()) {
            if (arena.getPlayers().contains(p.getUniqueId())) {
                plugin.getGameController().leave(p, arena);
                sender.sendMessage(ok("Oyundan ayrildin."));
                return true;
            }
        }
        sender.sendMessage(err("Su an bir oyunda degilsin."));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- RandomItem Komutlari ---");
        sender.sendMessage(ChatColor.YELLOW + "/ri create <isim>" + ChatColor.GRAY + " - yeni harita olustur (otomatik secer)");
        sender.sendMessage(ChatColor.YELLOW + "/ri select <isim>" + ChatColor.GRAY + " - bir haritayi 'secili' yap");
        sender.sendMessage(ChatColor.YELLOW + "/ri delete <isim>" + ChatColor.GRAY + " - harita sil");
        sender.sendMessage(ChatColor.YELLOW + "/ri setspawn <1-5>" + ChatColor.GRAY + " - secili haritaya dogma noktasi ekle");
        sender.sendMessage(ChatColor.YELLOW + "/ri setspawn <isim> <1-5>" + ChatColor.GRAY + " - belirli haritaya dogma noktasi ekle");
        sender.sendMessage(ChatColor.YELLOW + "/ri setnpc <isim>" + ChatColor.GRAY + " - menu npc'si olustur");
        sender.sendMessage(ChatColor.YELLOW + "/ri list" + ChatColor.GRAY + " - haritalari listele");
        sender.sendMessage(ChatColor.YELLOW + "/ri menu" + ChatColor.GRAY + " - harita menusunu ac");
        sender.sendMessage(ChatColor.YELLOW + "/ri forcestart <isim>" + ChatColor.GRAY + " - oyunu zorla baslat");
        sender.sendMessage(ChatColor.YELLOW + "/ri leave" + ChatColor.GRAY + " - oyundan ayril");
    }

    private String ok(String msg) { return ChatColor.GREEN + "" + msg; }
    private String err(String msg) { return ChatColor.RED + "" + msg; }
    private void noPerm(CommandSender sender) { sender.sendMessage(ChatColor.RED + "Bu komutu kullanma izniniz yok."); }
}
