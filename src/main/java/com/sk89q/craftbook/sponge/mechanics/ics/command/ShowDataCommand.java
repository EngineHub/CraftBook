package com.sk89q.craftbook.sponge.mechanics.ics.command;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICSocket;
import com.sk89q.craftbook.sponge.mechanics.ics.SerializedICData;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.SerializedICFactory;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ShowDataCommand implements CommandExecutor {

    private ICSocket icSocket;

    public ShowDataCommand(ICSocket icSocket) {
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
            Field[] fields = data.getClass().getFields();

            PaginationList.Builder builder = PaginationList.builder()
                    .title(Text.of(TextColors.LIGHT_PURPLE, "IC Data"));
            List<Text> variables = new ArrayList<>();

            for (Field field : fields) {
                try {
                    variables.add(Text.of(TextColors.BLUE, field.getName(), TextColors.GRAY, "=", TextColors.YELLOW, field.get(data).toString()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            builder.contents(variables);

            builder.sendTo(src);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "IC does not store extra data!"));
            return CommandResult.empty();
        }

        return CommandResult.success();
    }
}
