package com.sk89q.craftbook.mechanics.area;

// $Id$
/*
 * CraftBook Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
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
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Used to load, save, and cache cuboid copies.
 */
public class CopyManager {

    private static CraftBookPlugin plugin = CraftBookPlugin.inst();
    private static final CopyManager INSTANCE = new CopyManager();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Cache.
     */
    private final HistoryHashMap<String, BlockArrayClipboard> cache = new HistoryHashMap<>(10);

    /**
     * Remembers missing copies so as to not look for them on disk.
     */
    private final HistoryHashMap<String, Long> missing = new HistoryHashMap<>(10);

    /**
     * Gets the copy manager instance
     *
     * @return The Copy Manager Instance
     */
    public static CopyManager getInstance() {

        return INSTANCE;
    }

    /**
     * Checks to see whether a name is a valid copy name.
     *
     * @param name Checks if it's a valid schematic name
     *
     * @return If it's valid
     */
    public static boolean isValidName(String name) {

        // name needs to be between 1 and 13 letters long so we can fit the
        return !name.isEmpty() && name.length() <= 13 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks to see whether a name is a valid namespace.
     *
     * @param name Checks if it's a valid namespace
     *
     * @return If it's valid
     */
    public static boolean isValidNamespace(String name) {

        return !name.isEmpty() && name.length() <= 14 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Renames a namespace.
     * 
     * @param originalName The old name.
     * @param newName The new name. (Post rename)
     */
    public static void renameNamespace(File dataFolder, String originalName, String newName) {

        File oldDir = new File(dataFolder, "areas/" + originalName);
        File newDir = new File(dataFolder, "areas/" + newName);
        if (oldDir.isDirectory()) {
            oldDir.renameTo(newDir);
        } else {
            oldDir.mkdir();
            oldDir.renameTo(newDir);
        }
    }

    /**
     * Checks if the area and namespace exists.
     *
     * @param namespace to check
     * @param area      to check
     */
    public static boolean isExistingArea(File dataFolder, String namespace, String area) {
        area = StringUtils.replace(area, "-", "");
        File file = new File(dataFolder, "areas/" + namespace);
        if (!new File(file, area + getFileSuffix()).exists()) {
            return new File(file, area + '.' + BuiltInClipboardFormat.MCEDIT_SCHEMATIC.getPrimaryFileExtension()).exists();
        } else {
            return true;
        }
    }

    /**
     * Load a copy from disk. This may return a cached copy. If the copy is not cached,
     * the file will be loaded from disk if possible. If the copy
     * does not exist, an exception will be raised. An exception may be raised if the file exists but cannot be read
     * for whatever reason.
     *
     * @param namespace The clipboard namespace
     * @param id The clipboard ID
     *
     * @return The loaded clipboard
     *
     * @throws IOException If it fails to load
     */
    public BlockArrayClipboard load(String namespace, String id) throws IOException {

        id = id.toLowerCase(Locale.ENGLISH);
        String cacheKey = namespace + '/' + id;

        if (missing.containsKey(cacheKey)) {
            long lastCheck = missing.get(cacheKey);
            if (lastCheck > System.currentTimeMillis()) throw new FileNotFoundException(id);
        }

        BlockArrayClipboard copy = cache.get(cacheKey);

        if (copy == null) {
            File file = new File(new File(new File(plugin.getDataFolder(), "areas"), namespace), id + getFileSuffix());
            if (!file.exists()) {
                file = new File(new File(new File(plugin.getDataFolder(), "areas"), namespace),
                        id + '.' + BuiltInClipboardFormat.MCEDIT_SCHEMATIC.getPrimaryFileExtension());
            }
            if (file.exists()) {
                ClipboardFormat format = ClipboardFormats.findByFile(file);
                if (format == null) {
                    missing.put(cacheKey, System.currentTimeMillis());
                    throw new IOException("Unknown clipboard format!");
                }
                try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                    copy = (BlockArrayClipboard) reader.read();
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
     *
     * @throws IOException If the file failed to save
     */
    public void save(String namespace, String id, BlockArrayClipboard clipboard) throws IOException {
        File folder = new File(new File(plugin.getDataFolder(), "areas"), namespace);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        id = id.toLowerCase(Locale.ENGLISH);

        String cacheKey = namespace + '/' + id;

        File file = new File(folder, id + getFileSuffix());
        try (ClipboardWriter writer = getDefaultClipboardFormat().getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        }
        missing.remove(cacheKey);
        cache.put(cacheKey, clipboard);
    }

    /**
     * Copies a region into the BlockArrayClipboard.
     *
     * @param region The region
     * @return The BlockArrayClipboard
     * @throws WorldEditException If something went wrong.
     */
    public BlockArrayClipboard copy(Region region) throws WorldEditException {
        return copy(region, false, false);
    }

    /**
     * Copies a region into the BlockArrayClipboard.
     *
     * @param region The region
     * @return The BlockArrayClipboard
     * @throws WorldEditException If something went wrong.
     */
    public BlockArrayClipboard copy(Region region, boolean copyEntities, boolean copyBiomes) throws WorldEditException {
        BlockArrayClipboard copy = new BlockArrayClipboard(region);
//        copy.setOrigin(origin);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1);

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
    public void paste(BlockArrayClipboard clipboard) throws WorldEditException {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(clipboard.getRegion().getWorld(), -1)) {

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
     * Clears the area a clipboard can inhabit
     *
     * @param clipboard The clipboard
     */
    public void clear(BlockArrayClipboard clipboard) {
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(clipboard.getRegion().getWorld(), -1)) {
            editSession.setBlocks(clipboard.getRegion(), BlockTypes.AIR.getDefaultState());
        } catch (MaxChangedBlocksException e) {
            // is never thrown
        }
    }

    /**
     * Gets whether a copy can be made.
     *
     * @param namespace The clipboard namespace
     * @param ignore File name to ignore
     * @param quota The limit of tiels
     *
     * @return -1 if the copy can be made, some other number for the count
     */
    public static int meetsQuota(String namespace, String ignore, int quota) {

        String ignoreFilename = ignore + getFileSuffix();

        String[] files = new File(new File(plugin.getDataFolder(), "areas"), namespace).list();

        if (files == null) return quota > 0 ? -1 : 0;
        else if (ignore == null) return files.length < quota ? -1 : files.length;
        else {
            int count = 0;

            for (String f : files) {
                if (f.equals(ignoreFilename)) return -1;

                count++;
            }

            return count < quota ? -1 : count;
        }
    }

    private static ClipboardFormat getDefaultClipboardFormat() {
        return BuiltInClipboardFormat.SPONGE_SCHEMATIC;
    }

    private static String getFileSuffix() {
        return '.' + getDefaultClipboardFormat().getPrimaryFileExtension();
    }
}