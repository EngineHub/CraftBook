package com.sk89q.craftbook.bukkit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sk89q.craftbook.ChangedSign;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ChangedSign.class)
public class BukkitChangedSignTest {

    @Test(expected=IllegalArgumentException.class)
    public void testBukkitChangedSign() {

        new ChangedSign(null, null);

        Block mockBlock = mock(Block.class);
        when(mockBlock.getState()).thenReturn(mock(Sign.class));

        ChangedSign sign = new ChangedSign(mockBlock, new String[]{"","","",""});
        assertTrue(sign.getSign() != null);
        assertTrue(sign.getLines().length == 4);
    }
}