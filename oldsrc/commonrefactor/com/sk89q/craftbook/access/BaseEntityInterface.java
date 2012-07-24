/*    
Craftbook 
Copyright (C) 2010 Lymia <lymiahugs@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.craftbook.access;

import com.sk89q.craftbook.util.Vector;

public interface BaseEntityInterface {

    public Vector getPosition();

    public double getYaw();

    public double getPitch();

    public void setYaw(double yaw);

    public void setPitch(double pitch);

    public double getX();

    public double getY();

    public double getZ();

    public void setX(double x);

    public void setY(double y);

    public void setZ(double z);

    public void setPosition(Vector pos);

    public void setPosition(Vector pos, float pitch, float yaw);

    public WorldInterface getWorld();

    public Vector getBlockIn();

    public Vector getBlockOn();

    public void remove();

    public int getEntityId();

    double getXSpeed();

    double getYSpeed();

    double getZSpeed();

    void setXSpeed(double s);

    void setYSpeed(double s);

    void setZSpeed(double s);
}
