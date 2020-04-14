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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
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
        assertSame(TernaryState.TRUE, TernaryState.getFromString("true"));
        assertSame(TernaryState.TRUE, TernaryState.getFromString("yes"));
        assertSame(TernaryState.TRUE, TernaryState.getFromString("1"));
        assertSame(TernaryState.TRUE, TernaryState.getFromString("y"));
    }

    @Test
    public void testParseFalse() {
        assertSame(TernaryState.FALSE, TernaryState.getFromString("false"));
        assertSame(TernaryState.FALSE, TernaryState.getFromString("no"));
        assertSame(TernaryState.FALSE, TernaryState.getFromString("0"));
        assertSame(TernaryState.FALSE, TernaryState.getFromString("n"));
    }

    @Test
    public void testParseNone() {
        assertSame(TernaryState.NONE, TernaryState.getFromString("none"));
        assertSame(TernaryState.NONE, TernaryState.getFromString("unknown"));
        assertSame(TernaryState.NONE, TernaryState.getFromString("undefined"));
        assertSame(TernaryState.NONE, TernaryState.getFromString("both"));
    }
}
