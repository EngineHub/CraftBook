/*
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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

public class VerifyUtilTest {

    @Test
    public void testVerifyRadius() {

        double rad = VerifyUtil.verifyRadius(7, 15);
        assertEquals(7, rad);
        rad = VerifyUtil.verifyRadius(20, 15);
        assertEquals(15, rad);
    }

    @Test
    public void testWithoutNulls() {

        List<Object> list = new ArrayList<>();
        list.add(null);
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(null);
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(null);

        list = (List<Object>) VerifyUtil.withoutNulls(list);

        assertFalse(list.contains(null));
        assertEquals(5, list.size());
    }
}
