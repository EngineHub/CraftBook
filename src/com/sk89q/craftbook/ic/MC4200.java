// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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
 * Dispatcher
 * 
 * @author Lymia
 */
public class MC4200 extends BaseIC {
	public String getTitle() {
		return "DISPATCHER";
	}

	public void think(ChipState chip) {
		boolean value = chip.getIn(1).is();
		boolean targetB = chip.getIn(2).is();
		boolean targetC = chip.getIn(3).is();

		if (targetB) {
			chip.getOut(2).set(value);
		}
		if (targetC) {
			chip.getOut(3).set(value);
		}
	}
}
