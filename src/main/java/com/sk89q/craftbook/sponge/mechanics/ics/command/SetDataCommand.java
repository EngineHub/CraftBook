package com.sk89q.craftbook.sponge.mechanics.ics.command;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICSocket;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;

public class SetDataCommand implements CommandExecutor {

    private ICSocket icSocket;

    public SetDataCommand(ICSocket icSocket) {
        this.icSocket = icSocket;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Location<World> icLocation = args.<Location<World>>getOne("block").orElseGet(() -> {
            if (src instanceof Player) {
                BlockRay<World> blockRay = BlockRay.from((Player) src).stopFilter(BlockRay.onlyAirFilter()).build();
                return blockRay.end().map(BlockRayHit::getLocation).orElse(null);

            }
            return null;
        });
        if (icLocation == null) {
            src.sendMessage(Text.of(TextColors.RED, "Location must be provided!"));
            return CommandResult.empty();
        }
        IC ic = icSocket.getIC(icLocation).orElse(null);
        if (ic == null) {
            System.out.println(icLocation.toString());
            src.sendMessage(Text.of(TextColors.RED, "Location not an IC!"));
            return CommandResult.empty();
        }
        if (ic.getFactory() instanceof SerializedICFactory) {
            SerializedICFactory factory = (SerializedICFactory) ic.getFactory();
            SerializedICData data = factory.getData(ic);

            String variable = args.<String>getOne("variable").orElse("");
            String value = args.<String>getOne("value").orElse("");

            try {
                Field field = data.getClass().getField(variable);
                try {
                    // TODO Make this a generic system that supports more than "String".
                    if (field.getType().getSimpleName().equalsIgnoreCase("String")) {
                        field.set(data, value);
                    } else if (CatalogType.class.isAssignableFrom(field.getType())) {
                        field.set(data, Sponge.getRegistry().getType((Class<? extends CatalogType>) field.getType(), value).get());
                    } else {
                        System.out.println(field.getType().getName());
                        src.sendMessage(Text.of("Unknown Type."));
                        return CommandResult.empty();
                    }

                    src.sendMessage(Text.of("Assigned value."));

                    // Call load again so that the IC can update for the new data.
                    ic.load();
                } catch (Throwable t) {
                    src.sendMessage(Text.of(TextColors.RED, "Invalid value!"));
                    return CommandResult.empty();
                }
            } catch (Throwable t) {
                src.sendMessage(Text.of(TextColors.RED, "Unknown variable!"));
                return CommandResult.empty();
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "IC does not store extra data!"));
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
