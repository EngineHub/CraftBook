package com.sk89q.craftbook.mechanics.ic.gates.logic;

import static org.junit.Assert.assertTrue;

import com.sk89q.craftbook.mechanics.ic.gates.logic.AndGate;
import com.sk89q.craftbook.mechanics.ic.gates.logic.LogicICTests.LogicICTest;

public class AndGateTest implements LogicICTest {

    AndGate ic;

    public void testGetResult() {

        assertTrue(ic.getResult(2, 2));
        assertTrue(!ic.getResult(2, 1));
        assertTrue(!ic.getResult(2, 3));
        assertTrue(ic.getResult(3, 3));
        assertTrue(!ic.getResult(3, 0));
    }

    @Override
    public boolean testIC () {

        ic = new AndGate(null, null, null);

        try {
            testGetResult();
        } catch(Throwable e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}