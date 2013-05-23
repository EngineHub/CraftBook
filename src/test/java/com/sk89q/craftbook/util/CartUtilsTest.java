package com.sk89q.craftbook.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CartUtils.class)
public class CartUtilsTest {

    @Test
    public void testReverse() {

        Minecart cart = mock(Minecart.class);
        when(cart.getVelocity()).thenReturn(new Vector(0,1,0));
        CartUtils.reverse(cart);
        verify(cart).setVelocity(new Vector(0,-1,0));
    }

    @Test
    public void testStop() {

        Minecart cart = mock(Minecart.class);
        CartUtils.stop(cart);
        verify(cart).setVelocity(new Vector(0,0,0));
    }
}