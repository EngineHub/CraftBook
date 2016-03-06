package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeSignMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongePermissionNode;
import com.sk89q.craftbook.sponge.util.SpongeRedstoneMechanicData;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Area", onEnable="onInitialize", onDisable="onDisable")
public class ComplexArea extends SpongeSignMechanic {

    @Override
    public boolean isValid(Location location) {
        return SignUtil.isSign(location) && isMechanicSign((Sign) location.getTileEntity().get());
    }

    public String[] getValidSigns() {
        return new String[]{
                "[Area]",
                "[SaveArea]"
        };
    }

    @Override
    public SpongePermissionNode getCreatePermission() {
        return null;
    }

    private static class ComplexAreaData extends SpongeRedstoneMechanicData {
        public String namespace;
    }
}
