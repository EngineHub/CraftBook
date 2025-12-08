package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

  private final Material material;         // remaining bits
  private final BlockFace pistonFacing;    // 3b
  private final boolean hasInventory;      // 1b
  private final boolean isValidPipeBlock;  // 1b
  private final TubeColor tubeColor;       // 9b
  private final boolean isPane;            // 1b

  public CachedBlock(
    Material material,
    BlockFace pistonFacing,
    boolean hasInventory,
    boolean isValidPipeBlock,
    TubeColor tubeColor,
    boolean isPane
  ) {
    this.material = material;
    this.pistonFacing = pistonFacing;
    this.hasInventory = hasInventory;
    this.isValidPipeBlock = isValidPipeBlock;
    this.tubeColor = tubeColor;
    this.isPane = isPane;
  }

  public boolean isPane() {
    return isPane;
  }

  public boolean hasInventory() {
    return hasInventory;
  }

  public boolean isIsValidPipeBlock() {
    return isValidPipeBlock;
  }

  public Block getBlockAtThisPistonFacing(Block block) {
    return block.getRelative(pistonFacing);
  }

  public boolean doTubeColorsMismatch(CachedBlock other) {
    return this.tubeColor != TubeColor.NONE && other.tubeColor != TubeColor.NONE && this.tubeColor != other.tubeColor;
  }

  public boolean hasTubeColor() {
    return this.tubeColor != TubeColor.NONE;
  }

  public boolean isMaterial(Material material) {
    return this.material == material;
  }

  public static CachedBlock fromBlock(Block block) {
    Material type = block.getType();
    BlockFace facing = BlockFace.SELF;

    if (type == Material.PISTON || type == Material.STICKY_PISTON)
      facing = ((Piston) block.getBlockData()).getFacing();

    TubeColor.TypeAwareTubeColor tubeColor = TubeColor.fromMaterial(type);

    return new CachedBlock(
      type,
      facing,
      InventoryUtil.doesBlockHaveInventory(type),
      tubeColor.color() != TubeColor.NONE || facing != BlockFace.SELF,
      tubeColor.color(),
      tubeColor.isPane()
    );
  }
}
