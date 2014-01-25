package com.sk89q.craftbook.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockUtil.class)
public class BlockUtilTest {

    @Test
    public void testAreBlocksSimilar() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getType()).thenReturn(Material.SAND);

        Block mockBlock2 = mock(Block.class);
        when(mockBlock2.getType()).thenReturn(Material.STONE);

        assertTrue(!BlockUtil.areBlocksSimilar(mockBlock1, mockBlock2));

        when(mockBlock2.getType()).thenReturn(Material.SAND);

        assertTrue(BlockUtil.areBlocksSimilar(mockBlock1, mockBlock2));
    }

    @Test
    public void testAreBlocksIdentical() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getType()).thenReturn(Material.SAND);
        when(mockBlock1.getData()).thenReturn((byte) 1);

        Block mockBlock2 = mock(Block.class);
        when(mockBlock2.getType()).thenReturn(Material.STONE);
        when(mockBlock2.getData()).thenReturn((byte) 1);

        assertTrue(!BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));

        when(mockBlock2.getType()).thenReturn(Material.SAND);

        assertTrue(BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));

        when(mockBlock2.getData()).thenReturn((byte) 2);

        assertTrue(!BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));
    }

    @Test
    public void testIsBlockSimilarTo() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getType()).thenReturn(Material.WOOD);

        assertTrue(!BlockUtil.isBlockSimilarTo(mockBlock1, Material.COBBLESTONE));

        assertTrue(BlockUtil.isBlockSimilarTo(mockBlock1, Material.WOOD));
    }

    @Test
    public void testIsBlockIdenticalTo() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getType()).thenReturn(Material.WOOD);
        when(mockBlock1.getData()).thenReturn((byte) 1);

        assertTrue(!BlockUtil.isBlockIdenticalTo(mockBlock1, Material.SAND, (byte) 1));

        assertTrue(BlockUtil.isBlockIdenticalTo(mockBlock1, Material.WOOD, (byte) 1));

        assertTrue(!BlockUtil.isBlockIdenticalTo(mockBlock1, Material.WOOD, (byte) 4));
    }

    @Test
    public void testIsBlockReplacable() {

        assertTrue(!BlockUtil.isBlockReplacable(Material.STONE));
        assertTrue(BlockUtil.isBlockReplacable(Material.WATER));
        assertTrue(BlockUtil.isBlockReplacable(Material.LAVA));
        assertTrue(BlockUtil.isBlockReplacable(Material.AIR));
    }

    @Test
    public void testHasTileData() {

        assertTrue(!BlockUtil.hasTileData(Material.STONE));
        assertTrue(BlockUtil.isBlockReplacable(Material.WATER));
        assertTrue(BlockUtil.isBlockReplacable(Material.LAVA));
        assertTrue(BlockUtil.isBlockReplacable(Material.AIR));
    }

    @Test
    public void testGetBlockCentre() {

        Block mlock = mock(Block.class);
        Location mocation = mock(Location.class);

        when(mlock.getLocation()).thenReturn(mocation);

        BlockUtil.getBlockCentre(mlock);

        verify(mocation).add(0.5, 0.5, 0.5);
    }

    @Test
    public void testGetBlockDrops() {

        Block mlock = mock(Block.class);

        when(mlock.getType()).thenReturn(Material.SNOW);

        ItemStack[] drops = BlockUtil.getBlockDrops(mlock, null);

        assertTrue(drops.length == 0);
    }
}