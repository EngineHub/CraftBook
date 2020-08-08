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

package org.enginehub.craftbook.bukkit;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.enginehub.craftbook.ChangedSign;
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