/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.util;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ItemUtil.class, ItemSyntax.class })
public class ItemUtilTest {

    @Test
    public void testAddToStack() {

        ItemStack mockStack = newMockItemStack(Material.SAND, (byte) 0, 38);
        ItemStack mockStack2 = newMockItemStack(Material.SAND, (byte) 0, 10);

        when(mockStack.getMaxStackSize()).thenReturn(48);

        assertNull(ItemUtil.addToStack(mockStack, mockStack2));

        verify(mockStack).setAmount(48);

        when(mockStack2.getAmount()).thenReturn(11);

        ItemUtil.addToStack(mockStack, mockStack2);

        verify(mockStack, times(2)).setAmount(48);
        verify(mockStack2).setAmount(1);
    }

    @Test
    public void testFilterItems() {

        ArrayList<ItemStack> items = new ArrayList<>();
        items.add(newMockItemStack(Material.GRASS_BLOCK, (byte) 0, 1));
        items.add(newMockItemStack(Material.GRAVEL, (byte) 0, 1));
        items.add(newMockItemStack(Material.STONE, (byte) 0, 1));
        HashSet<ItemStack> inclusions = new HashSet<>();
        inclusions.add(newMockItemStack(Material.GRASS_BLOCK, (byte) 0, 1));
        List<ItemStack> filtered = ItemUtil.filterItems(items, inclusions, null);
        assertEquals(1, filtered.size());
        HashSet<ItemStack> exclusions = new HashSet<>();
        exclusions.add(newMockItemStack(Material.GRAVEL, (byte) 0, 1));
        filtered = ItemUtil.filterItems(items, null, exclusions);
        assertEquals(2, filtered.size());
    }

    @Test
    public void testAreItemsIdentical() {

        ItemStack test1 = newMockItemStack(Material.OAK_PLANKS, (byte) 4, 1);
        ItemStack test2 = newMockItemStack(Material.OAK_PLANKS, (byte) 8, 1);
        assertTrue(!ItemUtil.areItemsIdentical(test1, test2));
        test2 = newMockItemStack(Material.GRASS_BLOCK, (byte) 8, 1);
        assertTrue(!ItemUtil.areItemsIdentical(test1, test2));
        test2 = newMockItemStack(Material.OAK_PLANKS, (byte) 4, 1);
        assertTrue(ItemUtil.areItemsIdentical(test1, test2));
    }

    @Test
    public void testIsStackValid() {

        ItemStack mockStack = newMockItemStack(Material.AIR, (byte) 0, 5);
        assertTrue(!ItemUtil.isStackValid(mockStack));
        when(mockStack.getType()).thenReturn(Material.BEETROOT);
        when(mockStack.getAmount()).thenReturn(5);
        assertTrue(ItemUtil.isStackValid(mockStack));
        when(mockStack.getAmount()).thenReturn(-60);
        assertTrue(!ItemUtil.isStackValid(mockStack));
        assertTrue(!ItemUtil.isStackValid(null));
    }

    @Test
    public void testTakeFromEntity() {

        Item entity = mock(Item.class);
        when(entity.isDead()).thenReturn(true);
        assertTrue(!ItemUtil.takeFromItemEntity(null, 1));
        assertTrue(!ItemUtil.takeFromItemEntity(entity, 1));
        when(entity.isDead()).thenReturn(false);
        when(entity.getItemStack()).thenReturn(ItemSyntax.getItem("2:0*20"));
        assertTrue(!ItemUtil.takeFromItemEntity(entity, 21));
        assertTrue(ItemUtil.takeFromItemEntity(entity, 2));
        assertTrue(ItemUtil.takeFromItemEntity(entity, 18));
        verify(entity).remove();
    }

    @Test
    public void testIsCookable() {

        ItemStack ingredient = newMockItemStack(Material.CHICKEN, (byte) 0, 1);
        assertTrue(ItemUtil.isCookable(ingredient));
    }

    @Test
    public void testIsSmeltable() {

        ItemStack ingredient = newMockItemStack(Material.CLAY, (byte) 0, 1);
        assertTrue(ItemUtil.isSmeltable(ingredient));
    }

    @Test
    public void testGetItem() {

        ItemStack ret1 = ItemSyntax.getItem("2");
        assertSame(ret1.getType(), Material.GRASS_BLOCK);
        assertEquals(ret1.getData().getData(), -1);
        assertEquals(1, ret1.getAmount());
        ret1 = ItemSyntax.getItem("2:5");
        assertSame(ret1.getType(), Material.GRASS_BLOCK);
        assertEquals(5, ret1.getData().getData());
        assertEquals(1, ret1.getAmount());
        ret1 = ItemSyntax.getItem("2:5*4");
        assertSame(ret1.getType(), Material.GRASS_BLOCK);
        assertEquals(5, ret1.getData().getData());
        assertEquals(4, ret1.getAmount());
        ret1 = ItemSyntax.getItem("2*4");
        assertSame(ret1.getType(), Material.GRASS_BLOCK);
        assertEquals(ret1.getData().getData(), -1);
        assertEquals(4, ret1.getAmount());
    }

    public ItemStack newMockItemStack(Material id, byte data, int amount) {

        ItemStack mockStack = mock(ItemStack.class);
        when(mockStack.getAmount()).thenReturn(amount);
        when(mockStack.getType()).thenReturn(id);
        when(mockStack.getDurability()).thenReturn((short) data);

        return mockStack;
    }
}