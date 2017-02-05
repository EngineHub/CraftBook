package com.sk89q.craftbook.sponge.mechanics.powerable;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.LastPowerData;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Jukebox;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Module(moduleId = "redstonejukebox", moduleName = "RedstoneJukebox", onEnable="onInitialize", onDisable="onDisable")
public class RedstoneJukebox extends SimplePowerable implements DocumentationProvider {

    @Override
    public String getPath() {
        return "mechanics/jukebox";
    }

    @Override
    public boolean isValid(Location<?> location) {
        return location.getBlockType() == BlockTypes.JUKEBOX;
    }

    @Override
    public void updateState(Location<?> location, boolean powered) {
        Jukebox jukebox = (Jukebox) location.getTileEntity().get();
        jukebox.offer(new LastPowerData(powered ? 15 : 0));

        if (powered) {
            jukebox.playRecord();
        } else {
            jukebox.stopPlaying();
        }
    }

    @Override
    public boolean getState(Location<?> location) {
        return location.get(CraftBookKeys.LAST_POWER).orElse(0) > 0;
    }
}
