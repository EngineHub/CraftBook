package com.sk89q.craftbook.mech.ic;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.Colors;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.MinecraftUtil;
import com.sk89q.craftbook.util.RedstoneUtil;
import com.sk89q.craftbook.util.Vector;

/**
 * IC types.
 * 
 * @author Lymia
 */
public enum ICType {
    /**
     * Zero input, single output
     */
    ZISO("ZISO", true) {    
        public void think(ServerInterface s, WorldInterface w, Vector v, SignInterface t, IC i) {
            Vector outputVec = MinecraftUtil.getWallSignBack(w, v, 2);
            Vector backVec = MinecraftUtil.getWallSignBack(w, v, 1);

            Signal[] in = new Signal[0];

            Signal[] out = new Signal[1];
            out[0] = new Signal(RedstoneUtil.getOutput(w, outputVec));

            ChipState chip = new ChipState(s, w, v, backVec.toBlockVector(), in, out, t);

            i.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, outputVec, out[0].is());
            }
        }
    },
    /**
     * Single input, single output
     */
    SISO("SISO") {
        public void think(ServerInterface s, WorldInterface w, Vector pt, Vector changedRedstoneInput, 
                SignInterface sign, IC sisoIC) {
            Vector outputVec = MinecraftUtil.getWallSignBack(w, pt, 2);
            Vector in0 = MinecraftUtil.getWallSignBack(w, pt, -1);
            Vector backVec = MinecraftUtil.getWallSignBack(w, pt, 1);

            Signal[] in = new Signal[1];
            in[0] = new Signal(RedstoneUtil.isHighBinary(w, in0, true),
                    changedRedstoneInput.equals(in0));

            Signal[] out = new Signal[1];
            out[0] = new Signal(RedstoneUtil.getOutput(w, outputVec));

            ChipState chip = new ChipState(s, w, pt, backVec.toBlockVector(), in, out, sign);

            sisoIC.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, outputVec, chip.getOut(1).is());
            }

            if (chip.hasErrored()) {
                sign.setLine2(Colors.GOLD + sign.getLine2());
                sign.allowUpdate();
            }
        }
    },
    /**
     * Single input, triple output
     */
    SI3O("SI3O") {
        public void think(ServerInterface s, WorldInterface w, Vector pt, Vector changedRedstoneInput, 
                SignInterface sign, IC si3oIC) {
            Vector backVec = MinecraftUtil.getWallSignBack(w, pt, 1);
            Vector backShift = backVec.subtract(pt);
            Vector in0 = MinecraftUtil.getWallSignBack(w, pt, -1);
            Vector output1Vec = MinecraftUtil.getWallSignBack(w, pt, 2);
            Vector output2Vec = MinecraftUtil.getWallSignSide(w, pt, 1).add(backShift);
            Vector output3Vec = MinecraftUtil.getWallSignSide(w, pt, -1).add(backShift);

            Signal[] in = new Signal[1];
            in[0] = new Signal(RedstoneUtil.isHighBinary(w, in0, true),
                    changedRedstoneInput.equals(in0));

            Signal[] out = new Signal[3];
            out[0] = new Signal(RedstoneUtil.getOutput(w, output1Vec));
            out[1] = new Signal(RedstoneUtil.getOutput(w, output2Vec));
            out[2] = new Signal(RedstoneUtil.getOutput(w, output3Vec));

            ChipState chip = new ChipState(s, w, pt, backVec.toBlockVector(), in, out, sign);

            // The most important part...
            si3oIC.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, output1Vec, chip.getOut(1).is());
                RedstoneUtil.setOutput(w, output2Vec, chip.getOut(2).is());
                RedstoneUtil.setOutput(w, output3Vec, chip.getOut(3).is());
            }

            if (chip.hasErrored()) {
                sign.setLine2(Colors.GOLD + sign.getLine2());
                sign.allowUpdate();
            }
        }
    },
    /**
     * Triple input, single output
     */
    _3ISO("3ISO") {
        public void think(ServerInterface s, WorldInterface w, Vector pt, Vector changedRedstoneInput, 
                SignInterface sign, IC _3isoIC) {
            Vector backVec = MinecraftUtil.getWallSignBack(w, pt, 1);
            Vector outputVec = MinecraftUtil.getWallSignBack(w, pt, 2);
            Vector input1Vec = MinecraftUtil.getWallSignBack(w, pt, -1);
            Vector input2Vec = MinecraftUtil.getWallSignSide(w, pt, 1);
            Vector input3Vec = MinecraftUtil.getWallSignSide(w, pt, -1);

            Signal[] in = new Signal[3];
            in[0] = new Signal(RedstoneUtil.isHighBinary(w, input1Vec, true),
                    changedRedstoneInput.equals(input1Vec));
            in[1] = new Signal(RedstoneUtil.isHighBinary(w, input2Vec, true),
                    changedRedstoneInput.equals(input2Vec));
            in[2] = new Signal(RedstoneUtil.isHighBinary(w, input3Vec, true),
                    changedRedstoneInput.equals(input3Vec));

            Signal[] out = new Signal[1];
            out[0] = new Signal(RedstoneUtil.getOutput(w, outputVec));

            ChipState chip = new ChipState(s, w, pt, backVec.toBlockVector(), in, out, sign);

            // The most important part...
            _3isoIC.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, outputVec, chip.getOut(1).is());
            }

            if (chip.hasErrored()) {
                sign.setLine2(Colors.GOLD + sign.getLine2());
                sign.allowUpdate();
            }
        }
    },
    /**
     * Triple input, triple output
     */
    _3I3O("3I3O") {
        public void think(ServerInterface s, WorldInterface w, Vector pt, Vector changedRedstoneInput, 
                SignInterface sign, IC _3i3oIC) {
            Vector backVec = MinecraftUtil.getWallSignBack(w, pt, 1);
            Vector backShift = MinecraftUtil.getWallSignBack(w, pt, 2).subtract(pt);

            Vector out0 = MinecraftUtil.getWallSignBack(w, pt, 3);
            Vector out1 = MinecraftUtil.getWallSignSide(w, pt, 1).add(backShift);
            Vector out2 = MinecraftUtil.getWallSignSide(w, pt, -1).add(backShift);

            Vector in0 = MinecraftUtil.getWallSignBack(w, pt, -1);
            Vector in1 = MinecraftUtil.getWallSignSide(w, pt, 1);
            Vector in2 = MinecraftUtil.getWallSignSide(w, pt, -1);

            Signal[] in = new Signal[3];
            in[0] = new Signal(RedstoneUtil.isHighBinary(w, in0, true),
                    changedRedstoneInput.equals(in0));
            in[1] = new Signal(RedstoneUtil.isHighBinary(w, in1, true),
                    changedRedstoneInput.equals(in1));
            in[2] = new Signal(RedstoneUtil.isHighBinary(w, in2, true),
                    changedRedstoneInput.equals(in2));

            Signal[] out = new Signal[3];
            out[0] = new Signal(RedstoneUtil.getOutput(w, out0));
            out[1] = new Signal(RedstoneUtil.getOutput(w, out1));
            out[2] = new Signal(RedstoneUtil.getOutput(w, out2));

            ChipState chip = new ChipState(s, w, pt, backVec.toBlockVector(), in, out, sign);

            // The most important part...
            _3i3oIC.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, out0, chip.getOut(1).is());
                RedstoneUtil.setOutput(w, out1, chip.getOut(2).is());
                RedstoneUtil.setOutput(w, out2, chip.getOut(3).is());
            }

            if (chip.hasErrored()) {
                sign.setLine2(Colors.GOLD + sign.getLine2());
                sign.allowUpdate();
            }
        }
    },
    /**
     * Variable input, variable output
     */
    VIVO("VIVO") {
        public void think(ServerInterface s, WorldInterface w, Vector pt, Vector changedRedstoneInput, 
                SignInterface sign, IC vivoIC) {
            Vector backVec = MinecraftUtil.getWallSignBack(w, pt, 1);
            Vector backShift = backVec.subtract(pt);

            Vector out0 = MinecraftUtil.getWallSignBack(w, pt, 2);
            Vector out1 = MinecraftUtil.getWallSignSide(w, pt, 1).add(backShift);
            Vector out2 = MinecraftUtil.getWallSignSide(w, pt, -1).add(backShift);

            Vector in0 = MinecraftUtil.getWallSignBack(w, pt, -1);
            Vector in1 = MinecraftUtil.getWallSignSide(w, pt, 1);
            Vector in2 = MinecraftUtil.getWallSignSide(w, pt, -1);

            boolean hasOut1 = w.getId(out1) == BlockType.LEVER;
            boolean hasOut2 = w.getId(out2) == BlockType.LEVER;

            Signal[] in = new Signal[3];
            Signal[] out = new Signal[3];

            out[0] = new Signal(RedstoneUtil.getOutput(w, out0));
            in[0] = new Signal(RedstoneUtil.isHighBinary(w, in0, true),
                    changedRedstoneInput.equals(in0));

            if (hasOut1) {
                out[1] = new Signal(RedstoneUtil.getOutput(w, out1));
                in[1] = new Signal(false);
            } else {
                out[1] = new Signal(false);
                in[1] = new Signal(RedstoneUtil.isHighBinary(w, in1, true),
                        changedRedstoneInput.equals(in1));
            }

            if (hasOut2) {
                out[2] = new Signal(RedstoneUtil.getOutput(w, out2));
                in[2] = new Signal(false);
            } else {
                out[2] = new Signal(false);
                in[2] = new Signal(RedstoneUtil.isHighBinary(w, in2, true),
                        changedRedstoneInput.equals(in2));
            }

            ChipState chip = new ChipState(s, w, pt, backVec.toBlockVector(), in, out, sign);

            // The most important part...
            vivoIC.think(chip);

            if (chip.isModified()) {
                RedstoneUtil.setOutput(w, out0, chip.getOut(1).is());
                if (hasOut1)
                    RedstoneUtil.setOutput(w, out1, chip.getOut(2).is());
                if (hasOut2)
                    RedstoneUtil.setOutput(w, out2, chip.getOut(3).is());
            }

            if (chip.hasErrored()) {
                sign.setLine2(Colors.GOLD + sign.getLine2());
                sign.allowUpdate();
            }
        }
    };

    public final String name;
    public final boolean isSelfTriggered;

    private ICType(String name) {
        this.name = name;
        this.isSelfTriggered = false;
    }

    private ICType(String name, boolean torchUpdate) {
        this.name = name;
        this.isSelfTriggered = torchUpdate;
    }

    public void think(ServerInterface s, WorldInterface w, Vector v, Vector c, SignInterface t, IC i) {
    }

    public void think(ServerInterface s, WorldInterface w, Vector v, SignInterface t, IC i) {
    }

    public static ICType forName(String name) {
        if (name.equals("ziso"))
            return SISO;
        else if (name.equals("siso"))
            return SISO;
        else if (name.equals("si3o"))
            return SI3O;
        else if (name.equals("3iso"))
            return _3ISO;
        else if (name.equals("3i3o"))
            return _3I3O;
        else if (name.equals("vivo"))
            return VIVO;
        else
            return null;
    }
}