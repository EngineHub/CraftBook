package com.sk89q.craftbook.bukkit.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class PlayerUtil {

    /**
     * Checks to see if the sender is a player, otherwise throw an exception.
     *
     * @param sender
     *
     * @return
     *
     * @throws com.sk89q.minecraft.util.commands.CommandException
     *
     */
    public static Player checkPlayer(CommandSender sender) throws CommandException {

        if (sender instanceof Player) return (Player) sender;
        else
            throw new CommandException("A player context is required. (Specify a world or player if the command " +
                    "supports it.)");
    }

    /**
     * Match player names.
     *
     * @param filter
     *
     * @return
     */
    public static List<Player> matchPlayerNames(String filter) {

        Collection<? extends Player> players = CraftBookPlugin.inst().getServer().getOnlinePlayers();
        boolean useDisplayNames = true;

        filter = filter.toLowerCase(Locale.ENGLISH);

        // Allow exact name matching
        if (filter.charAt(0) == '@' && filter.length() >= 2) {
            filter = filter.substring(1);

            for (Player player : players) {
                if (player.getName().equalsIgnoreCase(filter) || useDisplayNames
                        && ChatColor.stripColor(player.getDisplayName()).equalsIgnoreCase(filter)) {
                    List<Player> list = new ArrayList<>();
                    list.add(player);
                    return list;
                }
            }

            return new ArrayList<>();
            // Allow partial name matching
        } else if (filter.charAt(0) == '*' && filter.length() >= 2) {
            filter = filter.substring(1);

            List<Player> list = new ArrayList<>();

            for (Player player : players) {
                if (player.getName().toLowerCase(Locale.ENGLISH).contains(filter) || useDisplayNames
                        && ChatColor.stripColor(player.getDisplayName().toLowerCase(Locale.ENGLISH)).contains(filter)) {
                    list.add(player);
                }
            }

            return list;

            // Start with name matching
        } else {
            List<Player> list = new ArrayList<>();

            for (Player player : players) {
                if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(filter) || useDisplayNames
                        && ChatColor.stripColor(player.getDisplayName().toLowerCase(Locale.ENGLISH)).startsWith(filter)) {
                    list.add(player);
                }
            }

            return list;
        }
    }

    /**
     * Checks if the given list of players is greater than size 0, otherwise throw an exception.
     *
     * @param players
     *
     * @return
     *
     * @throws CommandException
     */
    protected static Iterable<? extends Player> checkPlayerMatch(Collection<? extends Player> players) throws CommandException {
        // Check to see if there were any matches
        if (players.isEmpty()) throw new CommandException("No players matched query.");

        return players;
    }

    /**
     * Checks permissions and throws an exception if permission is not met.
     *
     * @param source
     * @param filter
     *
     * @return iterator for players
     *
     * @throws CommandException no matches found
     */
    public static Iterable<? extends Player> matchPlayers(CommandSender source, String filter) throws CommandException {

        if (CraftBookPlugin.server().getOnlinePlayers().size() == 0)
            throw new CommandException("No players matched query.");

        if (filter.equals("*")) return checkPlayerMatch(CraftBookPlugin.server().getOnlinePlayers());

        // Handle special hash tag groups
        if (filter.charAt(0) == '#') // Handle #world, which matches player of the same world as the
            // calling source
            if (filter.equalsIgnoreCase("#world")) {
                List<Player> players = new ArrayList<>();
                Player sourcePlayer = checkPlayer(source);
                World sourceWorld = sourcePlayer.getWorld();

                for (Player player : CraftBookPlugin.server().getOnlinePlayers()) {
                    if (player.getWorld().equals(sourceWorld)) {
                        players.add(player);
                    }
                }

                return checkPlayerMatch(players);

                // Handle #near, which is for nearby players.
            } else if (filter.equalsIgnoreCase("#near")) {
                List<Player> players = new ArrayList<>();
                Player sourcePlayer = checkPlayer(source);

                for (Player player : CraftBookPlugin.server().getOnlinePlayers()) {
                    if (player.getWorld().equals(sourcePlayer.getWorld()) && LocationUtil.getDistanceSquared(player.getLocation(), sourcePlayer.getLocation()) < 900)
                        players.add(player);
                }

                return checkPlayerMatch(players);

            } else throw new CommandException("Invalid group '" + filter + "'.");

        List<Player> players = matchPlayerNames(filter);

        return checkPlayerMatch(players);
    }

    /**
     * Match a single player exactly.
     *
     * @param sender
     * @param filter
     *
     * @return
     *
     * @throws CommandException
     */
    public static Player matchPlayerExactly(CommandSender sender, String filter) throws CommandException {

        Collection<? extends Player> players = CraftBookPlugin.server().getOnlinePlayers();
        for (Player player : players) {
            if (player.getName().equalsIgnoreCase(filter) || player.getDisplayName().equalsIgnoreCase(filter))
                return player;
        }

        throw new CommandException("No player found!");
    }

    /**
     * Match only a single player.
     *
     * @param sender
     * @param filter
     *
     * @return
     *
     * @throws CommandException
     */
    public static Player matchSinglePlayer(CommandSender sender, String filter) throws CommandException {
        // This will throw an exception if there are no matches
        Iterator<? extends Player> players = matchPlayers(sender, filter).iterator();

        Player match = players.next();

        // We don't want to match the wrong person, so fail if if multiple
        // players were found (we don't want to just pick off the first one,
        // as that may be the wrong player)
        if (players.hasNext())
            throw new CommandException("More than one player found! " + "Use @<name> for exact matching.");

        return match;
    }

    /**
     * Match only a single player or console.
     *
     * @param sender
     * @param filter
     *
     * @return
     *
     * @throws CommandException
     */
    public static CommandSender matchPlayerOrConsole(CommandSender sender, String filter) throws CommandException {

        // Let's see if console is wanted
        if (filter.equalsIgnoreCase("#console") || filter.equalsIgnoreCase("*console*") || filter.equals("!")) {
            return CraftBookPlugin.server().getConsoleSender();
        }
        return matchSinglePlayer(sender, filter);
    }

    /**
     * Get a single player as an iterator for players.
     *
     * @param player
     *
     * @return iterator for players
     */
    public static Iterable<Player> matchPlayers(Player player) {

        return Collections.singletonList(player);
    }

    /**
     * Gets the name of a command sender. This may be a display name.
     *
     * @param sender
     *
     * @return
     */
    public static String toName(CommandSender sender) {

        return ChatColor.stripColor(toColoredName(sender, null));
    }

    /**
     * Gets the name of a command sender. This may be a display name.
     *
     * @param sender
     * @param endColor
     *
     * @return
     */
    public static String toColoredName(CommandSender sender, ChatColor endColor) {

        if (sender instanceof Player) {
            String name = ((Player) sender).getDisplayName();
            if (endColor != null && name.contains("\u00A7")) {
                name = name + endColor;
            }
            return name;
        } else if (sender instanceof ConsoleCommandSender) return "*Console*";
        else return sender.getName();
    }

    /**
     * Gets the name of a command sender. This is a unique name and this method should never return a "display name".
     *
     * @param sender
     *
     * @return
     */
    public static String toUniqueName(CommandSender sender) {

        if (sender instanceof Player) return sender.getName();
        else return "*Console*";
    }
}