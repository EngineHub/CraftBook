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

package com.sk89q.craftbook;

/**
 *
 * @author sk89q
 */
public class SignText {
    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private boolean changed;
    private boolean noUpdate = false;

    /**
     * Construct the object.
     * 
     * @param l1
     * @param l2
     * @param l3
     * @param l4
     */
    public SignText(String l1, String l2, String l3, String l4) {
        line1 = l1;
        line2 = l2;
        line3 = l3;
        line4 = l4;
    }

    /**
     * @return the line1
     */
    public String getLine1() {
        return line1;
    }

    /**
     * @param line1 the line1 to set
     */
    public void setLine1(String line1) {
        if(this.line1.equals(line1)) return;
        this.line1 = line1;
        changed = true;
    }

    /**
     * @return the line2
     */
    public String getLine2() {
        return line2;
    }

    /**
     * @param line2 the line2 to set
     */
    public void setLine2(String line2) {
        if(this.line2.equals(line2)) return;
        this.line2 = line2;
        changed = true;
    }

    /**
     * @return the line3
     */
    public String getLine3() {
        return line3;
    }

    /**
     * @param line3 the line3 to set
     */
    public void setLine3(String line3) {
        if(this.line3.equals(line3)) return;
        this.line3 = line3;
        changed = true;
    }

    /**
     * @return the line4
     */
    public String getLine4() {
        return line4;
    }

    /**
     * @param line4 the line4 to set
     */
    public void setLine4(String line4) {
        if(this.line4.equals(line4)) return;
        this.line4 = line4;
        changed = true;
    }

    /**
     * Suppress sending the client a update.
     */
    public void supressUpdate() {
        noUpdate = true;
    }
    
    /**
     * @return the changed
     */
    public boolean isChanged() {
        return changed;
    }
    /**
     * Check if an update is requested.
     * 
     * @return
     */
    public boolean shouldUpdate() {
        return !noUpdate;
    }
}
