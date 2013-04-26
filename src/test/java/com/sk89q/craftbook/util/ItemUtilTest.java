package com.sk89q.craftbook.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bukkit.inventory.ItemStack;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ItemUtil.class)
public class ItemUtilTest {

    @Test
    public void testIsStackValid() {

        ItemStack mockStack = mock(ItemStack.class);
        when(mockStack.getTypeId()).thenReturn(-5);
        when(mockStack.getAmount()).thenReturn(5);
        assertTrue(!ItemUtil.isStackValid(mockStack));
        when(mockStack.getTypeId()).thenReturn(20);
        when(mockStack.getAmount()).thenReturn(5);
        assertTrue(ItemUtil.isStackValid(mockStack));
        when(mockStack.getAmount()).thenReturn(-60);
        assertTrue(!ItemUtil.isStackValid(mockStack));
        assertTrue(!ItemUtil.isStackValid(null));
    }

    @Test
    public void testGetItem() {

        ItemStack ret1 = ItemUtil.getItem("2");
        assertTrue(ret1.getTypeId() == 2);
        assertTrue(ret1.getData().getData() == -1);
        assertTrue(ret1.getAmount() == 1);
        ret1 = ItemUtil.getItem("2:5");
        assertTrue(ret1.getTypeId() == 2);
        assertTrue(ret1.getData().getData() == 5);
        assertTrue(ret1.getAmount() == 1);
        ret1 = ItemUtil.getItem("2:5*4");
        assertTrue(ret1.getTypeId() == 2);
        assertTrue(ret1.getData().getData() == 5);
        assertTrue(ret1.getAmount() == 4);
        ret1 = ItemUtil.getItem("2*4");
        assertTrue(ret1.getTypeId() == 2);
        assertTrue(ret1.getData().getData() == -1);
        assertTrue(ret1.getAmount() == 4);
    }
}