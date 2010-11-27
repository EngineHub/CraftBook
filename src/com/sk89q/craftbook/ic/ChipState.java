package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.*;

public class ChipState {
	private Signal[] in;
	private Signal[] out;
	private boolean[] mem;
	private Vector pos;
	private SignText text;
	
	public ChipState (Vector pos, Signal[] in, Signal[] out, SignText text) {
		this.pos = pos;
		this.in = in;
		this.out = out;
		this.text = text;
		
		mem = new boolean[out.length];
		int i = 0;
		for (Signal bit : out) {
			mem[i++] = bit.is();
		}
	}
	
	public void title(String title) {
		String curTitle = text.getLine1();
		if (!curTitle.equals(title)) {
			text.setLine1(title);
		}
	}

	public Signal in(int n) {
		if (n > in.length) return null;
		return in[n-1];
	}

	public Signal out(int n) {
		if (n > out.length) return null;
		return out[n-1];
	}
	
	public boolean last(int n) {
		if (n > mem.length) return false;
		return mem[n-1];
	}
	
	public boolean modified() {
		int i = 0;
		
		for (Signal bit : out) {
			if (bit.is() != mem[i++])
				return true;
		}
		
		return false;
	}

	public Vector pos() {
		return pos;
	}
	
	public SignText text() {
		return text;
	}
}
