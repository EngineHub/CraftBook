package com.sk89q.craftbook.mechanics.pipe;

import com.sk89q.craftbook.util.InventoryUtil;
import com.sk89q.craftbook.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

  public final Material material;
  public final BlockFace pistonFacing;
  public final boolean hasInventory;
  public final boolean validPipeBlock;

  public CachedBlock(
    Material material,
    BlockFace pistonFacing,
    boolean hasInventory,
    boolean validPipeBlock
  ) {
    this.material = material;
    this.pistonFacing = pistonFacing;
    this.hasInventory = hasInventory;
    this.validPipeBlock = validPipeBlock;
  }

  public static CachedBlock fromBlock(Block block) {
    Material type = block.getType();
    BlockFace facing = BlockFace.SELF;

    if (type == Material.PISTON || type == Material.STICKY_PISTON)
      facing = ((Piston) block.getBlockData()).getFacing();

    return new CachedBlock(
      type,
      facing,
      InventoryUtil.doesBlockHaveInventory(type),
      switch (type) {
        case GLASS, PISTON, STICKY_PISTON, GLASS_PANE -> true;
        default -> ItemUtil.isStainedGlass(type) || ItemUtil.isStainedGlassPane(type);
      }
    );
  }
}
