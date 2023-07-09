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

package org.enginehub.craftbook;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.util.ParsingUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A ChangedSign represents a single side of a sign block.
 */
public class ChangedSign {
    private final Block block;
    private final Side side;

    private Sign sign;
    private Component[] lines;
    private Component[] oldLines;

    private ChangedSign(Block block, Side side, Component[] lines, CraftBookPlayer player) {
        this.block = block;
        this.side = side;

        if (lines == null) {
            this.flushLines();
        } else {
            this.lines = lines;
            this.oldLines = new Component[this.lines.length];
            System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);
        }

        if (player != null) {
            checkPlayerVariablePermissions(player);
        }
    }

    public void checkPlayerVariablePermissions(CraftBookPlayer player) {
        if (this.lines != null && VariableManager.instance != null) {
            for (int i = 0; i < 4; i++) {
                Component line = this.lines[i];
                for (VariableKey variableKey : VariableManager.getPossibleVariables(line, player)) {
                    if (!variableKey.hasPermission(player, "use")) {
                        TextReplacementConfig config = TextReplacementConfig.builder()
                            .matchLiteral("%" + variableKey.getOriginalForm() + "%")
                            .replacement("").build();
                        setLine(i, line.replaceText(config));
                    }
                }
            }
        }
    }

    public Block getBlock() {
        return block;
    }

    public Sign getSign() {
        if (this.sign == null) {
            this.sign = (Sign) this.block.getState(false);
        }
        return sign;
    }

    public SignSide getSignSide() {
        return getSign().getSide(this.side);
    }

    public Side getSide() {
        return this.side;
    }

    public Material getType() {
        return block.getType();
    }

    public int getX() {
        return this.block.getX();
    }

    public int getY() {
        return this.block.getY();
    }

    public int getZ() {
        return this.block.getZ();
    }

    public Component[] getLines() {
        return this.lines;
    }

    public Component getLine(int index) throws IndexOutOfBoundsException {
        return this.getLine(index, null);
    }

    public Component getLine(int index, @Nullable Player player) throws IndexOutOfBoundsException {
        return ParsingUtil.parseLine(this.lines[index], player);
    }

    public Component getRawLine(int index) throws IndexOutOfBoundsException {
        return this.lines[index];
    }

    public void setLine(int index, Component line) throws IndexOutOfBoundsException {
        this.lines[index] = line;
    }

    public void setType(Material type) {
        block.setType(type);
    }

    public boolean update(boolean force) {
        if (!hasChanged() && !force) {
            return false;
        }

        for (int i = 0; i < 4; i++) {
            getSignSide().line(i, lines[i]);
        }
        System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);

        return getSign().update(force, false);
    }

    public void setLines(Component[] lines) {
        this.lines = lines;
    }

    public void setOldLines(Component[] oldLines) {
        this.oldLines = oldLines;
    }

    public boolean hasChanged() {
        for (int i = 0; i < 4; i++) {
            if (!Objects.equals(this.oldLines[i], this.lines[i])) {
                return true;
            }
        }

        return false;
    }

    public void flushLines() {
        this.sign = null;
        this.lines = this.getSignSide().lines().toArray(new Component[0]);

        if (this.oldLines == null) {
            this.oldLines = new Component[lines.length];
        }

        System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);
    }

    public boolean updateSign(ChangedSign sign) {
        if (!equals(sign)) {
            flushLines();
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChangedSign other) {
            return Objects.equals(other.getType(), getType())
                && other.getSide() == getSide()
                && other.getX() == getX()
                && other.getY() == getY()
                && other.getZ() == getZ()
                && Objects.equals(other.getBlock().getWorld().getUID(), getBlock().getWorld().getUID())
                && Arrays.equals(other.getLines(), getLines());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), side, Arrays.hashCode(this.lines), getX(), getY(), getZ(), block.getWorld().getUID());
    }

    @Override
    public String toString() {
        return Arrays.stream(this.lines).map(PlainTextComponentSerializer.plainText()::serialize).collect(Collectors.joining("|"));
    }

    public static ChangedSign create(@Nonnull Sign sign, @Nonnull Side side) {
        return create(sign.getBlock(), side, sign.getSide(side).lines().toArray(new Component[0]), null);
    }

    public static ChangedSign create(@Nonnull Sign sign, @Nonnull Side side, @Nullable CraftBookPlayer player) {
        return create(sign.getBlock(), side, sign.getSide(side).lines().toArray(new Component[0]), player);
    }

    public static ChangedSign create(@Nonnull Block block, @Nonnull Side side) {
        return create(block, side, null, null);
    }

    public static ChangedSign create(@Nonnull Block block, @Nonnull Side side, @Nullable Component[] lines, @Nullable CraftBookPlayer player) {
        Preconditions.checkNotNull(block, "block");
        Preconditions.checkNotNull(side, "side");

        return new ChangedSign(block, side, lines, player);
    }
}
