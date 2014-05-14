package com.sk89q.craftbook.mechanics.drops;

import java.util.List;

import org.bukkit.entity.EntityType;

public class EntityCustomDropDefinition extends CustomDropDefinition {

    private EntityType entityType;

    /**
     * Instantiate an Entity-Type CustomDrop.
     */
    public EntityCustomDropDefinition(String name, List<DropItemStack> drops, EntityType entityType) {
        super(name, drops);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {

        return entityType;
    }
}