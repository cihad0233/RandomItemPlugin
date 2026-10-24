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

public class RandomItemCommand implements CommandExecutor {

    private final RandomItemPlugin plugin;

    public RandomItemCommand(RandomItemPlugin plugin) {
        this.plugin = plugin;
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
        sender.sendMessage(ok("'" + name + "' haritasi olusturuldu. Simdi /ri setspawn ile 5 dogma noktasi belirle."));
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
        if (args.length < 3) { sender.sendMessage(err("Kullanim: /ri setspawn <isim> <1-5>")); return true; }

        Arena arena = plugin.getArenaManager().get(args[1]);
        if (arena == null) { sender.sendMessage(err("Harita bulunamadi.")); return true; }

        int index;
        try {
            index = Integer.parseInt(args[2]);
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
        sender.sendMessage(ok(index + ". oyuncu dogma noktasi ayarlandi. (" + arena.getName() + ")"));
        return true;
    }

    private boolean handleSetNpc(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage(err("Bu komut sadece oyun icinde kullanilabilir.")); return true; }
        if (!sender.hasPermission("randomitem.admin")) { noPerm(sender); return true; }
        if (args.length < 2) { sender.sendMessage(err("Kullanim: /ri setnpc <isim>")); return true; }

        Arena arena = plugin.getArenaManager().get(args[1]);
        if (arena == null) { sender.sendMessage(err("Harita bulunamadi.")); return true; }

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
        sender.sendMessage(ChatColor.YELLOW + "/ri create <isim>" + ChatColor.GRAY + " - yeni harita olustur");
        sender.sendMessage(ChatColor.YELLOW + "/ri delete <isim>" + ChatColor.GRAY + " - harita sil");
        sender.sendMessage(ChatColor.YELLOW + "/ri setspawn <isim> <1-5>" + ChatColor.GRAY + " - dogma noktasi ayarla");
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
