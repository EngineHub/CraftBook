package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

public class Fan extends SimpleDispenserRecipe {

    public Fan() {
        super(new ItemStack[]{
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
                ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.PISTON, 1), ItemStack.of(ItemTypes.LEAVES, 1),
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe, Vector3d velocity) {
        return false;
    }
}
