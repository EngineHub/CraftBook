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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VerifyUtil.class)
public class VerifyUtilTest {

    @Test
    public void testVerifyRadius() {

        double rad = VerifyUtil.verifyRadius(7, 15);
        assertTrue(rad == 7);
        rad = VerifyUtil.verifyRadius(20, 15);
        assertTrue(rad == 15);
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

        assertTrue(!list.contains(null));
        assertTrue(list.size() == 5);
    }
}