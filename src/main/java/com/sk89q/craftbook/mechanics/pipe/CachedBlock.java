package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

  public static final int NULL_SENTINEL = (1 << 31);

  private static final BlockFace[] BLOCK_FACE_VALUES = BlockFace.values();

  public static boolean isPane(int cachedBlock) {
    return (cachedBlock & 1) != 0;
  }

  public static boolean hasInventory(int cachedBlock) {
    return (cachedBlock & (1 << 2)) != 0;
  }

  public static boolean isSign(int cachedBlock) {
    return isStandingSign(cachedBlock) || isWallSign(cachedBlock);
  }

  public static boolean isStandingSign(int cachedBlock) {
    return (cachedBlock & (1 << 3)) != 0;
  }

  public static boolean isWallSign(int cachedBlock) {
    return (cachedBlock & (1 << 4)) != 0;
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
    return (cachedBlock >> 5) & (32 - 1);
  }

  public static BlockFace getPistonFacing(int cachedBlock) {
    int index = (cachedBlock >> 10) & (32 - 1);

    if (index > BLOCK_FACE_VALUES.length)
      return BlockFace.SELF;

    return BLOCK_FACE_VALUES[index];
  }

  public static boolean isMaterial(int cachedBlock, Material material) {
    return ((cachedBlock >> 15) & (8192 - 1)) == material.ordinal();
  }

  public static int fromBlock(Block block) {
    Material material = block.getType();
    TubeColor.TypeAwareTubeColor tubeColor = TubeColor.fromMaterial(material);
    boolean hasInventory = InventoryUtil.doesBlockHaveInventory(material);
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
      ((material.ordinal() & (8192 - 1))            << 15)
        | ((pistonFacing.ordinal() & (32 - 1))      << 10)
        | ((tubeColor.color().ordinal() & (32 - 1)) << 5)
        | ((isWallSign ? 1 : 0)                     << 4)
        | ((isStandingSign ? 1 : 0)                 << 3)
        | ((hasInventory ? 1 : 0)                   << 2)
        | ((isValidPipeBlock ? 1 : 0)               << 1)
        | (tubeColor.isPane() ? 1 : 0)
    );
  }
}
