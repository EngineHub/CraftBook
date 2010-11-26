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

package com.sk89q.craftbook.ic;

import java.util.Map;
import com.sk89q.craftbook.*;

/**
 * Wireless transmitter.
 *
 * @author sk89q
 */
public class MC1110 extends SISOFamilyIC {
	public static Map<String,Boolean> airwaves =
		new HistoryHashMap<String,Boolean>(100);

	public void think(ChipState chip) {
		chip.title("TRANSMITTER");
		
		String id = chip.text().getLine3();
		
		if (!id.isEmpty()) {
			airwaves.put(id, chip.in(1).is());
			chip.out(1).set(true);
		} else {
			chip.out(1).set(false);
		}
	}
}
