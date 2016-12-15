package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.SelfTriggeringIC;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class WirelessReceiver extends SelfTriggeringIC {

    private String wideband;
    private String shortband;

    private transient Tuple<String, String> cachedTuple;

    public WirelessReceiver(ICType<? extends IC> type, Location<World> block) {
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
    public void think() {
        trigger();
    }

    @Override
    public void trigger() {
        getPinSet().setOutput(0, WirelessTransmitter.wirelessStates.contains(cachedTuple), this);
    }
}
