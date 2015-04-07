package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class LogicICTests {

    public static LogicICTests instance;

    LogicICTest[] tests = new LogicICTest[] {new AndGateTest()};

    @Test
    public void testLogicICs() {

        instance = this;

        for(LogicICTest test : tests)
            if(!test.testIC())
                throw new AssertionError();
    }

    public static interface LogicICTest {

        public boolean testIC();
    }
}