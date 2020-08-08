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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ArrayUtil.class)
public class ArrayUtilTest {

    @Test
    public void testGetArrayPage() {

        List<String> strings = new ArrayList<>();
        strings.add("Line1");
        strings.add("Line2");
        strings.add("Line3");
        strings.add("Line4");
        String[] lines = ArrayUtil.getArrayPage(strings, 1);
        assertTrue(lines.length == 4);
        strings.add("Line5");
        strings.add("Line6");
        strings.add("Line7");
        strings.add("Line8");
        strings.add("Line9");
        lines = ArrayUtil.getArrayPage(strings, 1);
        assertTrue(lines.length == 8);
        assertTrue(lines[7].equals("Line8"));
        lines = ArrayUtil.getArrayPage(strings, 2);
        assertTrue(lines.length == 8);
        assertTrue(lines[0].equals("Line9"));
    }
}