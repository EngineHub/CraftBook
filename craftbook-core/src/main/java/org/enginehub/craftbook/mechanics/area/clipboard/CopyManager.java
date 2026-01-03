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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.mechanic.MechanicTypes;
import org.enginehub.craftbook.util.HistoryHashMap;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Used to load, save, and cache cuboid copies.
 */
public class CopyManager {
    private static final CopyManager INSTANCE = new CopyManager();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

    public static final int MAX_NAMESPACE_LENGTH = 15;
    public static final int MAX_AREA_NAME_LENGTH = 13;

    /**
     * Cache.
     */
    private final HistoryHashMap<String, Clipboard> cache = new HistoryHashMap<>(10);

    /**
     * Remembers missing copies so as to not look for them on disk.
     */
    private final HistoryHashMap<String, Long> missing = new HistoryHashMap<>(10);

    /**
     * Gets the copy manager instance.
     *
     * @return The Copy Manager Instance
     */
    public static CopyManager getInstance() {
        return INSTANCE;
    }

    private ToggleArea getToggleAreaInstance() {
        return CraftBook.getInstance().getPlatform().getMechanicManager().getMechanic(MechanicTypes.TOGGLE_AREA.get()).get();
    }

    public static Path getAreaPath() {
        return CraftBook.getInstance().getPlatform().getWorkingDirectory().resolve("areas");
    }

    /**
     * Checks to see whether a name is a valid copy name.
     *
     * @param name Checks if it's a valid schematic name
     * @return If it's valid
     */
    public static boolean isValidName(String name) {
        // name needs to be between 1 and 13 letters long so we can fit the -- around the name
        return !name.isEmpty() && name.length() <= MAX_AREA_NAME_LENGTH && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks to see whether a name is a valid namespace.
     *
     * @param name Checks if it's a valid namespace
     * @return If it's valid
     */
    public static boolean isValidNamespace(String name) {
        return !name.isEmpty() && name.length() <= MAX_NAMESPACE_LENGTH && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks if the area and namespace exists.
     *
     * @param namespace to check
     * @param area to check
     */
    public static boolean isExistingArea(String namespace, String area) {
        area = area.replace("-", "");
        var namespacePath = getAreaPath().resolve(namespace);
        if (!Files.exists(namespacePath.resolve(area + getDefaultFileSuffix()))) {
            boolean found = false;
            for (String extension : ClipboardFormats.getFileExtensionArray()) {
                if (Files.exists(namespacePath.resolve(area + "." + extension))) {
                    found = true;
                    break;
                }
            }
            return found;
        } else {
            return true;
        }
    }

    /**
     * Load a copy from disk. This may return a cached copy. If the copy is not cached,
     * the file will be loaded from disk if possible. If the copy
     * does not exist, an exception will be raised. An exception may be raised if the file exists
     * but cannot be read
     * for whatever reason.
     *
     * @param namespace The clipboard namespace
     * @param id The clipboard ID
     * @return The loaded clipboard
     * @throws IOException If it fails to load
     */
    public Clipboard load(String namespace, String id) throws IOException {
        id = id.toLowerCase(Locale.ENGLISH);
        String cacheKey = namespace + '/' + id;

        if (missing.containsKey(cacheKey)) {
            long lastCheck = missing.get(cacheKey);
            // Assume the file is still missing if it was checked within 60 seconds
            if (lastCheck > System.currentTimeMillis() - 1000 * 60) {
                throw new FileNotFoundException(id);
            }
        }

        Clipboard copy = cache.get(cacheKey);

        if (copy == null) {
            var namespacePath = getAreaPath().resolve(namespace);

            Path path = namespacePath.resolve(id + getDefaultFileSuffix());
            if (!Files.exists(path)) {
                for (String extension : ClipboardFormats.getFileExtensionArray()) {
                    path = namespacePath.resolve(id + "." + extension);
                    if (Files.exists(path)) {
                        break;
                    }
                }
            }
            if (Files.exists(path)) {
                ClipboardFormat format = ClipboardFormats.findByPath(path);
                if (format == null) {
                    missing.put(cacheKey, System.currentTimeMillis());
                    throw new IOException("Unknown clipboard format!");
                }
                try (ClipboardReader reader = format.getReader(Files.newInputStream(path))) {
                    copy = reader.read();
                    missing.remove(cacheKey);
                    cache.put(cacheKey, copy);
                    return copy;
                }
            } else {
                missing.put(cacheKey, System.currentTimeMillis());
                throw new FileNotFoundException(id);
            }
        }

        return copy;
    }

    /**
     * Save a copy to disk. The copy will be cached.
     *
     * @param namespace The save namespace
     * @param id The save id
     * @param clipboard The clipboard containing the save
     * @throws IOException If the file failed to save
     */
    public void save(String namespace, String id, Clipboard clipboard) throws IOException {
        var namespacePath = getAreaPath().resolve(namespace);

        if (!Files.exists(namespacePath)) {
            Files.createDirectories(namespacePath);
        }

        id = id.toLowerCase(Locale.ENGLISH);

        String cacheKey = namespace + '/' + id;

        Path path = namespacePath.resolve(id + getDefaultFileSuffix());
        try (ClipboardWriter writer = getDefaultClipboardFormat().getWriter(Files.newOutputStream(path))) {
            writer.write(clipboard);
        }
        missing.remove(cacheKey);
        cache.put(cacheKey, clipboard);
    }

    /**
     * Copies a region into the clipboard.
     *
     * @param region The region
     * @return The clipboard
     * @throws WorldEditException If something went wrong.
     */
    public Clipboard copy(Region region, World world) throws WorldEditException {
        return copy(region, world, false, false);
    }

    /**
     * Copies a region into the clipboard.
     *
     * @param region The region
     * @return The clipboard
     * @throws WorldEditException If something went wrong.
     */
    public Clipboard copy(Region region, World world, boolean copyEntities, boolean copyBiomes) throws WorldEditException {
        Clipboard copy = new BlockArrayClipboard(region);

        EditSession editSession = WorldEdit.getInstance().newEditSession(world);
        editSession.setTrackingHistory(false);

        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, copy, region.getMinimumPoint());
        forwardExtentCopy.setCopyingEntities(copyEntities);
        forwardExtentCopy.setCopyingBiomes(copyBiomes);
        Operations.complete(forwardExtentCopy);

        return copy;
    }

    /**
     * Pastes the clipboard into the world.
     *
     * @param clipboard The clipboard
     * @throws WorldEditException If it fails
     */
    public void paste(Clipboard clipboard, World world) throws WorldEditException {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            editSession.setTrackingHistory(false);

            if (getToggleAreaInstance().removeEntitiesOnToggle) {
                editSession.getEntities(clipboard.getRegion()).forEach(Entity::remove);
            }

            Operation operation = new ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(clipboard.getOrigin())
                .copyBiomes(true)
                .copyEntities(true)
                .ignoreAirBlocks(false)
                .build();

            Operations.complete(operation);
        }
    }

