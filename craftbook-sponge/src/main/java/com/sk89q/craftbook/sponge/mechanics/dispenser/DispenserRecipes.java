package com.sk89q.craftbook.sponge.mechanics.dispenser;

import com.flowpowered.math.vector.Vector3d;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Module(id = "dispenserrecipes", name = "DispenserRecipes", onEnable = "onInitialize", onDisable = "onDisable")
public class DispenserRecipes extends SpongeBlockMechanic implements DocumentationProvider {

    private List<DispenserRecipe> recipes = new ArrayList<>();

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        recipes.add(new Cannon());
        recipes.add(new Fan());
    }

    @Override
    public String getPath() {
        return "mechanics/dispenser_recipes";
    }

    @Override
    public boolean isValid(Location<World> location) {
        return location.getBlockType() == BlockTypes.DISPENSER;
    }

    @Listener
    public void onDispense(DropItemEvent.Dispense event, @First LocatableBlock dispenser) {
        if (dispenser.getBlockState().getType() != BlockTypes.DISPENSER) {
            return;
        }

        Dispenser dispenserTile = (Dispenser) dispenser.getLocation().getTileEntity().get();
        if (handleDispenserAction(dispenserTile, event.getEntities().get(0).getVelocity())) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onCreateEntity(ConstructEntityEvent event, @First LocatableBlock dispenser) {
        // TODO
    }

    // TODO Water/Lava form

    public boolean handleDispenserAction(Dispenser dispenser, Vector3d velocity) {
        for (DispenserRecipe recipe : recipes) {
            ItemStack[] items = StreamSupport.stream(dispenser.getInventory().slots().spliterator(), false)
                    .map(Inventory::peek).filter(Optional::isPresent)
                    .map(Optional::get).toArray(ItemStack[]::new);
            if (recipe.doesPass(items)) {
                if (recipe.doAction(dispenser, items, velocity)) {
                    dispenser.getInventory().slots().forEach(Inventory::poll); // Take one of every stack.
                    return true;
                }

                break;
            }
        }

        return false;
    }
}
