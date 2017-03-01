/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TernaryStateTest {

    @Test
    public void testDoesPassWrong() {
        assertFalse(TernaryState.FALSE.doesPass(true));
    }

    @Test
    public void testDoesPassRight() {
        assertTrue(TernaryState.TRUE.doesPass(true));
    }

    @Test
    public void testNone() {
        assertTrue(TernaryState.NONE.doesPass(true));
        assertTrue(TernaryState.NONE.doesPass(false));
    }

    @Test
    public void testParseTrue() {
        assertTrue(TernaryState.TRUE == TernaryState.getFromString("true"));
        assertTrue(TernaryState.TRUE == TernaryState.getFromString("yes"));
        assertTrue(TernaryState.TRUE == TernaryState.getFromString("1"));
        assertTrue(TernaryState.TRUE == TernaryState.getFromString("y"));
    }

    @Test
    public void testParseFalse() {
        assertTrue(TernaryState.FALSE == TernaryState.getFromString("false"));
        assertTrue(TernaryState.FALSE == TernaryState.getFromString("no"));
        assertTrue(TernaryState.FALSE == TernaryState.getFromString("0"));
        assertTrue(TernaryState.FALSE == TernaryState.getFromString("n"));
    }

    @Test
    public void testParseNone() {
        assertTrue(TernaryState.NONE == TernaryState.getFromString("none"));
        assertTrue(TernaryState.NONE == TernaryState.getFromString("unknown"));
        assertTrue(TernaryState.NONE == TernaryState.getFromString("undefined"));
        assertTrue(TernaryState.NONE == TernaryState.getFromString("both"));
    }
}
