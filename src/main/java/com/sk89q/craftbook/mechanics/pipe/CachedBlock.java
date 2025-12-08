package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

import java.util.ArrayList;
import java.util.List;

public class CachedBlock {

  public static final int NULL_SENTINEL = (1 << 31);

  private static final BlockFace[] BLOCK_FACE_VALUES = BlockFace.values();
  private static final List<Material> additionalChests = findAdditionalChestTypes();

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
  public static boolean isIsValidPipeBlock(int cachedBlock) {
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

    if (index > BLOCK_FACE_VALUES.length)
      return BlockFace.SELF;

    return BLOCK_FACE_VALUES[index];
  }

  public static boolean isMaterial(int cachedBlock, Material material) {
    return ((cachedBlock >> 16) & (8192 - 1)) == material.ordinal();
  }

  public static int fromBlock(Block block) {
    Material material = block.getType();
    TubeColor.TypeAwareTubeColor tubeColor = TubeColor.fromMaterial(material);
    BlockFace pistonFacing = BlockFace.SELF;
    boolean isValidPipeBlock = tubeColor.color() != TubeColor.NONE;

    if (material == Material.PISTON || material == Material.STICKY_PISTON) {
      pistonFacing = ((Piston) block.getBlockData()).getFacing();
      isValidPipeBlock = true;
    }

    boolean isStandingSign = false;
    boolean isWallSign = false;

    if (!isValidPipeBlock) {
      isWallSign = Tag.WALL_SIGNS.isTagged(material);

      if (!isWallSign)
        isStandingSign = Tag.STANDING_SIGNS.isTagged(material);
    }

    return (
      ((material.ordinal() & (8192 - 1))                 << 16)
        | ((pistonFacing.ordinal() & (32 - 1))           << 11)
        | ((tubeColor.color().ordinal() & (32 - 1))      << 6)
        | ((isWallSign ? 1 : 0)                          << 5)
        | ((isStandingSign ? 1 : 0)                      << 4)
        | ((hasHandledOutputInventory(material) ? 1 : 0) << 3)
        | ((hasHandledInputInventory(material) ? 1 : 0)  << 2)
        | ((isValidPipeBlock ? 1 : 0)                    << 1)
        | (tubeColor.isPane() ? 1 : 0)
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
    return switch (material) {
      case CHEST, TRAPPED_CHEST, DROPPER, DISPENSER, HOPPER, BARREL, DECORATED_POT, CRAFTER,
           // v- FurnaceInventory
           FURNACE, SMOKER, BLAST_FURNACE,
           // v- BrewingStand state
           BREWING_STAND -> true;
      default -> additionalChests.contains(material) || Tag.SHULKER_BOXES.isTagged(material);
    };
  }

  // Currently, this includes copper-chests - because why not? Linear search on such a small contiguous
  // chunk of memory is more than fast enough (plus, we're caching it). Once we're adding them to the switches
  // above when bumping to 1.21.10 API, the dynamic loading of additional chest-types can be removed again.

  private static List<Material> findAdditionalChestTypes() {
    List<Material> additionalChests = new ArrayList<>();

    for (var material : Material.values()) {
      if (material == Material.CHEST || material == Material.TRAPPED_CHEST || material == Material.ENDER_CHEST)
        continue;

      var name = material.name();

      if (name.contains("LEGACY"))
        continue;

      if (name.endsWith("_CHEST"))
        additionalChests.add(material);
    }

    return additionalChests;
  }
}
