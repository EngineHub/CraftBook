package com.sk89q.craftbook;

import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.mechanics.variables.VariableCommands;
import com.sk89q.craftbook.mechanics.variables.VariableManager;
import com.sk89q.craftbook.util.ParsingUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalWorld;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.block.Sign;

import java.util.Arrays;
import java.util.Locale;

public class ChangedSign {

    private Sign sign;
    private String[] lines;

    public ChangedSign(Sign sign, String[] lines, LocalPlayer player) {

        this(sign, lines);

        if(lines != null && VariableManager.instance != null) {
            for(int i = 0; i < 4; i++) {

                String line = lines[i];
                for(String var : ParsingUtil.getPossibleVariables(line)) {

                    String key;

                    if(var.contains("|")) {
                        String[] bits = RegexUtil.PIPE_PATTERN.split(var);
                        key = bits[0];
                    } else
                        key = "global";

                    if(player != null && !VariableCommands.hasVariablePermission(((BukkitPlayer) player).getPlayer(), key, var, "use"))
                        setLine(i, StringUtils.replace(line, '%' + key + '|' + var + '%', ""));
                }
            }
        }
    }

    public ChangedSign(Sign sign, String[] lines) {

        Validate.notNull(sign);

        this.sign = sign;
        this.lines = lines;
    }

    public BlockWorldVector getBlockVector() {

        return BukkitUtil.toWorldVector(sign.getBlock());
    }

    public Sign getSign() {

        return sign;
    }

    public Material getType() {

        return sign.getType();
    }

    public byte getLightLevel() {

        return sign.getLightLevel();
    }

    public LocalWorld getLocalWorld() {

        return BukkitUtil.getLocalWorld(sign.getWorld());
    }

    public int getX() {

        return sign.getX();
    }

    public int getY() {

        return sign.getY();
    }

    public int getZ() {

        return sign.getZ();
    }

    public String[] getLines() {

        return lines;
    }

    public String getLine(int index) throws IndexOutOfBoundsException {

        return ParsingUtil.parseLine(lines[index], null);
    }

    public String getRawLine(int index) throws IndexOutOfBoundsException {

        return lines[index];
    }

    public void setLine(int index, String line) throws IndexOutOfBoundsException {

        lines[index] = line;
    }

    public void setType(Material type) {

        sign.setType(type);
    }

    public boolean update(boolean force) {

        if(!hasChanged() && !force)
            return false;
        for(int i = 0; i < 4; i++)
            sign.setLine(i, lines[i]);
        return sign.update(force);
    }

    public byte getRawData() {

        return sign.getRawData();
    }

    public void setRawData(byte b) {

        sign.setRawData(b);
    }

    public void setLines(String[] lines) {

        this.lines = lines;
    }

    public boolean hasChanged () {

        boolean ret = false;
        try {
            for(int i = 0; i < 4; i++)
                if(!sign.getLine(i).equals(lines[i])) {
                    ret = true;
                    break;
                }
        }
        catch(Exception ignored){}
        return ret;
    }

    public void flushLines () {

        lines = sign.getLines();
    }

    public boolean updateSign(ChangedSign sign) {

        if(!equals(sign)) {
            this.sign = sign.sign;
            flushLines();
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {

        if(o instanceof ChangedSign) {
            if(((ChangedSign) o).getType() != getType())
                return false;
            if(((ChangedSign) o).getRawData() != getRawData())
                return false;
            for(int i = 0; i < 4; i++)
                if(!((ChangedSign) o).getRawLine(i).equals(getRawLine(i)))
                    return false;
            if(((ChangedSign) o).getX() != getX())
                return false;
            if(((ChangedSign) o).getY() != getY())
                return false;
            if(((ChangedSign) o).getZ() != getZ())
                return false;
            if(!((ChangedSign) o).getLocalWorld().getName().equals(getLocalWorld().getName()))
                return false;
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        return (getType().hashCode() * 1103515245 + 12345
                ^ Arrays.hashCode(lines) * 1103515245 + 12345
                ^ getX() * 1103515245 + 12345
                ^ getY() * 1103515245 + 12345
                ^ getZ() * 1103515245 + 12345
                ^ getLocalWorld().getName().hashCode() * 1103515245 + 12345
                ^ getRawData() * 1103515245 + 12345) * 1103515245 + 12345;
    }

    @Override
    public String toString() {
        return lines[0] + '|' + lines[1] + '|' + lines[2] + '|' + lines[3];
    }

    public boolean hasVariable(String var) {

        if(VariableManager.instance == null) return false;

        var = var.toLowerCase(Locale.ENGLISH);
        return lines[0].toLowerCase(Locale.ENGLISH).contains('%' + var + '%') || lines[1].toLowerCase(Locale.ENGLISH).contains('%' + var + '%') || lines[2].toLowerCase(Locale.ENGLISH).contains('%' + var + '%') || lines[3].toLowerCase(Locale.ENGLISH).contains('%' + var + '%');
    }
}
