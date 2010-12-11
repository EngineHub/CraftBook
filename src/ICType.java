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
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.Signal;

/**
 * IC types.
 */
public enum ICType {
    /**
     * Single input, single output
     */
    SISO {
        void think(Vector pt, Vector changedRedstoneInput, SignText signText, Sign sign, IC sisoIC) {
            Vector outputVec = CraftBookListener.getWallSignBack(pt, 2);
            Vector in0 = CraftBookListener.getWallSignBack(pt, -1);
            Vector backVec = CraftBookListener.getWallSignBack(pt, 1);

            Signal[] in = new Signal[1];
            in[0] = new Signal(CraftBookListener.isRedstoneHighBinary(in0, true),
                    changedRedstoneInput.equals(in0));
            
            Signal[] out = new Signal[1];
            out[0] = new Signal(CraftBookListener.getRedstoneOutput(outputVec));
            
            ChipState chip = new ChipState(pt, backVec, in, out, signText);
            
            sisoIC.think(chip);
            
            if (chip.isModified()) {
                CraftBookListener.setRedstoneOutput(outputVec, chip.getOut(1).is());
            }
        }
    },
    /**
     * Single input, triple output
     */
    SI3O {
        void think(Vector pt, Vector changedRedstoneInput, SignText signText, Sign sign, IC si3oIC) {
            Vector backVec = CraftBookListener.getWallSignBack(pt, 1);
            Vector backShift = backVec.subtract(pt);
            Vector in0 = CraftBookListener.getWallSignBack(pt, -1);
            Vector output1Vec = CraftBookListener.getWallSignBack(pt, 2);
            Vector output2Vec = CraftBookListener.getWallSignSide(pt, 1).add(backShift);
            Vector output3Vec = CraftBookListener.getWallSignSide(pt, -1).add(backShift);

            Signal[] in = new Signal[1];
            in[0] = new Signal(CraftBookListener.isRedstoneHighBinary(in0, true),
                    changedRedstoneInput.equals(in0));

            Signal[] out = new Signal[3];
            out[0] = new Signal(CraftBookListener.getRedstoneOutput(output1Vec));
            out[1] = new Signal(CraftBookListener.getRedstoneOutput(output2Vec));
            out[2] = new Signal(CraftBookListener.getRedstoneOutput(output3Vec));

            ChipState chip = new ChipState(pt, backVec, in, out, signText);

            // The most important part...
            si3oIC.think(chip);

            if (chip.isModified()) {
                CraftBookListener.setRedstoneOutput(output1Vec, chip.getOut(1).is());
                CraftBookListener.setRedstoneOutput(output2Vec, chip.getOut(2).is());
                CraftBookListener.setRedstoneOutput(output3Vec, chip.getOut(3).is());
            }
        }
    },
    /**
     * Triple input, single output
     */
    _3ISO {
        void think(Vector pt, Vector changedRedstoneInput, SignText signText, Sign sign, IC _3isoIC) {
            Vector backVec = CraftBookListener.getWallSignBack(pt, 1);
            Vector outputVec = CraftBookListener.getWallSignBack(pt, 2);
            Vector input1Vec = CraftBookListener.getWallSignBack(pt, -1);
            Vector input2Vec = CraftBookListener.getWallSignSide(pt, 1);
            Vector input3Vec = CraftBookListener.getWallSignSide(pt, -1);

            Signal[] in = new Signal[3];
            in[0] = new Signal(CraftBookListener.isRedstoneHighBinary(input1Vec, true),
                    changedRedstoneInput.equals(input1Vec));
            in[1] = new Signal(CraftBookListener.isRedstoneHighBinary(input2Vec, true),
                    changedRedstoneInput.equals(input2Vec));
            in[2] = new Signal(CraftBookListener.isRedstoneHighBinary(input3Vec, true),
                    changedRedstoneInput.equals(input3Vec));

            Signal[] out = new Signal[1];
            out[0] = new Signal(CraftBookListener.getRedstoneOutput(outputVec));

            ChipState chip = new ChipState(pt, backVec, in, out, signText);

            // The most important part...
            _3isoIC.think(chip);

            if (chip.isModified()) {
                CraftBookListener.setRedstoneOutput(outputVec, chip.getOut(1).is());
            }
        }
    },
    /**
     * Triple input, triple output
     */
    _3I3O {
        void think(Vector pt, Vector changedRedstoneInput, SignText signText, Sign sign, IC _3i3oIC) {
            Vector backVec = CraftBookListener.getWallSignBack(pt, 1);
            Vector backShift = CraftBookListener.getWallSignBack(pt, 2).subtract(pt);
            
            Vector out0 = CraftBookListener.getWallSignBack(pt, 3);
            Vector out1 = CraftBookListener.getWallSignSide(pt, 1).add(backShift);
            Vector out2 = CraftBookListener.getWallSignSide(pt, -1).add(backShift);
            
            Vector in0 = CraftBookListener.getWallSignBack(pt, -1);
            Vector in1 = CraftBookListener.getWallSignSide(pt, 1);
            Vector in2 = CraftBookListener.getWallSignSide(pt, -1);

            Signal[] in = new Signal[3];
            in[0] = new Signal(CraftBookListener.isRedstoneHighBinary(in0, true),
                    changedRedstoneInput.equals(in0));
            in[1] = new Signal(CraftBookListener.isRedstoneHighBinary(in1, true),
                    changedRedstoneInput.equals(in1));
            in[2] = new Signal(CraftBookListener.isRedstoneHighBinary(in2, true),
                    changedRedstoneInput.equals(in2));

            Signal[] out = new Signal[3];
            out[0] = new Signal(CraftBookListener.getRedstoneOutput(out0));
            out[1] = new Signal(CraftBookListener.getRedstoneOutput(out1));
            out[2] = new Signal(CraftBookListener.getRedstoneOutput(out2));

            ChipState chip = new ChipState(pt, backVec, in, out, signText);

            // The most important part...
            _3i3oIC.think(chip);

            if (chip.isModified()) {
                CraftBookListener.setRedstoneOutput(out0, chip.getOut(1).is());
                CraftBookListener.setRedstoneOutput(out1, chip.getOut(2).is());
                CraftBookListener.setRedstoneOutput(out2, chip.getOut(3).is());
            }
        }
    },
    /**
     * Variable input, variable output
     */
    VIVO {
        void think(Vector pt, Vector changedRedstoneInput, SignText signText, Sign sign, IC vivoIC) {
            Vector backVec = CraftBookListener.getWallSignBack(pt, 1);
            Vector backShift = backVec.subtract(pt);
            
            Vector out0 = CraftBookListener.getWallSignBack(pt, 2);
            Vector out1 = CraftBookListener.getWallSignSide(pt, 1).add(backShift);
            Vector out2 = CraftBookListener.getWallSignSide(pt, -1).add(backShift);
            
            Vector in0 = CraftBookListener.getWallSignBack(pt, -1);
            Vector in1 = CraftBookListener.getWallSignSide(pt, 1);
            Vector in2 = CraftBookListener.getWallSignSide(pt, -1);

            boolean hasOut1 = CraftBook.getBlockID(out1) == BlockType.LEVER;
            boolean hasOut2 = CraftBook.getBlockID(out2) == BlockType.LEVER;
            
            Signal[] in = new Signal[3];
            Signal[] out = new Signal[3];
            
            out[0] = new Signal(CraftBookListener.getRedstoneOutput(out0));
            in[0] = new Signal(CraftBookListener.isRedstoneHighBinary(in0, true),
                               changedRedstoneInput.equals(in0));
            
            if(hasOut1) {
                out[1] = new Signal(CraftBookListener.getRedstoneOutput(out1));
                in[1] = new Signal(false);
            }
            else {
                out[1] = new Signal(false);
                in[1] = new Signal(CraftBookListener.isRedstoneHighBinary(in1, true),
                                   changedRedstoneInput.equals(in1));
            }
            
            if(hasOut2) {
                out[2] = new Signal(CraftBookListener.getRedstoneOutput(out2));
                in[2] = new Signal(false);
            }
            else {
                out[2] = new Signal(false);
                in[2] = new Signal(CraftBookListener.isRedstoneHighBinary(in2, true),
                                   changedRedstoneInput.equals(in2));
            }
            
            ChipState chip = new ChipState(pt, backVec, in, out, signText);
            
            // The most important part...
            vivoIC.think(chip);

            if (chip.isModified()) {
                CraftBookListener.setRedstoneOutput(out0, chip.getOut(1).is());
                CraftBookListener.setRedstoneOutput(out1, chip.getOut(2).is());
                CraftBookListener.setRedstoneOutput(out2, chip.getOut(3).is());
            }
        }
    };
    
    abstract void think(Vector v, Vector c, SignText t, Sign s, IC i);
}