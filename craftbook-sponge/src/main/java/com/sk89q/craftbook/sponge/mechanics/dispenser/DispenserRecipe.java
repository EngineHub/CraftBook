package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.item.inventory.ItemStack;

public interface DispenserRecipe {

    boolean doesPass(ItemStack[] recipe);

    boolean doAction(Dispenser dispenser, ItemStack[] recipe, Vector3d velocity);
}
