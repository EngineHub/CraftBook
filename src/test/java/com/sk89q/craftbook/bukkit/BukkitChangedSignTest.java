package com.sk89q.craftbook.bukkit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.bukkit.block.Sign;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BukkitChangedSign.class)
public class BukkitChangedSignTest {

    @Test(expected=IllegalArgumentException.class)
    public void testBukkitChangedSign() {

        BukkitChangedSign sign = new BukkitChangedSign(null, null);

        sign = new BukkitChangedSign(mock(Sign.class), new String[]{"","","",""});
        assertTrue(sign.getSign() != null);
        assertTrue(sign.getLines().length == 4);
    }
}