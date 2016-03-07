/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.craftbook.sponge.mechanics.ics;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinSet;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.Pins3ISO;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinsSISO;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggerManager;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.SpongeMechanicData;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;

import java.util.HashMap;
import java.util.Map.Entry;

@Module(moduleName = "ICSocket", onEnable="onInitialize", onDisable="onDisable")
public class ICSocket extends SpongeBlockMechanic implements SelfTriggeringMechanic, DocumentationProvider {

    public static final HashMap<String, PinSet> PINSETS = new HashMap<>();

    static {
        PINSETS.put("SISO", new PinsSISO());
        PINSETS.put("3ISO", new Pins3ISO());
    }

    /**
     * Gets the IC that is in use by this IC Socket.
     * 
     * @return The IC
     */
    public IC getIC(Location block) {
        return createICData(block).ic;
    }

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();
    }

    @Override
    public String getName() {
        return "ICs";
    }

    @Listener
    public void onChangeSign(ChangeSignEvent event) {
        ICType<? extends IC> icType = ICManager.getICType((event.getText().lines().get(1)).toPlain());
        if (icType == null) return;

        System.out.println(icType.shorthandId);

        event.getText().lines().set(1, Text.of("=" + icType.shorthandId));
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First BlockSnapshot source) {
        if(!SignUtil.isSign(source.getState())) return;

        BaseICData data = createICData(source.getLocation().get());
        if (data == null) return;

        for(Entry<Direction, BlockState> entries : event.getNeighbors().entrySet()) {

            boolean powered = entries.getValue().get(Keys.POWER).orElse(0) > 0;

            if (powered != data.ic.getPinSet().getInput(data.ic.getPinSet().getPinForLocation(data.ic, source.getLocation().get().getRelative(entries.getKey())), data.ic)) {
                data.ic.getPinSet().setInput(data.ic.getPinSet().getPinForLocation(data.ic, source.getLocation().get().getRelative(entries.getKey())), powered, data.ic);
                data.ic.trigger();
            }
        }
    }

    @Override
    public void onThink(Location block) {
        BaseICData data = createICData(block);
        if (data == null) return;
        if (!(data.ic instanceof SelfTriggeringIC)) return;
        ((SelfTriggeringIC) data.ic).think();
    }

    @Override
    public boolean isValid(Location location) {
        return createICData(location) != null;
    }

    public BaseICData createICData(Location block) {
        if (block.getBlockType() == BlockTypes.WALL_SIGN) {
            if(block.getExtent() instanceof Chunk)
                block = ((Chunk) block.getExtent()).getWorld().getLocation(block.getX(), block.getY(), block.getZ());
            Sign sign = ((Sign) block.getTileEntity().get());
            ICType<? extends IC> icType = ICManager.getICType(SignUtil.getTextRaw(sign, 1));
            if (icType == null) return null;

            BaseICData data = getData(BaseICData.class, block);

            if (data.ic == null || !icType.equals(data.ic.type)) {
                // Initialize new IC.
                data.ic = icType.buildIC(block);
                if(data.ic instanceof SelfTriggeringIC && SignUtil.getTextRaw(sign, 1).endsWith("S") ||  SignUtil.getTextRaw(sign, 1).endsWith(" ST"))
                    ((SelfTriggeringIC)data.ic).selfTriggering = true;
            } else if(data.ic.block == null) {
                data.ic.block = block;
                data.ic.type = icType;
            }

            if (data.ic instanceof SelfTriggeringIC && (((SelfTriggeringIC) data.ic).canThink())) SelfTriggerManager.register(this, block);

            return data;
        }

        return null;
    }

    @Override
    public String getPath() {
        return "mechanics/ics";
    }

    @Override
    public String[] getMainDocumentation() {
        return new String[0];
    }

    public static class BaseICData extends SpongeMechanicData {
        public IC ic;
    }
}
