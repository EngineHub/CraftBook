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
 * 
 * @author Lymia
 */
public enum ICType {
	/**
	 * Zero input, single output
	 */
	ZISO("ZISO", true) {
		void think(Vector pt, SignText signText, Sign sign, IC zisoIC) {
			Vector outputVec = Util.getWallSignBack(pt, 2);
			Vector backVec = Util.getWallSignBack(pt, 1);

			Signal[] in = new Signal[0];

			Signal[] out = new Signal[1];
			out[0] = new Signal(Redstone.getOutput(outputVec));

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			zisoIC.think(chip);

			if (chip.isModified()) {
			    Redstone.setOutput(outputVec, out[0].is());
			}
		}
	},
	/**
	 * Single input, single output
	 */
	SISO("SISO") {
		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, IC sisoIC, TickDelayer r) {
			Vector outputVec = Util.getWallSignBack(pt, 2);
			Vector in0 = Util.getWallSignBack(pt, -1);
			Vector backVec = Util.getWallSignBack(pt, 1);

			Signal[] in = new Signal[1];
			in[0] = new Signal(Redstone.isHighBinary(in0, true),
					changedRedstoneInput.equals(in0));

			Signal[] out = new Signal[1];
			out[0] = new Signal(Redstone.getOutput(outputVec));

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			sisoIC.think(chip);

			if (chip.isModified()) {
				Redstone.setOutput(outputVec, chip.getOut(1).is());
			}

			if (chip.hasErrored()) {
				signText.setLine2(Colors.Gold + signText.getLine2());
				signText.allowUpdate();
			}
		}
	},
	/**
	 * Single input, triple output
	 */
	SI3O("SI3O") {
		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, IC si3oIC, TickDelayer r) {
			Vector backVec = Util.getWallSignBack(pt, 1);
			Vector backShift = backVec.subtract(pt);
			Vector in0 = Util.getWallSignBack(pt, -1);
			Vector output1Vec = Util.getWallSignBack(pt, 2);
			Vector output2Vec = Util.getWallSignSide(pt, 1).add(backShift);
			Vector output3Vec = Util.getWallSignSide(pt, -1).add(backShift);

			Signal[] in = new Signal[1];
			in[0] = new Signal(Redstone.isHighBinary(in0, true),
					changedRedstoneInput.equals(in0));

			Signal[] out = new Signal[3];
			out[0] = new Signal(Redstone.getOutput(output1Vec));
			out[1] = new Signal(Redstone.getOutput(output2Vec));
			out[2] = new Signal(Redstone.getOutput(output3Vec));

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			// The most important part...
			si3oIC.think(chip);

			if (chip.isModified()) {
				Redstone.setOutput(output1Vec, chip.getOut(1).is());
				Redstone.setOutput(output2Vec, chip.getOut(2).is());
				Redstone.setOutput(output3Vec, chip.getOut(3).is());
			}

			if (chip.hasErrored()) {
				signText.setLine2(Colors.Gold + signText.getLine2());
				signText.allowUpdate();
			}
		}
	},
	/**
	 * Triple input, single output
	 */
	_3ISO("3ISO") {
		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, IC _3isoIC, TickDelayer r) {
			Vector backVec = Util.getWallSignBack(pt, 1);
			Vector outputVec = Util.getWallSignBack(pt, 2);
			Vector input1Vec = Util.getWallSignBack(pt, -1);
			Vector input2Vec = Util.getWallSignSide(pt, 1);
			Vector input3Vec = Util.getWallSignSide(pt, -1);

			Signal[] in = new Signal[3];
			in[0] = new Signal(Redstone.isHighBinary(input1Vec, true),
					changedRedstoneInput.equals(input1Vec));
			in[1] = new Signal(Redstone.isHighBinary(input2Vec, true),
					changedRedstoneInput.equals(input2Vec));
			in[2] = new Signal(Redstone.isHighBinary(input3Vec, true),
					changedRedstoneInput.equals(input3Vec));

			Signal[] out = new Signal[1];
			out[0] = new Signal(Redstone.getOutput(outputVec));

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			// The most important part...
			_3isoIC.think(chip);

			if (chip.isModified()) {
				Redstone.setOutput(outputVec, chip.getOut(1).is());
			}

			if (chip.hasErrored()) {
				signText.setLine2(Colors.Gold + signText.getLine2());
				signText.allowUpdate();
			}
		}
	},
	/**
	 * Triple input, triple output
	 */
	_3I3O("3I3O") {
		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, IC _3i3oIC, TickDelayer r) {
			Vector backVec = Util.getWallSignBack(pt, 1);
			Vector backShift = Util.getWallSignBack(pt, 2).subtract(pt);

			Vector out0 = Util.getWallSignBack(pt, 3);
			Vector out1 = Util.getWallSignSide(pt, 1).add(backShift);
			Vector out2 = Util.getWallSignSide(pt, -1).add(backShift);

			Vector in0 = Util.getWallSignBack(pt, -1);
			Vector in1 = Util.getWallSignSide(pt, 1);
			Vector in2 = Util.getWallSignSide(pt, -1);

			Signal[] in = new Signal[3];
			in[0] = new Signal(Redstone.isHighBinary(in0, true),
					changedRedstoneInput.equals(in0));
			in[1] = new Signal(Redstone.isHighBinary(in1, true),
					changedRedstoneInput.equals(in1));
			in[2] = new Signal(Redstone.isHighBinary(in2, true),
					changedRedstoneInput.equals(in2));

			Signal[] out = new Signal[3];
			out[0] = new Signal(Redstone.getOutput(out0));
			out[1] = new Signal(Redstone.getOutput(out1));
			out[2] = new Signal(Redstone.getOutput(out2));

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			// The most important part...
			_3i3oIC.think(chip);

			if (chip.isModified()) {
				Redstone.setOutput(out0, chip.getOut(1).is());
				Redstone.setOutput(out1, chip.getOut(2).is());
				Redstone.setOutput(out2, chip.getOut(3).is());
			}

			if (chip.hasErrored()) {
				signText.setLine2(Colors.Gold + signText.getLine2());
				signText.allowUpdate();
			}
		}
	},
	/**
	 * Variable input, variable output
	 */
	VIVO("VIVO") {
		void think(Vector pt, Vector changedRedstoneInput, SignText signText,
				Sign sign, IC vivoIC, TickDelayer r) {
			Vector backVec = Util.getWallSignBack(pt, 1);
			Vector backShift = backVec.subtract(pt);

			Vector out0 = Util.getWallSignBack(pt, 2);
			Vector out1 = Util.getWallSignSide(pt, 1).add(backShift);
			Vector out2 = Util.getWallSignSide(pt, -1).add(backShift);

			Vector in0 = Util.getWallSignBack(pt, -1);
			Vector in1 = Util.getWallSignSide(pt, 1);
			Vector in2 = Util.getWallSignSide(pt, -1);

			boolean hasOut1 = CraftBook.getBlockID(out1) == BlockType.LEVER;
			boolean hasOut2 = CraftBook.getBlockID(out2) == BlockType.LEVER;

			Signal[] in = new Signal[3];
			Signal[] out = new Signal[3];

			out[0] = new Signal(Redstone.getOutput(out0));
			in[0] = new Signal(Redstone.isHighBinary(in0, true),
					changedRedstoneInput.equals(in0));

			if (hasOut1) {
				out[1] = new Signal(Redstone.getOutput(out1));
				in[1] = new Signal(false);
			} else {
				out[1] = new Signal(false);
				in[1] = new Signal(Redstone.isHighBinary(in1, true),
						changedRedstoneInput.equals(in1));
			}

			if (hasOut2) {
				out[2] = new Signal(Redstone.getOutput(out2));
				in[2] = new Signal(false);
			} else {
				out[2] = new Signal(false);
				in[2] = new Signal(Redstone.isHighBinary(in2, true),
						changedRedstoneInput.equals(in2));
			}

			ChipState chip = new ChipState(pt, backVec, in, out, signText, etc.getServer().getTime());

			// The most important part...
			vivoIC.think(chip);

			if (chip.isModified()) {
				Redstone.setOutput(out0, chip.getOut(1).is());
				if (hasOut1)
					Redstone.setOutput(out1, chip.getOut(2).is());
				if (hasOut2)
					Redstone.setOutput(out2, chip.getOut(3).is());
			}

			if (chip.hasErrored()) {
				signText.setLine2(Colors.Gold + signText.getLine2());
				signText.allowUpdate();
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

	void think(Vector v, Vector c, SignText t, Sign s, IC i, TickDelayer r) {
	}

	void think(Vector v, SignText t, Sign s, IC i) {
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