    /**
     * Clears the area a clipboard can inhabit.
     *
     * @param clipboard The clipboard
     */
    public void clear(Clipboard clipboard, World world) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            editSession.setTrackingHistory(false);
            editSession.setBlocks(clipboard.getRegion(), BlockTypes.AIR.getDefaultState());
            if (getToggleAreaInstance().removeEntitiesOnToggle) {
                editSession.getEntities(clipboard.getRegion()).forEach(Entity::remove);
            }
        } catch (MaxChangedBlocksException e) {
            // is never thrown
        }
    }

    /**
     * Gets whether a copy can be made.
     *
     * @param namespace The clipboard namespace
     * @param ignore File name to ignore
     * @param quota The limit of blocks
     * @return -1 if the copy can be made, some other number for the count
     */
    public static int meetsQuota(String namespace, @Nullable String ignore, int quota) {
        String[] files = new File(getAreaPath().toFile(), namespace).list();

        if (files == null) {
            return quota > 0 ? -1 : 0;
        } else if (ignore == null) {
            return files.length < quota ? -1 : files.length;
        } else {
            int count = 0;

            for (String f : files) {
                if (f.substring(0, f.lastIndexOf('.')).equals(ignore)) {
                    return -1;
                }

                count++;
            }

            return count < quota ? -1 : count;
        }
    }

    private static ClipboardFormat getDefaultClipboardFormat() {
        return BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC;
    }

    private static String getDefaultFileSuffix() {
        return '.' + getDefaultClipboardFormat().getPrimaryFileExtension();
    }
}
