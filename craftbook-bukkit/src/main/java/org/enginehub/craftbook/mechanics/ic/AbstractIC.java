/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.mechanics.ic;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.RegexUtil;
import org.enginehub.craftbook.util.SignUtil;

import java.util.Objects;

/**
 * A base abstract IC that all ICs can inherit from.
 *
 * @author sk89q
 */
public abstract class AbstractIC implements IC {

    private final Server server;
    private final ChangedSign sign;
    private final ICFactory factory;

    public AbstractIC(Server server, ChangedSign sign, ICFactory factory) {

        this.factory = factory;
        this.server = server;
        this.sign = sign;
    }

    protected Server getServer() {

        return server;
    }

    @Override
    public ChangedSign getSign() {

        return sign;
    }

    public Location getLocation() {

        return getSign().getBlock().getLocation();
    }

    public Block getBackBlock() {

        return SignUtil.getBackBlock(sign.getBlock());
    }

    public String getLine(int line) {
        return PlainTextComponentSerializer.plainText().serialize(sign.getLine(line));
    }

    public String getRawLine(int line) {
        return PlainTextComponentSerializer.plainText().serialize(sign.getRawLine(line));
    }

    public ICFactory getFactory() {

        return factory;
    }

    public CraftBookPlugin getPlugin() {

        return CraftBookPlugin.inst();
    }

    @Override
    public void onRightClick(Player p) {

        if (p.isSneaking()) {
            ICDocsParser.generateICDocs(CraftBookPlugin.inst().wrapPlayer(p),
                RegexUtil.RIGHT_BRACKET_PATTERN.split(RegexUtil.LEFT_BRACKET_PATTERN.split(getLine(1))[1])[0]);
        }
    }

    @Override
    public void onICBreak(BlockBreakEvent event) {
    }

    @Override
    public void unload() {

        sign.update(false);
    }

    @Override
    public void load() {

    }

    @Override
    public boolean equals(Object o) {

        return o instanceof AbstractIC
            && getSignTitle().equalsIgnoreCase(((AbstractIC) o).getSignTitle())
            && getTitle().equalsIgnoreCase(((AbstractIC) o).getTitle())
            && sign.equals(((AbstractIC) o).sign);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign, getSignTitle(), getTitle());
    }
}
