package com.sk89q.craftbook.mechanics.drops;

import java.util.List;

import org.bukkit.entity.EntityType;

import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;

public class EntityCustomDropDefinition extends CustomDropDefinition {

    private EntityType entityType;

    /**
     * Instantiate an Entity-Type CustomDrop.
     */
    public EntityCustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, EntityType entityType) {
        super(name, drops, extraRewards);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {

        return entityType;
    }
}