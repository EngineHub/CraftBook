package com.sk89q.craftbook.ic;

public class MC4000 extends _3I3OFamilyIC {
	public String getTitle() {
		return "FULL ADDER";
	}

	public void think(ChipState chip) {
		boolean A = chip.getIn(1).is();
		boolean B = chip.getIn(2).is();
		boolean C = chip.getIn(3).is();
		
		boolean S = A^B^C;
		boolean Ca = (A&B)|((A^B)&C);
		
		chip.getOut(1).set(S);
		chip.getOut(2).set(Ca);
		chip.getOut(3).set(Ca);
	}
}
