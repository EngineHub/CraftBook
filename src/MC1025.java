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



import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.SISOFamilyIC;
import com.sk89q.craftbook.ic.Signal;

/**
 * 1-bit number based on modulus of server time.
 *
 * @author Shaun (sturmeh)
 */
public class MC1025 extends SISOFamilyIC {

	public boolean think(Vector pos, boolean input1, boolean oldState) {
		Signal in = new Signal(input1);
		Signal out = new Signal(oldState);
		if (in.state())
			out.set(isServerTimeOdd());
		return out.state();
	}
	
	private boolean isServerTimeOdd() {
		long time = etc.getServer().getRelativeTime() % 2;
		if (time < 0)
			time += 2;
		return (time == 1);
	}
}
