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
import com.sk89q.craftbook.core.util.documentation.DocumentationGenerator;
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

import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.createStringOfLength;
import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.padToLength;

@Module(moduleName = "ICSocket", onEnable="onInitialize", onDisable="onDisable")
public class ICSocket extends SpongeBlockMechanic implements SelfTriggeringMechanic, DocumentationProvider {

    static final HashMap<String, PinSet> PINSETS = new HashMap<>();

    static {
        PINSETS.put("SISO", new PinsSISO());
        PINSETS.put("3ISO", new Pins3ISO());
    }

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        if ("true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs"))) {
            ICManager.getICTypes().forEach(DocumentationGenerator::generateDocumentation);
        }
    }

    @Override
    public String getName() {
        return "ICs";
    }

    @Listener
    public void onChangeSign(ChangeSignEvent event) {
        ICType<? extends IC> icType = ICManager.getICType((event.getText().lines().get(1)).toPlain());
        if (icType == null) return;

        event.getText().lines().set(1, Text.of('=' + icType.shorthandId));
    }

    @Listener
    public void onBlockUpdate(NotifyNeighborBlockEvent event, @First BlockSnapshot source) {
        if(!SignUtil.isSign(source.getState())) return;

        BaseICData data = createICData(source.getLocation().get());
        if (data == null) return;

        for(Entry<Direction, BlockState> entries : event.getNeighbors().entrySet()) {

            boolean powered = entries.getValue().get(Keys.POWER).orElse(0) > 0;

            if (powered != PinSet.getInput(data.ic.getPinSet().getPinForLocation(data.ic, source.getLocation().get().getRelative(entries.getKey())), data.ic)) {
                PinSet.setInput(data.ic.getPinSet().getPinForLocation(data.ic, source.getLocation().get().getRelative(entries.getKey())), powered, data.ic);
                data.ic.trigger();
            }
        }
    }

    @Override
    public void onThink(Location<?> block) {
        BaseICData data = createICData(block);
        if (data == null) return;
        if (!(data.ic instanceof SelfTriggeringIC)) return;
        ((SelfTriggeringIC) data.ic).think();
    }

    @Override
    public boolean isValid(Location<?> location) {
        return createICData(location) != null;
    }

    private BaseICData createICData(Location<?> block) {
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
    public String performCustomConversions(String input) {
        StringBuilder icTable = new StringBuilder();

        icTable.append("ICs\n");
        icTable.append("===\n\n");

        int idLength = "IC ID".length(),
                shorthandLength = "Shorthand".length(),
                nameLength = "Name".length(),
                descriptionLength = "Description".length(),
                familiesLength = "Family".length(),
                stLength = "Self Triggering".length();

        for(ICType<? extends IC> icType : ICManager.getICTypes()) {
            if(icType.modelId.length() > idLength)
                idLength = icType.modelId.length();
            if(icType.shorthandId.length() > shorthandLength)
                shorthandLength = icType.shorthandId.length();
            if(icType.name.length() > nameLength)
                nameLength = icType.name.length();
            if(icType.description.length() > descriptionLength)
                descriptionLength = icType.description.length();
            if(icType.getDefaultPinSet().length() > familiesLength)
                familiesLength = icType.getDefaultPinSet().length();
            if((SelfTriggeringIC.class.isAssignableFrom(icType.icClass) ? "Yes" : "No").length() > stLength)
                stLength = (SelfTriggeringIC.class.isAssignableFrom(icType.icClass) ? "Yes" : "No").length();
        }

        String border = createStringOfLength(idLength, '=') + ' '
                + createStringOfLength(shorthandLength, '=') + ' '
                + createStringOfLength(nameLength, '=') + ' '
                + createStringOfLength(descriptionLength, '=') + ' '
                + createStringOfLength(familiesLength, '=') + ' '
                + createStringOfLength(stLength, '=');

        icTable.append(border + "\n");
        icTable.append(padToLength("IC ID", idLength+1)
                + padToLength("Shorthand", shorthandLength+1)
                + padToLength("Name", nameLength+1)
                + padToLength("Description", descriptionLength+1)
                + padToLength("Family", familiesLength+1)
                + padToLength("Self Triggering", stLength+1) + "\n");
        icTable.append(border + "\n");
        for(ICType<? extends IC> icType : ICManager.getICTypes()) {
            icTable.append(padToLength(icType.modelId, idLength+1)
                    + padToLength(icType.shorthandId, shorthandLength+1)
                    + padToLength(icType.name, nameLength+1)
                    + padToLength(icType.description, descriptionLength+1)
                    + padToLength(icType.getDefaultPinSet(), familiesLength+1)
                    + padToLength((SelfTriggeringIC.class.isAssignableFrom(icType.icClass) ? "Yes" : "No"), stLength+1) + "\n");
        }
        icTable.append(border + "\n");

        return input.replace("%IC_TABLE%", icTable.toString());
    }

    public static class BaseICData extends SpongeMechanicData {
        public IC ic;
    }
}
