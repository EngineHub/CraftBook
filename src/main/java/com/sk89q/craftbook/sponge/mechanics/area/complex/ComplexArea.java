package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeRedstoneMechanicData;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.world.Location;

@Module(moduleName = "Area", onEnable="onInitialize", onDisable="onDisable")
public class ComplexArea extends SpongeBlockMechanic {

    @Override
    public boolean isValid(Location location) {
        return SignUtil.isSign(location) && isMechanicSign((Sign) location.getTileEntity().get());
    }

    public static boolean isMechanicSign(Sign sign) {
        for(String text : getValidSigns())
            if(SignUtil.getTextRaw(sign, 1).equals(text))
                return true;
        return false;
    }

    public static String[] getValidSigns() {
        return new String[]{
                "[Area]",
                "[SaveArea]"
        };
    }

    private static class ComplexAreaData extends SpongeRedstoneMechanicData {
        public String namespace;
    }
}
