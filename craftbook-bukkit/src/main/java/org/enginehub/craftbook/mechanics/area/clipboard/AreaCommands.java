/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.area.clipboard;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.exception.CraftBookException;
import org.enginehub.craftbook.mechanics.area.clipboard.AreaCommandsRegistration;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class AreaCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration) {
        registration.register(
            commandManager,
            AreaCommandsRegistration.builder(),
            new AreaCommands()
        );
    }

    public AreaCommands() {
    }

    private final CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(name = "save", desc = "Saves the selected area")
    public void saveArea(CraftBookPlayer player,
                         @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                         @Arg(desc = "The area name") String name,
                         @Switch(name = 'b', desc = "Save biomes") boolean saveBiomes,
                         @Switch(name = 'e', desc = "Save entities") boolean saveEntities
    ) throws AuthorizationException, CraftBookException {
        boolean personal = true;

        if (namespace != null) {
            if (!player.hasPermission("craftbook.mech.area.save." + namespace))
                throw new CraftBookException("You do not have permission to use this namespace.");
            personal = false;
        } else {
            if (!player.hasPermission("craftbook.mech.area.save.self")) {
                throw new AuthorizationException();
            }

            namespace = player.getCraftBookId();
        }

        if (Area.instance.shortenNames && namespace.length() > 14)
            namespace = namespace.substring(0, 14);

        if (!CopyManager.isValidNamespace(namespace))
            throw new CraftBookException("Invalid namespace. Needs to be between 1 and 14 letters long.");

        if (personal) {
            namespace = '~' + namespace;
        }

        if (!CopyManager.isValidName(name))
            throw new CraftBookException("Invalid area name. Needs to be between 1 and 13 letters long.");

        try {
            com.sk89q.worldedit.world.World world = player.getWorld();
            Region sel = WorldEdit.getInstance().getSessionManager().findByName(player.getName()).getSelection(world);
            if (sel == null) {
                player.printError("You have not made a selection!");
                return;
            }
            BlockVector3 min = sel.getMinimumPoint();
            BlockVector3 max = sel.getMaximumPoint();
            BlockVector3 size = max.subtract(min).add(1, 1, 1);

            // Check maximum size
            if (Area.instance.maxAreaSize != -1 && size.getBlockX() * size.getBlockY() * size.getBlockZ()
                > Area.instance.maxAreaSize) {
                throw new CraftBookException("Area is larger than allowed " + Area.instance.maxAreaSize + " blocks.");
            }

            // Check to make sure that a user doesn't have too many toggle
            // areas (to prevent flooding the server with files)
            if (Area.instance.maxAreasPerUser >= 0 && !namespace.equals("global") && !player.hasPermission("craftbook.mech.area.bypass-limit")) {
                int count = CopyManager.meetsQuota(namespace, name,
                    Area.instance.maxAreasPerUser);

                if (count > -1) {
                    throw new CraftBookException("You are limited to " + Area.instance.maxAreasPerUser + " toggle area(s). "
                        + "You have " + count + " areas.");
                }
            }

            // Copy
            BlockArrayClipboard copy = CopyManager.getInstance().copy(sel, world, saveEntities, saveBiomes);

            plugin.getServer().getLogger().info(player.getName() + " saving toggle area with folder '" + namespace +
                "' and ID '" + name + "'.");

            // Save
            try {
                CopyManager.getInstance().save(namespace, name.toLowerCase(Locale.ENGLISH), copy);
                player.print("Area saved as '" + name + "' under the '" + namespace + "' namespace.");
            } catch (IOException e) {
                player.printError("Could not save area: " + e.getMessage());
            }
        } catch (NoClassDefFoundError e) {
            throw new CraftBookException("WorldEdit.jar does not exist in plugins/, or is outdated. (Or you are using an outdated version of CraftBook)");
        } catch (IncompleteRegionException e) {
            throw new CraftBookException("Invalid selection");
        } catch (WorldEditException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(name = "list", desc = "Lists the areas of the given namespace or lists all areas.")
    public void list(Actor actor,
                     @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                     @Switch(name = 'a', desc = "List from all namespaces") boolean listAll,
                     @Arg(desc = "The page", def = "1") int page
    ) throws AuthorizationException, CraftBookException {
        // get the namespace from the flag (if set)
        if (namespace != null) {
            if (!actor.hasPermission("craftbook.mech.area.list." + namespace))
                throw new AuthorizationException("You do not have permission to use this namespace.");
        } else if (listAll && actor.hasPermission("craftbook.mech.area.list.all")) {
            namespace = "";
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.mech.area.list.self")) {
                throw new AuthorizationException();
            }
            namespace = "~" + ((CraftBookPlayer) actor).getCraftBookId();
        } else {
            throw new CraftBookException("Must supply a player");
        }

        if (Area.instance.shortenNames && namespace.length() > 15)
            namespace = namespace.substring(0, 15);

        // get the areas for the defined namespace
        File areas = new File(CraftBookPlugin.inst().getDataFolder(), "areas");

        if (!areas.exists()) throw new CraftBookException("There are no saved areas.");

        File folder = null;
        if (!namespace.isEmpty()) {
            folder = new File(areas, namespace);
        }

        if (folder != null && !folder.exists())
            throw new CraftBookException("The namespace '" + namespace + "' does not exist.");

        List<String> areaList = new ArrayList<>();

        FilenameFilter fnf = (dir, name) -> Area.instance.useSchematics
            ? name.endsWith(".schematic") || name.endsWith(".schem")
            : name.endsWith(".cbcopy");

        if (folder != null && folder.exists()) {
            for (File area : folder.listFiles(fnf)) {
                String areaName = area.getName();
                areaName = areaName.replace(".schematic", "");
                areaName = areaName.replace(".schem", "");
                areaName = areaName.replace(".cbcopy", "");
                areaList.add(ChatColor.AQUA + folder.getName() + "   :   " + ChatColor.YELLOW + areaName);
            }
        } else {
            for (File file : areas.listFiles()) {
                if (file.isDirectory()) {
                    for (File area : file.listFiles(fnf)) {
                        String areaName = area.getName();
                        areaName = areaName.replace(".schematic", "");
                        areaName = areaName.replace(".schem", "");
                        areaName = areaName.replace(".cbcopy", "");
                        areaList.add(ChatColor.AQUA + folder.getName() + "   :   " + ChatColor.YELLOW + areaName);
                    }
                }
            }
        }

        // now lets list the areas with a nice pagination
        if (!areaList.isEmpty()) {
            String tmp = namespace.isEmpty() ? "All Areas " : "Areas for " + namespace;
            actor.print(ChatColor.GREEN + tmp + " - Page " + Math.abs(page) + " of " + (areaList.size() / 8 + 1));
            // list the areas one by one
//            FIXME for (String str : ArrayUtil.getArrayPage(areaList, page)) {
//                if (str != null && !str.isEmpty()) {
//                    actor.print(str);
//                }
//            }
        } else {
            actor.printError("There are no saved areas in the '" + namespace + "' namespace.");
        }
    }

    @Command(name = "toggle", desc = "Toggle an area sign at the given location.")
    @CommandPermissions("craftbook.mech.area.command.toggle")
    public void toggle(Actor actor,
                       @ArgFlag(name = 'w', desc = "The world") World world,
                       @Arg(desc = "The location") BlockVector3 position,
                       @Switch(name = 's', desc = "Silence output") boolean silent
    ) throws CraftBookException {
        if (world == null && actor instanceof CraftBookPlayer) {
            world = BukkitAdapter.adapt(((CraftBookPlayer) actor).getWorld());
        }

        if (world == null) {
            throw new CraftBookException("You must be a player or specify a valid world to use this command.");
        }

        Block block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
        if (!SignUtil.isSign(block))
            throw new CraftBookException("No sign found at the specified location.");

        if (!Area.toggleCold(block)) {
            throw new CraftBookException("Failed to toggle an area at the specified location.");
        }
        if (!silent) {
            actor.print("Area toggled!");
        }
    }

    @Command(name = "delete", desc = "Lists the areas of the given namespace or lists all areas.")
    public void delete(Actor actor,
                       @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                       @Arg(desc = "The area name") String name
    ) throws AuthorizationException, CraftBookException {
        // Get the namespace
        if (namespace != null) {
            if (!actor.hasPermission("craftbook.mech.area.delete." + namespace))
                throw new CraftBookException("You do not have permission to use this namespace.");
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.mech.area.delete.self"))
                throw new AuthorizationException();
            namespace = "~" + ((CraftBookPlayer) actor).getCraftBookId();
        } else {
            throw new CraftBookException("Must provide a player");
        }

        if (Area.instance.shortenNames && namespace.length() > 15)
            namespace = namespace.substring(0, 15);

        File areas = null;
        try {
            areas = new File(plugin.getDataFolder(), "areas/" + namespace);
        } catch (Exception ignored) {
        }

        if (areas == null || !areas.exists())
            throw new CraftBookException("The namespace " + namespace + " does not exist.");

        // add the area suffix
        String[] possibleFilenames = { name + ".schematic", name + ".schem", name + ".cbcopy" };

        for (String filename : possibleFilenames) {
            File file = new File(areas, filename);
            if (file.exists()) {
                if (file.delete()) {
                    actor.print("The area '" + name + " in the namespace '" + namespace + "' has been deleted.");
                }
                break;
            }
        }
    }

    @Command(name = "delete-all", desc = "Deletes all the areas in a namespace.")
    public void delete(Actor actor,
                       @Arg(desc = "The namespace", variable = true) String namespace
    ) throws AuthorizationException, CraftBookException {
        String areaId = null;

        // Get the namespace
        if (namespace != null) {
            if (!actor.hasPermission("craftbook.mech.area.delete." + namespace + ".all"))
                throw new CraftBookException("You do not have permission to use this namespace.");
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.mech.area.delete.self.all"))
                throw new AuthorizationException();
            namespace = "~" + ((CraftBookPlayer) actor).getCraftBookId();
        } else {
            throw new CraftBookException("Must provide a player");
        }

        if (Area.instance.shortenNames && namespace.length() > 15)
            namespace = namespace.substring(0, 15);

        File areas = null;
        try {
            areas = new File(plugin.getDataFolder(), "areas/" + namespace);
        } catch (Exception ignored) {
        }

        if (areas == null || !areas.exists())
            throw new CraftBookException("The namespace " + namespace + " does not exist.");

        if (deleteDir(areas)) {
            actor.print("All areas in the namespace " + namespace + " have been deleted.");
        }
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private boolean deleteDir(File dir) {

        FilenameFilter fnf = (dir1, name) -> Area.instance.useSchematics
            ? name.endsWith(".schematic") || name.endsWith(".schem")
            : name.endsWith(".cbcopy");

        if (dir.isDirectory()) {
            for (File aChild : dir.listFiles(fnf)) {
                if (!aChild.delete()) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }
}
