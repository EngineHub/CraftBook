package com.sk89q.craftbook.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.block.Block;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.sk89q.worldedit.blocks.BlockID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockUtil.class)
public class BlockUtilTest {

    @Test
    public void testAreBlocksSimilar() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getTypeId()).thenReturn(5);

        Block mockBlock2 = mock(Block.class);
        when(mockBlock2.getTypeId()).thenReturn(8);

        assertTrue(!BlockUtil.areBlocksSimilar(mockBlock1, mockBlock2));

        when(mockBlock2.getTypeId()).thenReturn(5);

        assertTrue(BlockUtil.areBlocksSimilar(mockBlock1, mockBlock2));
    }

    @Test
    public void testAreBlocksIdentical() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getTypeId()).thenReturn(5);
        when(mockBlock1.getData()).thenReturn((byte) 1);

        Block mockBlock2 = mock(Block.class);
        when(mockBlock2.getTypeId()).thenReturn(8);
        when(mockBlock2.getData()).thenReturn((byte) 1);

        assertTrue(!BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));

        when(mockBlock2.getTypeId()).thenReturn(5);

        assertTrue(BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));

        when(mockBlock2.getData()).thenReturn((byte) 2);

        assertTrue(!BlockUtil.areBlocksIdentical(mockBlock1, mockBlock2));
    }

    @Test
    public void testIsBlockSimilarTo() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getTypeId()).thenReturn(5);

        assertTrue(!BlockUtil.isBlockSimilarTo(mockBlock1, 4));

        assertTrue(BlockUtil.isBlockSimilarTo(mockBlock1, 5));
    }

    @Test
    public void testIsBlockIdenticalTo() {

        Block mockBlock1 = mock(Block.class);
        when(mockBlock1.getTypeId()).thenReturn(5);
        when(mockBlock1.getData()).thenReturn((byte) 1);

        assertTrue(!BlockUtil.isBlockIdenticalTo(mockBlock1, 8, (byte) 1));

        assertTrue(BlockUtil.isBlockIdenticalTo(mockBlock1, 5, (byte) 1));

        assertTrue(!BlockUtil.isBlockIdenticalTo(mockBlock1, 5, (byte) 4));
    }

    @Test
    public void testIsBlockReplacable() {

        assertTrue(!BlockUtil.isBlockReplacable(BlockID.STONE));
        assertTrue(BlockUtil.isBlockReplacable(BlockID.WATER));
        assertTrue(BlockUtil.isBlockReplacable(BlockID.LAVA));
        assertTrue(BlockUtil.isBlockReplacable(BlockID.AIR));
    }
}