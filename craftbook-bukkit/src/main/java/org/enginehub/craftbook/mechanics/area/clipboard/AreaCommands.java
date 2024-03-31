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
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.component.InvalidComponentException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.SignUtil;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class AreaCommands {

    public static void register(CommandManager commandManager, CommandRegistrationHandler registration, ToggleArea toggleArea) {
        registration.register(
            commandManager,
            AreaCommandsRegistration.builder(),
            new AreaCommands(toggleArea)
        );
    }

    private final ToggleArea toggleArea;

    private AreaCommands(ToggleArea toggleArea) {
        this.toggleArea = toggleArea;
    }

    private final CraftBookPlugin plugin = CraftBookPlugin.inst();

    @Command(name = "save", desc = "Saves the selected area")
    @CommandPermissions({ "craftbook.togglearea.save" })
    public void save(CraftBookPlayer player,
                         @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                         @Arg(desc = "The area name") String name,
                         @Switch(name = 'b', desc = "Save biomes") boolean saveBiomes,
                         @Switch(name = 'e', desc = "Save entities") boolean saveEntities
    ) throws AuthorizationException {
        boolean personal = true;

        if (namespace != null && !namespace.equalsIgnoreCase("self")) {
            if (!player.hasPermission("craftbook.togglearea.save." + namespace)) {
                throw new AuthorizationException(TranslatableComponent.of("craftbook.togglearea.namespace-permissions", TextComponent.of(namespace, TextColor.DARK_PURPLE)));
            }

            personal = false;
        } else {
            if (!player.hasPermission("craftbook.togglearea.save.self")) {
                throw new AuthorizationException();
            }

            namespace = player.getUniqueId().toString();
        }

        // Non-personal namespaces are user-entered and require further validation
        if (!personal && !CopyManager.isValidNamespace(namespace)) {
            player.printError(TranslatableComponent.of("craftbook.togglearea.invalid-namespace"));
            return;
        }

        if (!CopyManager.isValidName(name)) {
            player.printError(TranslatableComponent.of("craftbook.togglearea.invalid-area-name"));
            return;
        }

        try {
            com.sk89q.worldedit.world.World world = player.getWorld();
            Region sel = WorldEdit.getInstance().getSessionManager().get(player).getSelection(world);
            if (sel == null) {
                player.printError(TranslatableComponent.of("craftbook.togglearea.missing-selection"));
                return;
            }

            // Check maximum size
            if (this.toggleArea.maxAreaSize != -1 && sel.getVolume() > this.toggleArea.maxAreaSize) {
                player.printError(TranslatableComponent.of("craftbook.togglearea.selection-too-large", TextComponent.of(this.toggleArea.maxAreaSize)));
                return;
            }

            // Check to make sure that a user doesn't have too many toggle
            // areas (to prevent flooding the server with files)
            if (personal && this.toggleArea.maxAreasPerUser >= 0 && !player.hasPermission("craftbook.togglearea.bypass-area-limit")) {
                int count = CopyManager.meetsQuota(namespace, name, this.toggleArea.maxAreasPerUser);

                if (count > -1) {
                    player.printError(TranslatableComponent.of("craftbook.togglearea.too-many-areas", TextComponent.of(this.toggleArea.maxAreasPerUser), TextComponent.of(count)));
                    return;
                }
            }

            // Copy
            BlockArrayClipboard copy = CopyManager.getInstance().copy(sel, world, saveEntities, saveBiomes);

            CraftBook.LOGGER.info(player.getName() + " saving toggle area with folder '" + namespace + "' and ID '" + name + "'.");

            // Save
            try {
                CopyManager.getInstance().save(namespace, name.toLowerCase(Locale.ENGLISH), copy);
                player.printInfo(TranslatableComponent.of("craftbook.togglearea.saved", TextComponent.of(name), TextComponent.of(namespace)));
            } catch (IOException e) {
                player.printError(TranslatableComponent.of("craftbook.togglearea.save-failed", TextComponent.of(e.getMessage())));
            }
        } catch (IncompleteRegionException e) {
            player.printError(TranslatableComponent.of("craftbook.togglearea.missing-selection"));
        } catch (WorldEditException e) {
            player.printError(e.getRichMessage());
        }
    }

    @Command(name = "list", desc = "Lists the areas of the given namespace or lists all areas.")
    @CommandPermissions({ "craftbook.togglearea.list" })
    public void list(Actor actor,
                     @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                     @Switch(name = 'a', desc = "List from all namespaces") boolean listAll,
                     @ArgFlag(name = 'p', desc = "The page", def = "1") int page
    ) throws AuthorizationException {
        boolean personal = false;
        if (namespace != null) {
            if (!actor.hasPermission("craftbook.togglearea.list." + namespace)) {
                throw new AuthorizationException(TranslatableComponent.of("craftbook.togglearea.namespace-permissions", TextComponent.of(namespace)));
            }
        } else if (listAll && actor.hasPermission("craftbook.togglearea.list.all")) {
            namespace = "";
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.togglearea.list.self")) {
                throw new AuthorizationException();
            }
            namespace = actor.getUniqueId().toString();
            personal = true;
        } else {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.player-or-namespace-required"));
            return;
        }

        // get the areas for the defined namespace
        Path areasPath = CraftBookPlugin.inst().getDataFolder().toPath().resolve("areas");

        if (!Files.exists(areasPath) || !Files.isDirectory(areasPath)) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.no-areas"));
            return;
        }

        if (!namespace.isEmpty()) {
            areasPath = areasPath.resolve(namespace);

            if (!Files.exists(areasPath) || !Files.isDirectory(areasPath)) {
                actor.printError(TranslatableComponent.of("craftbook.togglearea.unknown-namespace", TextComponent.of(namespace, TextColor.DARK_PURPLE)));
                return;
            }
        }

        try (var pathStream = Files.walk(areasPath)) {
            List<Path> areaList = pathStream.filter(SCHEMATIC_FILTER::matches).toList();

            if (!areaList.isEmpty()) {
                showListBox(actor, areaList, page, personal ? "" : namespace, listAll);
            } else {
                actor.printError(TranslatableComponent.of("craftbook.togglearea.no-areas-namespace", TextComponent.of(namespace)));
            }
        } catch (IOException | InvalidComponentException e) {
            throw new RuntimeException(e);
        }
    }

    @Command(name = "toggle", desc = "Toggle an area sign at the given location.")
    @CommandPermissions("craftbook.togglearea.toggle-command")
    public void toggle(Actor actor,
                       @ArgFlag(name = 'w', desc = "The world") World world,
                       @Arg(desc = "The location") BlockVector3 position,
                       @Switch(name = 's', desc = "Silence output") boolean silent
    ) {
        if (world == null && actor instanceof CraftBookPlayer player) {
            world = BukkitAdapter.adapt(player.getWorld());
        }

        if (world == null) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.player-or-world-required"));
            return;
        }

        Block block = world.getBlockAt(position.x(), position.y(), position.z());
        if (!SignUtil.isSign(block)) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.toggle.no-sign"));
            return;
        }

        if (!this.toggleArea.toggleCold(block)) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.toggle-failed"));
            return;
        }

        if (!silent) {
            actor.printInfo(TranslatableComponent.of("craftbook.togglearea.toggled"));
        }
    }

    @Command(name = "delete", desc = "Lists the areas of the given namespace or lists all areas.")
    @CommandPermissions({ "craftbook.togglearea.delete" })
    public void delete(Actor actor,
                       @ArgFlag(name = 'n', desc = "The namespace") String namespace,
                       @Arg(desc = "The area name") String name
    ) throws AuthorizationException {
        // Get the namespace
        if (namespace != null && !namespace.equalsIgnoreCase("self")) {
            if (!actor.hasPermission("craftbook.togglearea.delete." + namespace)) {
                actor.printError(TranslatableComponent.of("craftbook.togglearea.namespace-permissions", TextComponent.of(namespace)));
                return;
            }
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.togglearea.delete.self")) {
                throw new AuthorizationException();
            }
            namespace = actor.getUniqueId().toString();
        } else {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.player-or-namespace-required"));
            return;
        }

        Path areasPath = plugin.getDataFolder().toPath().resolve("areas").resolve(namespace);

        if (!Files.exists(areasPath) || !Files.isDirectory(areasPath)) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.unknown-namespace", TextComponent.of(namespace)));
            return;
        }

        // add the area suffix
        List<String> possibleFilenames = Arrays.stream(ClipboardFormats.getFileExtensionArray()).map(ext -> name + "." + ext).toList();

        for (String filename : possibleFilenames) {
            Path areaPath = areasPath.resolve(filename);
            if (Files.exists(areaPath)) {
                try {
                    Files.delete(areaPath);
                    actor.printInfo(TranslatableComponent.of("craftbook.togglearea.deleted-area", TextComponent.of(name), TextComponent.of(namespace)));
                } catch (IOException e) {
                    actor.printError(TranslatableComponent.of("craftbook.togglearea.failed-delete", TextComponent.of(name), TextComponent.of(namespace)));
                    return;
                }
                break;
            }
        }
    }

    @Command(name = "delete-all", desc = "Deletes all the areas in a namespace.")
    @CommandPermissions({ "craftbook.togglearea.delete" })
    public void deleteAll(Actor actor,
                       @Arg(desc = "The namespace", variable = true) String namespace
    ) throws AuthorizationException {
        // Get the namespace
        if (namespace != null && !namespace.equalsIgnoreCase("self")) {
            if (!actor.hasPermission("craftbook.togglearea.delete." + namespace + ".all")) {
                actor.printError(TranslatableComponent.of("craftbook.togglearea.namespace-permissions", TextComponent.of(namespace)));
                return;
            }
        } else if (actor instanceof CraftBookPlayer) {
            if (!actor.hasPermission("craftbook.togglearea.delete.self.all")) {
                throw new AuthorizationException();
            }
            namespace = actor.getUniqueId().toString();
        } else {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.player-or-namespace-required"));
            return;
        }

        Path areasPath = plugin.getDataFolder().toPath().resolve("areas").resolve(namespace);

        if (!Files.exists(areasPath) || !Files.isDirectory(areasPath)) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.unknown-namespace", TextComponent.of(namespace)));
            return;
        }

        try {
            deleteDir(areasPath);
            actor.printInfo(TranslatableComponent.of("craftbook.togglearea.deleted-all-in-namespace", TextComponent.of(namespace)));
        } catch (IOException e) {
            actor.printError(TranslatableComponent.of("craftbook.togglearea.failed-delete-all", TextComponent.of(namespace)));
        }
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private void deleteDir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (var listStream = Files.list(path)) {
                var children = listStream.filter(SCHEMATIC_FILTER::matches).toList();
                for (Path child : children) {
                    Files.delete(child);
                }
            }
        }

        // The directory is now empty so delete it
        Files.delete(path);
    }

    private static final PathMatcher SCHEMATIC_FILTER = (path) -> {
        String[] extensions = ClipboardFormats.getFileExtensionArray();
        boolean found = false;
        String filename = path.getFileName().toString();
        for (String extension : extensions) {
            if (filename.endsWith("." + extension)) {
                found = true;
                break;
            }
        }
        return found;
    };

    private void showListBox(Actor actor, List<Path> areaPaths, int page, String namespace, boolean showAll) throws InvalidComponentException {
        AreaListBox areaListBox = new AreaListBox(actor, areaPaths, namespace, showAll);

        actor.print(areaListBox.create(page));
    }
}
