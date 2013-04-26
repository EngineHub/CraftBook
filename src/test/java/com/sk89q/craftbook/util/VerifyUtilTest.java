package com.sk89q.craftbook.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VerifyUtil.class)
public class VerifyUtilTest {

    @Test
    public void testVerifyRadius() {

        int rad = VerifyUtil.verifyRadius(7, 15);
        assertTrue(rad == 7);
        rad = VerifyUtil.verifyRadius(20, 15);
        assertTrue(rad == 15);
    }

    @Test
    public void testWithoutNulls() {

        List<Object> list = new ArrayList<Object>();
        list.add(null);
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(null);
        list.add(mock(Object.class));
        list.add(mock(Object.class));
        list.add(null);

        list = (List<Object>) VerifyUtil.<Object>withoutNulls(list);

        assertTrue(!list.contains(null));
        assertTrue(list.size() == 5);
    }
}