package com.sk89q.craftbook.util;

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

        List<String> strings = new ArrayList<String>();
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