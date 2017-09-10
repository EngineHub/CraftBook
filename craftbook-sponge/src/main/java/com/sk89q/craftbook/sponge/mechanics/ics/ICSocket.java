/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
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

import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.createStringOfLength;
import static com.sk89q.craftbook.core.util.documentation.DocumentationGenerator.padToLength;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import com.sk89q.craftbook.core.util.ConfigValue;
import com.sk89q.craftbook.core.util.CraftBookException;
import com.sk89q.craftbook.core.util.documentation.DocumentationGenerator;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.ics.command.SetDataCommand;
import com.sk89q.craftbook.sponge.mechanics.ics.command.ShowDataCommand;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinSet;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.Pins3I3O;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.Pins3ISO;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinsSI3O;
import com.sk89q.craftbook.sponge.mechanics.ics.pinsets.PinsSISO;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeBlockMechanic;
import com.sk89q.craftbook.sponge.st.SelfTriggeringMechanic;
import com.sk89q.craftbook.sponge.st.SpongeSelfTriggerManager;
import com.sk89q.craftbook.sponge.util.SignUtil;
import com.sk89q.craftbook.sponge.util.data.CraftBookKeys;
import com.sk89q.craftbook.sponge.util.data.mutable.ICData;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Module(id = "icsocket", name = "ICSocket", onEnable="onInitialize", onDisable="onDisable")
public class ICSocket extends SpongeBlockMechanic implements SelfTriggeringMechanic, DocumentationProvider {

    static final HashMap<String, PinSet> PINSETS = new HashMap<>();
    private static final Pattern IC_TABLE_PATTERN = Pattern.compile("%IC_TABLE%", Pattern.LITERAL);

    static {
        PINSETS.put("SISO", new PinsSISO());
        PINSETS.put("SI3O", new PinsSI3O());
        PINSETS.put("3ISO", new Pins3ISO());
        PINSETS.put("3I3O", new Pins3I3O());
    }

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    public ConfigValue<Double> maxRadius = new ConfigValue<>("max-radius", "Maximum radius of IC mechanics.", 10d, TypeToken.of(Double.class));

    private Map<Location<World>, IC> loadedICs = new HashMap<>();

    private CommandMapping icCommandMapping;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        maxRadius.load(config);

        if ("true".equalsIgnoreCase(System.getProperty("craftbook.generate-docs"))) {
            ICManager.getICTypes().forEach(DocumentationGenerator::generateDocumentation);
        }

        CommandSpec showDataCommand = CommandSpec.builder()
                .arguments(GenericArguments.optional(GenericArguments.location(Text.of("block"))))
                .executor(new ShowDataCommand(this))
                .build();

        CommandSpec setDataCommand = CommandSpec.builder()
                .arguments(
                        GenericArguments.string(Text.of("variable")),
                        GenericArguments.string(Text.of("value")),
                        GenericArguments.optional(GenericArguments.location(Text.of("block")))
                )
                .executor(new SetDataCommand(this))
                .build();

        CommandSpec dataCommand = CommandSpec.builder()
                .child(showDataCommand, "show")
                .child(setDataCommand, "set")
                .build();

        CommandSpec icCommand = CommandSpec.builder()
                .description(Text.of("Base command for Integrated Circuits."))
                .child(dataCommand, "data")
                .build();

        icCommandMapping = Sponge.getCommandManager().register(CraftBookPlugin.spongeInst(), icCommand, "ic", "ics", "integratedcircuit").orElse(null);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        loadedICs.forEach((worldLocation, ic) -> {
            ic.unload();
            if (ic.getFactory() instanceof SerializedICFactory) {
                ic.getBlock().offer(new ICData(((SerializedICFactory) ic.getFactory()).getData(ic)));
            }
        });
        loadedICs.clear();

