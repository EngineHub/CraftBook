package com.sk89q.craftbook.mechanics.drops;

import com.sk89q.craftbook.mechanics.drops.rewards.DropReward;
import com.sk89q.craftbook.util.TernaryState;
import org.bukkit.entity.EntityType;

import java.util.List;

public class EntityCustomDropDefinition extends CustomDropDefinition {

    private EntityType entityType;

    /**
     * Instantiate an Entity-Type CustomDrop.
     */
    public EntityCustomDropDefinition(String name, List<DropItemStack> drops, List<DropReward> extraRewards, TernaryState silkTouch, EntityType entityType) {
        super(name, drops, extraRewards, silkTouch);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {

        return entityType;
    }
}