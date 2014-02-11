package com.sk89q.craftbook.mech.drops;

import java.util.List;

import org.bukkit.entity.EntityType;

public class EntityCustomDropDefinition extends CustomDropDefinition {

    private EntityType entityType;

    /**
     * Instantiate an Entity-Type CustomDrop.
     */
    public EntityCustomDropDefinition(List<DropItemStack> drops, EntityType entityType) {
        super(drops);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {

        return entityType;
    }
}