        if (icCommandMapping != null) {
            Sponge.getCommandManager().removeMapping(icCommandMapping);
        }
    }

    @Override
    public String getName() {
        return "ICs";
    }

    @Listener
    public void onChangeSign(ChangeSignEvent event, @First Player player) {
        ICType<? extends IC> icType = ICManager.getICType((event.getText().lines().get(1)).toPlain());
        if (icType == null) return;

        List<Text> lines = event.getText().lines().get();
        lines.set(0, Text.of(icType.getShorthand().toUpperCase()));
        lines.set(1, Text.of(SignUtil.getTextRaw(lines.get(1)).toUpperCase()));

        try {
            createICData(event.getTargetTile().getLocation(), lines, player);
            player.sendMessage(Text.of(TextColors.YELLOW, "Created " + icType.getName()));

            event.getText().set(Keys.SIGN_LINES, lines);
        } catch (InvalidICException e) {
            event.setCancelled(true);
            player.sendMessage(Text.of("Failed to create IC. " + e.getMessage()));
        }
    }

    @Listener
    public void onBlockBreak(ChangeBlockEvent.Break event) {
        event.getTransactions().stream().map(transaction -> transaction.getOriginal().getLocation().get()).forEach(location -> {
            if (loadedICs.containsKey(location)) {
                IC ic = loadedICs.remove(location);
                ic.unload();
                if (ic instanceof SelfTriggeringIC) {
                    ((SpongeSelfTriggerManager) CraftBookPlugin.inst().getSelfTriggerManager().get()).unregister(this, location);
                }
            }
        });
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent.Post event) {
        event.getTransactions().forEach(blockSnapshotTransaction -> {
            Location<World> baseLocation = blockSnapshotTransaction.getFinal().getLocation().get();
            BlockState originalState = blockSnapshotTransaction.getOriginal().getExtendedState();
            List<Location<World>> icCheckSpots = new ArrayList<>();
            boolean wasPowered = false;

            if (originalState.getType() == BlockTypes.REDSTONE_WIRE) {
                wasPowered = originalState.get(Keys.POWER).orElse(0) > 0;
                icCheckSpots.addAll(blockSnapshotTransaction.getFinal().get(Keys.WIRE_ATTACHMENTS).map(Map::keySet).orElse(EnumSet.noneOf(Direction.class))
                                .stream().map(baseLocation::getRelative)
                                .collect(Collectors.toList()));
                // TODO REMOVE
                icCheckSpots.add(baseLocation.getRelative(Direction.NORTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.SOUTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.EAST));
                icCheckSpots.add(baseLocation.getRelative(Direction.WEST));
            } else if (originalState.getType() == BlockTypes.POWERED_REPEATER ||
                    originalState.getType() == BlockTypes.UNPOWERED_REPEATER ||
                    originalState.getType() == BlockTypes.POWERED_COMPARATOR ||
                    originalState.getType() == BlockTypes.UNPOWERED_COMPARATOR) {
                //TODO icCheckSpots.add(baseLocation.getRelative(blockSnapshotTransaction.getFinal().getState().get(Keys.DIRECTION).get()));
                wasPowered = originalState.getType() == BlockTypes.POWERED_REPEATER || originalState.getType() == BlockTypes.POWERED_COMPARATOR;

                // TODO REMOVE
                icCheckSpots.add(baseLocation.getRelative(Direction.NORTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.SOUTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.EAST));
                icCheckSpots.add(baseLocation.getRelative(Direction.WEST));
            } else if (originalState.getType() == BlockTypes.LEVER) {
                wasPowered = originalState.get(Keys.POWERED).orElse(false);

                icCheckSpots.add(baseLocation.getRelative(Direction.NORTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.SOUTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.EAST));
                icCheckSpots.add(baseLocation.getRelative(Direction.WEST));
            } else if (originalState.getType() == BlockTypes.REDSTONE_TORCH || originalState.getType() == BlockTypes.UNLIT_REDSTONE_TORCH) {
                wasPowered = originalState.getType() == BlockTypes.REDSTONE_TORCH;

                icCheckSpots.add(baseLocation.getRelative(Direction.NORTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.SOUTH));
                icCheckSpots.add(baseLocation.getRelative(Direction.EAST));
                icCheckSpots.add(baseLocation.getRelative(Direction.WEST));
            }

            for (Location<World> location : icCheckSpots) {
                boolean powered = wasPowered;
                getIC(location).ifPresent(ic -> {
                    int pin = ic.getPinSet().getPinForLocation(ic, baseLocation);

                    if (pin >= 0) {
                        if (powered != ic.getPinSet().getInput(pin, ic)) {
                            ic.setTriggeredPin(pin);
                            Sponge.getScheduler().createTaskBuilder().execute(ic::trigger).submit(CraftBookPlugin.spongeInst().container);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onThink(Location<World> block) {
        Optional<IC> icOptional = getIC(block);

        icOptional.filter(ic -> ic instanceof SelfTriggeringIC)
                    .map(ic -> (SelfTriggeringIC) ic)
                    .ifPresent(SelfTriggeringIC::think);
    }

    @Override
    public boolean isValid(Location<World> location) {
        return SignUtil.isSign(location) && ICManager.getICType(SignUtil.getTextRaw((Sign) location.getTileEntity().get(), 1)) != null;
    }

    public Optional<IC> getIC(Location<World> location) {
        if (loadedICs.get(location) == null) {
            if (location.getBlockType() == BlockTypes.WALL_SIGN) {
                ICType<? extends IC> icType = ICManager.getICType(SignUtil.getTextRaw(location.getTileEntity().get().get(Keys.SIGN_LINES).get().get(1)));
                if (icType != null) {
                    IC ic = icType.getFactory().createInstance((Location<World>) location);
                    if (ic.getFactory() instanceof SerializedICFactory) {
                        SerializedICData data = location.get(CraftBookKeys.IC_DATA).orElse(null);
                        if (data != null) {
                            ((SerializedICFactory) ic.getFactory()).setData(ic, data);
                        } else {
                            CraftBookPlugin.inst().getLogger().warn("Broken IC at " + location.toString());
                            location.removeBlock();
                            return Optional.empty();
                        }
                    }
                    ic.load();
                    loadedICs.put(location, ic);
                    return Optional.of(ic);
                }
            } else {
                return Optional.empty();
            }
        }

        return Optional.ofNullable(loadedICs.get(location));
    }

    private void createICData(Location<World> block, List<Text> lines, Player player) throws InvalidICException {
        if (block.getBlockType() == BlockTypes.WALL_SIGN) {
            ICType<? extends IC> icType = ICManager.getICType(SignUtil.getTextRaw(lines.get(1)));
            if (icType == null) {
                throw new InvalidICException("Invalid IC Type");
            }

            IC ic = icType.getFactory().create(player, lines, block);

            ic.create(player, lines);

            if (icType.getFactory() instanceof SerializedICFactory) {
                block.offer(new ICData(((SerializedICFactory) icType.getFactory()).getData(ic)));
            }

            Sponge.getScheduler().createTaskBuilder().execute(task -> {
                ic.load();
                if (ic instanceof SelfTriggeringIC && (SignUtil.getTextRaw(lines.get(1)).endsWith("S") || (((SelfTriggeringIC) ic).isAlwaysST()))) {
                    ((SpongeSelfTriggerManager) CraftBookPlugin.inst().getSelfTriggerManager().get()).register(this, block);
                }
            }).submit(CraftBookPlugin.spongeInst().getContainer());
        } else {
            throw new InvalidICException("Block is not a sign");
        }
    }

    @Override
    public String getPath() {
        return "mechanics/ics/index";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue[] {
                maxRadius
        };
    }

    @Override
    public String performCustomConversions(String input) {
        StringBuilder icTable = new StringBuilder();

        icTable.append(".. toctree::\n");
        icTable.append("    :hidden:\n");
        icTable.append("    :glob:\n");
        icTable.append("    :titlesonly:\n\n");
        icTable.append("    *\n\n");


        icTable.append("ICs\n");
        icTable.append("===\n\n");

        int idLength = "IC ID".length(),
                shorthandLength = "Shorthand".length(),
                nameLength = "Name".length(),
                descriptionLength = "Description".length(),
                familiesLength = "Family".length(),
                stLength = "Self Triggering".length();

        for(ICType<? extends IC> icType : ICManager.getICTypes()) {
            if((":doc:`" + icType.getModel() + '`').length() > idLength)
                idLength = (":doc:`ics/" + icType.getModel() + '`').length();
            if(icType.getShorthand().length() > shorthandLength)
                shorthandLength = icType.getShorthand().length();
            if(icType.getName().length() > nameLength)
                nameLength = icType.getName().length();
            if(icType.getDescription().length() > descriptionLength)
                descriptionLength = icType.getDescription().length();
            if(icType.getDefaultPinSet().length() > familiesLength)
                familiesLength = icType.getDefaultPinSet().length();
            if((SelfTriggeringIC.class.isAssignableFrom(icType.getFactory().createInstance(null).getClass()) ? "Yes" : "No").length() > stLength)
                stLength = (SelfTriggeringIC.class.isAssignableFrom(icType.getFactory().createInstance(null).getClass()) ? "Yes" : "No").length();
        }

        String border = createStringOfLength(idLength, '=') + ' '
                + createStringOfLength(shorthandLength, '=') + ' '
                + createStringOfLength(nameLength, '=') + ' '
                + createStringOfLength(descriptionLength, '=') + ' '
                + createStringOfLength(familiesLength, '=') + ' '
                + createStringOfLength(stLength, '=');

        icTable.append(border).append('\n');
        icTable.append(padToLength("IC ID", idLength + 1))
                .append(padToLength("Shorthand", shorthandLength + 1))
                .append(padToLength("Name", nameLength + 1))
                .append(padToLength("Description", descriptionLength + 1))
                .append(padToLength("Family", familiesLength + 1))
                .append(padToLength("Self Triggering", stLength + 1))
                .append('\n');
        icTable.append(border).append('\n');
        for(ICType<? extends IC> icType : ICManager.getICTypes()) {
            icTable.append(padToLength(":doc:`" + icType.getModel() + '`', idLength + 1))
                    .append(padToLength(icType.getShorthand(), shorthandLength + 1))
                    .append(padToLength(icType.getName(), nameLength + 1))
                    .append(padToLength(icType.getDescription(), descriptionLength + 1))
                    .append(padToLength(icType.getDefaultPinSet(), familiesLength + 1))
                    .append(padToLength((SelfTriggeringIC.class.isAssignableFrom(icType.getFactory().createInstance(null).getClass()) ? "Yes" : "No"), stLength + 1))
                    .append('\n');
        }
        icTable.append(border).append('\n');

        return IC_TABLE_PATTERN.matcher(input).replaceAll(Matcher.quoteReplacement(icTable.toString()));
    }
}
