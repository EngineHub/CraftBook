package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

  private final Material material;
  public final BlockFace pistonFacing;
  public final boolean hasInventory;
  public final boolean validPipeBlock;
  public final TubeColor tubeColor;
  public final boolean isPane;

  public CachedBlock(
    Material material,
    BlockFace pistonFacing,
    boolean hasInventory,
    boolean validPipeBlock,
    TubeColor tubeColor,
    boolean isPane
  ) {
    this.material = material;
    this.pistonFacing = pistonFacing;
    this.hasInventory = hasInventory;
    this.validPipeBlock = validPipeBlock;
    this.tubeColor = tubeColor;
    this.isPane = isPane;
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
