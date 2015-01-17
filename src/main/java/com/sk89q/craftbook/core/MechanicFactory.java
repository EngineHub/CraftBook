package com.sk89q.craftbook.core;

import org.spongepowered.api.block.BlockLoc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;

import com.sk89q.craftbook.core.util.InvalidFactoryException;

public interface MechanicFactory<T extends Mechanic> {

    /**
     * Creates an instance of this Mechanic.
     * 
     * @param block The block to create it at.
     * @return The mechanic.
     */
    public T create(BlockLoc block);

    /**
     * Creates an instance of this Mechanic.
     * 
     * @param world The world to create it at.
     * @return The mechanic.
     */
    public T create(World world);
    
    /**
     * Creates an instance of this Mechanic.
     * 
     * @param entity The entity to create it at.
     * @return The mechanic.
     */
    public T create(Entity entity);

    /**
     * Called when the factory is initially registered.
     */
    public void onRegister() throws InvalidFactoryException;

}