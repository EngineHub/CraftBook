/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.ThrownExpBottle;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class XPShooter extends SimpleDispenserRecipe {

    public XPShooter() {
        super(new ItemStack[] {
                ItemStack.of(ItemTypes.AIR, 1), ItemStack.of(ItemTypes.REDSTONE, 1), ItemStack.of(ItemTypes.AIR, 1),
                ItemStack.of(ItemTypes.REDSTONE, 1), ItemStack.of(ItemTypes.GLASS_BOTTLE, 1), ItemStack.of(ItemTypes.REDSTONE, 1),
                ItemStack.of(ItemTypes.AIR, 1), ItemStack.of(ItemTypes.REDSTONE, 1), ItemStack.of(ItemTypes.AIR, 1),
        });
    }

    @Override
    public boolean doAction(Dispenser dispenser, ItemStack[] recipe) {
        Direction face = dispenser.getLocation().get(Keys.DIRECTION).orElse(Direction.NONE);
        if (face != Direction.NONE) {
            Location<World> location = dispenser.getLocation().getRelative(face).add(0.5, 0.5, 0.5);
            ThrownExpBottle bottle = (ThrownExpBottle) dispenser.getWorld().createEntity(EntityTypes.THROWN_EXP_BOTTLE, location.getPosition());
            Vector3d bottleVelocity = face.asOffset().add(0, 0.1f, 0).normalize().mul(1.5f);
            bottle.setVelocity(bottleVelocity);
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
                dispenser.getWorld().spawnEntity(bottle);
            }
            return true;
        }

        return false;
    }
}
