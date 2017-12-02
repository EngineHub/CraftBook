package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.craftbook.sponge.util.LocationUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Vacuum extends SimpleDispenserRecipe {

    public Vacuum() {
        super(new ItemStack[]{
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
                ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.STICKY_PISTON, 1), ItemStack.of(ItemTypes.LEAVES, 1),
                ItemStack.of(ItemTypes.WEB, 1), ItemStack.of(ItemTypes.LEAVES, 1), ItemStack.of(ItemTypes.WEB, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe, Vector3d velocity) {
        Direction face = dispenser.getLocation().get(Keys.DIRECTION).orElse(Direction.NONE);
        if (face != Direction.NONE) {
            Location<World> offset = dispenser.getLocation().getRelative(face);
            int distance = 0;
            while (offset.getBlockType() == BlockTypes.AIR && distance < 5) {
                for (Entity e : LocationUtil.getEntitiesAtLocation(offset)) {
                    e.setVelocity(face.asOffset().mul(5 - distance).mul(-1.0));
                }
                distance ++;
                offset = offset.getRelative(face);
            }
            return true;
        }
        return false;
    }
}
