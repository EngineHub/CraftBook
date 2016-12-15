package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous;

import com.google.common.collect.Sets;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Set;

public class WirelessTransmitter extends IC {

    public static final Set<Tuple<String, String>> wirelessStates = Sets.newHashSet();

    private String wideband;
    private String shortband;

    private transient Tuple<String, String> cachedTuple;

    public WirelessTransmitter(ICType<? extends IC> type, Location<World> block) {
        super(type, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        if (SignUtil.getTextRaw(lines.get(2)).length() == 0) {
            throw new InvalidICException("A band must be supplied on the 3rd line!");
        }

        shortband = SignUtil.getTextRaw(lines.get(2));
        if (SignUtil.getTextRaw(lines.get(3)).length() == 0) {
            wideband = player.getUniqueId().toString();
        } else {
            wideband = SignUtil.getTextRaw(lines.get(3));
        }
    }

    public void load() {
        cachedTuple = new Tuple<>(shortband, wideband);
    }

    @Override
    public void trigger() {
        boolean enable = getPinSet().getInput(0, this);
        if (enable) {
            wirelessStates.add(cachedTuple);
        } else {
            wirelessStates.remove(cachedTuple);
        }
    }
}
