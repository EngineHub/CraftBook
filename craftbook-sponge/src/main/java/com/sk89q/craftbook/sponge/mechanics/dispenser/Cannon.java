package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Cannon extends SimpleDispenserRecipe {

    public Cannon() {
        super(new ItemStack[]{
                ItemStack.of(ItemTypes.FIRE_CHARGE, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.FIRE_CHARGE, 1),
                ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.TNT, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1),
                ItemStack.of(ItemTypes.FIRE_CHARGE, 1), ItemStack.of(ItemTypes.GUNPOWDER, 1), ItemStack.of(ItemTypes.FIRE_CHARGE, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe, Vector3d velocity) {
        Direction face = dispenser.getLocation().get(Keys.DIRECTION).orElse(Direction.NONE);
        if (face != Direction.NONE) {
            Location<World> location = dispenser.getLocation().getRelative(face).add(0.5, 0.5, 0.5);
            PrimedTNT tnt = (PrimedTNT) dispenser.getWorld().createEntity(EntityTypes.PRIMED_TNT, location.getPosition());
            tnt.setVelocity(velocity.normalize().mul(2f));
            dispenser.getWorld().spawnEntity(tnt);
            return true;
        }

        return false;
    }
}
