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
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest(BlockUtil.class)
public class BlockUtilTest {

    @Test
    public void testIsBlockReplacable() {

        assertFalse(BlockUtil.isBlockReplacable(Material.STONE));
        assertTrue(BlockUtil.isBlockReplacable(Material.WATER));
        assertTrue(BlockUtil.isBlockReplacable(Material.LAVA));
        assertTrue(BlockUtil.isBlockReplacable(Material.AIR));
    }

    @Test
    public void testGetBlockDrops() {

        Block mlock = mock(Block.class);

        when(mlock.getType()).thenReturn(Material.SNOW);

        ItemStack[] drops = BlockUtil.getBlockDrops(mlock, null);

        assertEquals(0, drops.length);
    }
}