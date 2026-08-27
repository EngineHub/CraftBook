package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Passive observability for pipe networks, and the menu behind /cb pipes networks.
 *
 * Nothing here traverses anything: the pipe search already visits every block of a
 * network on every pulse, so activity, size and throughput are recorded as a free
 * side effect of pulses that actually ran. A network's identity is the smallest
 * visited position of its traversal, so the same network pulsed from different
 * pistons appears once. Networks disappear from the list ten minutes after their
 * last pulse - the menu deliberately shows what runs now, not a map of everything
 * ever built.
 */
public final class PipeNetworks implements Listener {

    private static final long FORGET_AFTER_MILLIS = 10 * 60 * 1000L;
    private static final int MAX_PER_WORLD = 256;
    private static final int PAGE_SIZE = 45; // top five rows; bottom row is navigation

    private static final Comparator<Vector> POSITION_ORDER = Comparator
            .comparingInt(Vector::getBlockX)
            .thenComparingInt(Vector::getBlockY)
            .thenComparingInt(Vector::getBlockZ);

    private static final class Net {
        Vector entry;
        int size;
        long moved;
        long lastActive;
        boolean blocked;
    }

    private static final Map<UUID, Map<Vector, Net>> byWorld = new HashMap<>();

    private static final PipeNetworks INSTANCE = new PipeNetworks();

    private PipeNetworks() {
    }

    public static PipeNetworks get() {
        return INSTANCE;
    }

    static void clear() {
        byWorld.clear();
    }

    /** Called after a pulse that traversed; costs two map operations and a min-scan. */
    static void record(World world, Set<Vector> visited, Vector entry, int moved, boolean blocked) {
        if (visited.isEmpty())
            return;
        Vector netKey = null;
        for (Vector visitedPos : visited) {
            if (netKey == null || POSITION_ORDER.compare(visitedPos, netKey) < 0)
                netKey = visitedPos;
        }
        Map<Vector, Net> nets = byWorld.computeIfAbsent(world.getUID(), w -> new HashMap<>());
        Net net = nets.get(netKey);
        if (net == null) {
            if (nets.size() >= MAX_PER_WORLD)
                trim(nets);
            net = new Net();
            nets.put(netKey, net);
        }
        net.entry = entry;
        net.size = Math.max(net.size, visited.size());
        net.moved += moved;
        net.lastActive = System.currentTimeMillis();
        if (blocked)
            net.blocked = true;
        else if (moved > 0)
            net.blocked = false;
    }

    private static void trim(Map<Vector, Net> nets) {
        long cutoff = System.currentTimeMillis() - FORGET_AFTER_MILLIS;
        nets.values().removeIf(net -> net.lastActive < cutoff);
        if (nets.size() >= MAX_PER_WORLD)
            nets.clear(); // pathological churn; start over rather than grow
    }

    /* ------------------------------------ menu ------------------------------------ */

    private static final class Menu implements InventoryHolder {
        final List<Location> slots = new ArrayList<>();
        Inventory inventory;
        int page;
        boolean hasPrev;
        boolean hasNext;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        long now = System.currentTimeMillis();
        record Row(UUID world, Net net) {
        }
        List<Row> rows = new ArrayList<>();
        for (Map.Entry<UUID, Map<Vector, Net>> worldEntry : byWorld.entrySet()) {
            worldEntry.getValue().values().removeIf(net -> now - net.lastActive > FORGET_AFTER_MILLIS);
            for (Net net : worldEntry.getValue().values()) {
                rows.add(new Row(worldEntry.getKey(), net));
            }
        }
        rows.sort(Comparator.comparingLong((Row row) -> row.net().lastActive).reversed());

        int pages = Math.max(1, (rows.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        if (page >= pages)
            page = pages - 1;
        if (page < 0)
            page = 0;
        boolean paginate = rows.size() > PAGE_SIZE;
        List<Row> shown = rows.subList(page * PAGE_SIZE, Math.min(rows.size(), (page + 1) * PAGE_SIZE));

        Menu menu = new Menu();
        menu.page = page;
        menu.hasPrev = page > 0;
        menu.hasNext = page < pages - 1;
        int size = paginate ? 54 : Math.min(54, ((Math.max(1, shown.size()) + 8) / 9) * 9);
        menu.inventory = Bukkit.createInventory(menu, size,
                ChatColor.DARK_AQUA + "Pipe networks (" + rows.size() + " active"
                + (paginate ? ", page " + (page + 1) + "/" + pages : "") + ")");

        for (Row row : shown) {
            if (menu.slots.size() >= PAGE_SIZE)
                break;
            Net net = row.net();
            World world = Bukkit.getWorld(row.world());
            if (world == null)
                continue;
            int x = net.entry.getBlockX();
            int y = net.entry.getBlockY();
            int z = net.entry.getBlockZ();

            ItemStack icon = new ItemStack(net.blocked ? Material.CAMPFIRE : Material.STICKY_PISTON,
                    Math.max(1, Math.min(64, net.size)));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName((net.blocked ? ChatColor.RED : ChatColor.AQUA)
                    + world.getName() + " " + x + " " + y + " " + z);
            List<String> lore = new ArrayList<>();
            // "At least": a delivering pulse only traverses until its items are
            // placed, so the recorded size is a high-water mark, not a survey.
            lore.add(ChatColor.GRAY + "Size: " + ChatColor.WHITE + "at least " + net.size + " blocks");
            lore.add(ChatColor.GRAY + "Items moved: " + ChatColor.WHITE + net.moved
                    + ChatColor.DARK_GRAY + " (since startup)");
            lore.add(ChatColor.GRAY + "Last pulse: " + ChatColor.WHITE + ago(now - net.lastActive));
            lore.add(net.blocked
                    ? ChatColor.RED + "BLOCKED - the last pull was refused everywhere"
                    : ChatColor.GREEN + "Running");
            lore.add(ChatColor.YELLOW + "Click to teleport");
            meta.setLore(lore);
            icon.setItemMeta(meta);

            menu.slots.add(new Location(world, x + 0.5, y + 1.0, z + 0.5));
            menu.inventory.addItem(icon);
        }

        if (paginate) {
            if (menu.hasPrev)
                menu.inventory.setItem(45, navItem(Material.ARROW, ChatColor.YELLOW + "Previous page"));
            menu.inventory.setItem(49, navItem(Material.PAPER, ChatColor.GRAY + "Page " + (page + 1) + " of " + pages));
            if (menu.hasNext)
                menu.inventory.setItem(53, navItem(Material.ARROW, ChatColor.YELLOW + "Next page"));
        }

        player.openInventory(menu.inventory);
    }

    private static ItemStack navItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private static String ago(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60)
            return seconds + "s ago";
        return (seconds / 60) + "m " + (seconds % 60) + "s ago";
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu menu))
            return;
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (slot == 45 && menu.hasPrev) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> open(player, menu.page - 1));
            return;
        }
        if (slot == 53 && menu.hasNext) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> open(player, menu.page + 1));
            return;
        }
        if (slot < 0 || slot >= menu.slots.size())
            return;
        Location target = menu.slots.get(slot);
        // Never close or teleport inside the click event itself: the client still
        // believes the inventory is open and desyncs, worst on shift-clicks.
        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
            player.closeInventory();
            player.teleport(target);
            player.sendMessage(ChatColor.AQUA + "Teleported to the pipe network at "
                    + target.getWorld().getName() + " " + target.getBlockX() + " " + target.getBlockY() + " " + target.getBlockZ());
        });
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Menu)
            event.setCancelled(true);
    }
}
