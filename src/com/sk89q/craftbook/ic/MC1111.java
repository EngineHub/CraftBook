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

/**
 * Positive edge-triggered wireless receiver.
 *
 * @author sk89q
 */
public class MC1111 extends SISOFamilyIC {
	public void think(ChipState chip) {
		chip.title("RECIEVER");
		if (chip.in(1).is()) {
			String id = chip.text().getLine3();
			if (!id.isEmpty()) {
				Boolean out = MC1110.airwaves.get(id);
				if (out == null) {
					chip.out(1).set(false);
				} else {
					chip.out(1).set(out);
				}
			} else {
				chip.out(1).set(false);
			}
		}
	}
}
