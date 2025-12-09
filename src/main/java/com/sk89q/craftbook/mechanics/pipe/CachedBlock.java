package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

    public static final int NULL_SENTINEL = (1 << 31);

    private static final BlockFace[] BLOCK_FACE_VALUES = BlockFace.values();

    private static int[] presetByOffsetMaterialOrdinal;
    private static int presetMaterialOrdinalOffset;

    public static boolean isPane(int cachedBlock) {
        return (cachedBlock & 1) != 0;
    }

    public static boolean hasHandledInputInventory(int cachedBlock) {
        return (cachedBlock & (1 << 2)) != 0;
    }

    public static boolean hasHandledOutputInventory(int cachedBlock) {
        return (cachedBlock & (1 << 3)) != 0;
    }

    public static boolean isSign(int cachedBlock) {
        return isStandingSign(cachedBlock) || isWallSign(cachedBlock);
    }

    public static boolean isStandingSign(int cachedBlock) {
        return (cachedBlock & (1 << 4)) != 0;
    }

    public static boolean isWallSign(int cachedBlock) {
        return (cachedBlock & (1 << 5)) != 0;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isValidPipeBlock(int cachedBlock) {
        return (cachedBlock & (1 << 1)) != 0;
    }

    public static boolean doTubeColorsMismatch(int cachedBlockA, int cachedBlockB) {
        int ordinalA = getTubeColorOrdinal(cachedBlockA);

        if (ordinalA == TubeColor.NONE.ordinal())
            return false;

        int ordinalB = getTubeColorOrdinal(cachedBlockB);

        if (ordinalB == TubeColor.NONE.ordinal())
            return false;

        return ordinalA != ordinalB;
    }

    public static boolean isTube(int cachedBlock) {
        return getTubeColorOrdinal(cachedBlock) != TubeColor.NONE.ordinal();
    }

    private static int getTubeColorOrdinal(int cachedBlock) {
        return (cachedBlock >> 6) & (32 - 1);
    }

    public static BlockFace getPistonFacing(int cachedBlock) {
        int index = (cachedBlock >> 11) & (32 - 1);

        if (index >= BLOCK_FACE_VALUES.length)
            return BlockFace.SELF;

        return BLOCK_FACE_VALUES[index];
    }

    public static boolean isMaterial(int cachedBlock, Material material) {
        return ((cachedBlock >> 16) & (8192 - 1)) == material.ordinal();
    }

    public static int fromBlock(Block block) {
        Material material = block.getType();
        BlockFace pistonFacing = BlockFace.SELF;

        if (material == Material.PISTON || material == Material.STICKY_PISTON)
            pistonFacing = ((Piston) block.getBlockData()).getFacing();

        return (
            getPreset(material)
                | ((material.ordinal() & (8192 - 1)) << 16)
                | ((pistonFacing.ordinal() & (32 - 1)) << 11)
        );
    }

    // The CHISELED_BOOKSHELF is excluded from this list, since there have been severe bugs
    // leading to item-duplication when trying to suck/put, despite calling BlockState#update.

    private static boolean hasHandledInputInventory(Material material) {
        // Do NOT try to get items from a CRAFTER - it doesn't have a result-slot, but merely drops the crafted item
        if (material == Material.CRAFTER)
            return false;

        return hasHandledOutputInventory(material);
    }

    private static boolean hasHandledOutputInventory(Material material) {
        switch (material) {
            case CHEST, TRAPPED_CHEST, DROPPER, DISPENSER, HOPPER, BARREL, DECORATED_POT, CRAFTER,
                 // v- FurnaceInventory
                 FURNACE, SMOKER, BLAST_FURNACE,
                 // v- BrewingStand state
                 BREWING_STAND -> { return true; }
            default -> {
                if (Tag.SHULKER_BOXES.isTagged(material))
                    return true;

                // Currently, this includes copper-chests. Once we're bumping the API to 1.21.10,
                // this hackish inclusion can be once again removed in favor of typed constants.

                if (material == Material.ENDER_CHEST)
                    return false;

                var name = material.name();

                return !name.contains("LEGACY") && name.endsWith("_CHEST");
            }
        }
    }

    public static void setupPresetTable() {
        int lowestIndex = -1;
        int highestIndex = -1;

        Material[] materials = Material.values();
        int[] presets = new int[materials.length];

        for (int index = 0; index < materials.length; ++index) {
            int value = presets[index] = makePreset(materials[index]);

            if (value == 0)
                continue;

            if (lowestIndex < 0)
                lowestIndex = index;

            highestIndex = index;
        }

        if (lowestIndex < 0)
            throw new IllegalStateException("Did not encounter a single non-zero preset!");

        presetByOffsetMaterialOrdinal = new int[highestIndex - lowestIndex + 1];
        presetMaterialOrdinalOffset = -lowestIndex;

        System.arraycopy(presets, lowestIndex, presetByOffsetMaterialOrdinal, 0, presetByOffsetMaterialOrdinal.length);
    }

    private static int makePreset(Material material) {
        TubeColor.TypeAwareTubeColor tubeColor = TubeColor.fromMaterial(material);

        boolean isValidPipeBlock = (
            tubeColor.color() != TubeColor.NONE
                || material == Material.PISTON
                || material == Material.STICKY_PISTON
        );

        boolean isInputInventory = hasHandledInputInventory(material);
        boolean isOutputInventory = hasHandledOutputInventory(material);
        boolean isStandingSign = Tag.STANDING_SIGNS.isTagged(material);
        boolean isWallSign = Tag.WALL_SIGNS.isTagged(material);

        return (
            ((tubeColor.color().ordinal() & (32 - 1)) << 6)
                | ((isWallSign ? 1 : 0) << 5)
                | ((isStandingSign ? 1 : 0) << 4)
                | ((isOutputInventory ? 1 : 0) << 3)
                | ((isInputInventory ? 1 : 0) << 2)
                | ((isValidPipeBlock ? 1 : 0) << 1)
                | (tubeColor.isPane() ? 1 : 0)
        );
    }

    public static int getPreset(Material material) {
        var index = material.ordinal() + presetMaterialOrdinalOffset;

        if (index >= 0 && index < presetByOffsetMaterialOrdinal.length)
            return presetByOffsetMaterialOrdinal[index];

        return 0;
    }
}
