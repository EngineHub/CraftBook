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

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CartUtil.class)
public class CartUtilsTest {

    @Test
    public void testReverse() {

        Minecart cart = mock(Minecart.class);
        when(cart.getVelocity()).thenReturn(new Vector(0, 1, 0));
        CartUtil.reverse(cart);
        verify(cart).setVelocity(new Vector(0, -1, 0));
    }

    @Test
    public void testStop() {

        Minecart cart = mock(Minecart.class);
        CartUtil.stop(cart);
        verify(cart).setVelocity(new Vector(0, 0, 0));
    }
}