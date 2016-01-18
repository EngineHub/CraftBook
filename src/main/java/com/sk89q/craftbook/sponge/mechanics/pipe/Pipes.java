package com.sk89q.craftbook.sponge.mechanics.pipe;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.InputPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.OutputPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.PassthroughPipePart;
import com.sk89q.craftbook.sponge.mechanics.pipe.parts.PipePart;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.BlockUtil;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.InventoryItemData;
import org.spongepowered.api.data.property.block.PoweredProperty;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;

import java.util.*;

@Module(moduleName = "Pipes", onEnable="onInitialize", onDisable="onDisable")
public class Pipes extends SpongeBlockMechanic {

    private PipePart[] pipeParts;

    @Override
    public void onInitialize() {
        List<PipePart> pipePartList = new ArrayList<>();
        pipePartList.add(new PassthroughPipePart());
        pipePartList.add(new InputPipePart());
        pipePartList.add(new OutputPipePart());

        pipeParts = pipePartList.toArray(new PipePart[pipePartList.size()]);
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event) {

        BlockSnapshot source;
        if(event.getCause().first(BlockSnapshot.class).isPresent())
            source = event.getCause().first(BlockSnapshot.class).get();
        else
            return;

        if(isValid(source.getLocation().get())) {
            PoweredProperty poweredProperty = source.getLocation().get().getProperty(PoweredProperty.class).orElse(null);
            if(poweredProperty.getValue() != null && poweredProperty.getValue())
                performPipeAction(source.getLocation().get());
        }
    }

    public void performPipeAction(Location location) {
        Direction direction = (Direction) location.get(Keys.DIRECTION).get();
        Location inventorySource = location.getRelative(direction);

        //Let's try and find a source of items!
        Inventory inventory = getInventoryForLocation(inventorySource);

        if(inventory != null) {
            Optional<ItemStack> itemStackOptional = inventory.poll(1);
            if(itemStackOptional.isPresent()) {
                ItemStack itemStack = itemStackOptional.get();
                Set<Location> traversed = new HashSet<>();

                try {
                    itemStack = doPipeIteration(location, itemStack, direction, traversed);
                } catch(StackOverflowError error) {
                    error.printStackTrace();
                }

                if(itemStack.getQuantity() > 0) {
                    for(ItemStackSnapshot snapshot : inventory.offer(itemStack).getRejectedItems()) {
                        Item item = (Item) location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition()).get();
                        item.offer(Keys.REPRESENTED_ITEM, snapshot);
                        location.getExtent().spawnEntity(item, Cause.of(location));
                    }
                }
            }
        }
    }

    private ItemStack doPipeIteration(Location location, ItemStack itemStack, Direction fromDirection, Set<Location> traversed) {
        if(traversed.contains(location))
            return itemStack;

        traversed.add(location);

        if(itemStack.getQuantity() == 0)
            return itemStack;

        PipePart pipePart = getPipePart(location);
        if(pipePart == null)
            return itemStack;

        for(Location location1 : pipePart.findValidOutputs(location, itemStack, fromDirection)) {
            if(pipePart instanceof OutputPipePart) {
                Inventory inventory = getInventoryForLocation(location1);

                if(inventory != null) {
                    InventoryTransactionResult result = inventory.offer(itemStack);
                    if(result.getRejectedItems().size() > 0) {
                        for (ItemStackSnapshot snapshot : result.getRejectedItems()) {
                            itemStack = snapshot.createStack();
                        }
                    } else {
                        itemStack.setQuantity(0);
                    }
                }
            } else
                itemStack = doPipeIteration(location1, itemStack, BlockUtil.getFacing(location, location1), traversed);
        }

        return itemStack;
    }

    private PipePart getPipePart(Location location) {
        for(PipePart pipePart : pipeParts)
            if(pipePart.isValid(location.getBlock()))
                return pipePart;
        return null;
    }

    private Inventory getInventoryForLocation(Location location) {
        Inventory inventory = null;

        if(location.hasTileEntity()) {
            TileEntity tileEntity = (TileEntity) location.getTileEntity().get();
            if(tileEntity instanceof Carrier) {
                inventory = ((Carrier) tileEntity).getInventory();
            }
        }

        return inventory;
    }

    @Override
    public boolean isValid(Location location) {
        return location.getBlockType() == BlockTypes.STICKY_PISTON;
    }
}
