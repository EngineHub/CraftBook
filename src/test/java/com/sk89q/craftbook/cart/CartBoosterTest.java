package com.sk89q.craftbook.cart;

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
@PrepareForTest(CartBooster.class)
public class CartBoosterTest {

    @Test
    public void testImpact() {

        CartBooster mech = new CartBooster(2);
        Minecart cart = mock(Minecart.class);
        when(cart.getVelocity()).thenReturn(new Vector(1,1,1));
        mech.impact(cart, new CartMechanismBlocks(null,null,null), false);
        verify(cart).setVelocity(new Vector(2,2,2));

        mech = new CartBooster(-2);
        cart = mock(Minecart.class);
        when(cart.getVelocity()).thenReturn(new Vector(1,1,1));
        mech.impact(cart, new CartMechanismBlocks(null,null,null), false);
        verify(cart).setVelocity(new Vector(-2,-2,-2));
    }
}