package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;

public class CachedBlock {

  public final Material material;
  public final BlockFace pistonFacing;

  private CachedBlock(Material material, BlockFace pistonFacing) {
    this.material = material;
    this.pistonFacing = pistonFacing;
  }

  public static CachedBlock fromBlock(Block block) {
    Material type = block.getType();
    BlockFace facing = BlockFace.SELF;

    if (type == Material.PISTON || type == Material.STICKY_PISTON)
      facing = ((Piston) block.getBlockData()).getFacing();

    return new CachedBlock(type, facing);
  }
}
