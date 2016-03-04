package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.worldedit.world.DataException;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

public class CopyManager {

    private static final CopyManager INSTANCE = new CopyManager();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

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
     * @param name The name to check
     *
     * @return If it is a valid name
     */
    public static boolean isValidName(String name) {

        // name needs to be between 1 and 13 letters long so we can fit the
        return !name.isEmpty() && name.length() <= 13 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks if the area and namespace exists.
     *
     * @param namespace to check
     * @param area to check
     */
    public static boolean isExistingArea(File dataFolder, String namespace, String area) {
        area = StringUtils.replace(area, "-", "") + getFileSuffix();
        File file = new File(dataFolder, "areas/" + namespace);
        return new File(file, area).exists();
    }

    /**
     * Load a copy from disk. This may return a cached copy. If the copy is not cached,
     * the file will be loaded from disk if possible. If the copy
     * does not exist, an exception will be raised. An exception may be raised if the file exists but cannot be read
     * for whatever reason.
     *
     * @param world
     * @param namespace
     * @param id
     *
     * @return
     *
     * @throws IOException
     * @throws CuboidCopyException
     */
    public CuboidCopy load(World world, String namespace, String id) throws IOException, CuboidCopyException {

        id = id.toLowerCase(Locale.ENGLISH);

        File folder = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace);
        CuboidCopy copy = CuboidCopy.load(new File(folder, id + getFileSuffix()), world);
        return copy;
    }

    /**
     * Save a copy to disk. The copy will be cached.
     *
     * @param namespace
     * @param id
     * @param copy
     *
     * @throws IOException
     */
    public void save(String namespace, String id, CuboidCopy copy) throws IOException, DataException {

        File folder = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        id = id.toLowerCase(Locale.ENGLISH);

        copy.save(new File(folder, id + getFileSuffix()));
    }

    private static String getFileSuffix() {
        return ".schematic";
    }
}