package com.sk89q.craftbook.circuits.gates.logic;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AndGate.class)
public class AndGateTest {

    @Test
    public void testGetResult() {

        AndGate ic = new AndGate(null, null, null);
        assertTrue(ic.getResult(2, 2));
        assertTrue(!ic.getResult(2, 1));
        assertTrue(!ic.getResult(2, 3));
        assertTrue(ic.getResult(3, 3));
        assertTrue(!ic.getResult(3, 0));
    }
}