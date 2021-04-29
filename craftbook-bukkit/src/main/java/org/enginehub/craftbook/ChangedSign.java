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

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.enginehub.craftbook.mechanics.variables.VariableKey;
import org.enginehub.craftbook.mechanics.variables.VariableManager;
import org.enginehub.craftbook.util.ParsingUtil;

import java.util.Arrays;
import java.util.Objects;

public class ChangedSign {

    private final Block block;
    private Sign sign;
    private String[] lines;
    private String[] oldLines;

    public ChangedSign(Block block, String[] lines, CraftBookPlayer player) {
        this(block, lines);

        if (player != null) {
            checkPlayerVariablePermissions(player);
        }
    }

    public ChangedSign(Block block, String[] lines) {
        Validate.notNull(block);

        this.block = block;

        if (lines == null) {
            this.flushLines();
        } else {
            this.lines = lines;
            this.oldLines = new String[this.lines.length];
            System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);
        }
    }

    public void checkPlayerVariablePermissions(CraftBookPlayer player) {
        if (this.lines != null && VariableManager.instance != null) {
            for (int i = 0; i < 4; i++) {
                String line = this.lines[i];
                for (VariableKey variableKey : VariableManager.getPossibleVariables(line, player)) {
                    if (!variableKey.hasPermission(player, "use")) {
                        setLine(i, line.replace('%' + variableKey.getOriginalForm() + '%', ""));
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

    public String[] getLines() {
        return this.lines;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {
        return ParsingUtil.parseLine(this.lines[index], null);
    }

    public String getRawLine(int index) throws IndexOutOfBoundsException {
        return this.lines[index];
    }

    public void setLine(int index, String line) throws IndexOutOfBoundsException {
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
            getSign().setLine(i, lines[i]);
        }
        System.arraycopy(this.lines, 0, this.oldLines, 0, this.lines.length);

        return getSign().update(force, false);
    }

    public void setLines(String[] lines) {
        this.lines = lines;
    }

    public void setOldLines(String[] oldLines) {
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
        this.lines = this.getSign().getLines();

        if (this.oldLines == null) {
            this.oldLines = new String[lines.length];
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
        if (o instanceof ChangedSign) {
            ChangedSign other = (ChangedSign) o;
            return Objects.equals(other.getType(), getType())
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
        return Objects.hash(getType(), this.lines, getX(), getY(), getZ(), block.getWorld().getUID());
    }

    @Override
    public String toString() {
        return lines[0] + '|' + lines[1] + '|' + lines[2] + '|' + lines[3];
    }
}
