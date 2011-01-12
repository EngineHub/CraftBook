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

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Versatile Set Block
 *
 * USAGE:
 * L3: [X|Y|Z] [+|-] dist : val                                         eg.  "Y+2:41"
 * L4: [F, H]                       F=Force,  H=Hold (remove on LO)     eg.  "FH"
 *
 * @author robhol
 */
public class MC1207 extends BaseIC {

    private static final int MAXDELTA = 5;

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "SET BLOCK EX";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {
        return true;
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign) {

        String l3 = sign.getLine3();
        String force = sign.getLine4();

        if (l3.isEmpty())
            return "No input found.";


        String l3check = checkLine3(l3);
        if (l3check != null)
            return l3check;


        return null;
    }

    //Line 3:  [X|Y|Z] [+|-] dist : val   eg.  Y+2:41
    String checkLine3(String line)
    {

        if (line.length() < 5)
            return "Too short";

        char axis = line.toUpperCase().charAt(0);
        switch (axis) //Commence 1337 switch validation!
        {
            case 'X': case 'Y': case 'Z': break;
            default: return "Invalid axis";
        }

        char op = line.charAt(1);
        if (op != '+' && op != '-')
            return "Invalid operator";

        char eq = line.charAt(3);
        if (eq != ':' && eq != '=')
            return "Syntax error, need =";

        char cdelta = line.charAt(2); // distance. Not designed for long-range, therefore only one digit.

        //Validate number. Note shorthand for string conversion.
        Integer delta = Util.tryParse(""+cdelta);
        if (delta == null || delta > MAXDELTA)
            return "Invalid distance. Max is " + MAXDELTA;

        //Validate item
        int item = getItem(line.substring(4));
        if (item < 0)
            return "Invalid block";

        return null;
    }


    /**
     * Get an item from its name or ID.
     *
     * @param id
     * @return
     */
    private int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {

        String line = chip.getText().getLine3();
        String flags = chip.getText().getLine4().toUpperCase();
        boolean isForced = flags.contains("F");
        boolean holdMode = flags.contains("H");

        //Rising flank only, unless hold flag is on.
        if (!chip.getIn(1).is() && !holdMode)
            return;

        //Collect our data
        char axis = line.toUpperCase().charAt(0);

        char op = line.charAt(1);

        char cdelta = line.charAt(2);
        int delta = Integer.parseInt(""+cdelta);

        int newType = getItem(line.substring(4));

        //Showtime
        if (op == '-')
            delta = -delta;

        int x = chip.getBlockPosition().getBlockX();
        int y = chip.getBlockPosition().getBlockY();
        int z = chip.getBlockPosition().getBlockZ();

        switch (axis)
        {
            case 'X': x += delta; break;
            case 'Y': y += delta; break;
            case 'Z': z += delta; break;
        }

        //Action! Hold mode:
        if (!chip.getIn(1).is())
        {
            etc.getServer().setBlockAt(0, x,y,z);
            return;
        }

        //Normal action. Skip if target is not empty, unless forced.
        if (!isForced && etc.getServer().getBlockAt(x,y,z).blockType != Block.Type.Air)
            return;

        etc.getServer().setBlockAt(newType, x,y,z);

        chip.getOut(1).set(false);
    }
}
