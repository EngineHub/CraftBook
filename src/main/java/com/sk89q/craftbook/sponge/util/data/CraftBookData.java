package com.sk89q.craftbook.sponge.util.data;

import com.sk89q.craftbook.sponge.util.data.builder.BlockBagDataManipulatorBuilder;
import com.sk89q.craftbook.sponge.util.data.builder.LastPowerDataManipulatorBuilder;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableBlockBagData;
import com.sk89q.craftbook.sponge.util.data.immutable.ImmutableLastPowerData;
import com.sk89q.craftbook.sponge.util.data.mutable.BlockBagData;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.Sponge;

public class CraftBookData {

    public static void registerData() {
        Sponge.getDataManager().register(LastPowerData.class, ImmutableLastPowerData.class, new LastPowerDataManipulatorBuilder());
        Sponge.getDataManager().register(BlockBagData.class, ImmutableBlockBagData.class, new BlockBagDataManipulatorBuilder());
    }
}
