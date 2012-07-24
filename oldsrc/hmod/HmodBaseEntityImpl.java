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

import com.sk89q.craftbook.access.BaseEntityInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.Vector;

public class HmodBaseEntityImpl implements BaseEntityInterface {

    private BaseEntity entity;

    protected WorldInterface world;

    public HmodBaseEntityImpl(BaseEntity entity, WorldInterface world) {

        this.entity = entity;
        this.world = world;
    }

    public WorldInterface getWorld() {

        return world;
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    @Override
    public Vector getBlockIn() {

        return Vector.toBlockPoint(entity.getX(), entity.getY(), entity.getZ());
    }

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    @Override
    public Vector getBlockOn() {

        return Vector.toBlockPoint(entity.getX(), entity.getY() - 1, entity.getZ());
    }


    /**
     * Move the player.
     *
     * @param pos
     */
    public void setPosition(Vector pos) {

        setPosition(pos, (float) getPitch(), (float) getYaw());
    }

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    @Override
    public double getPitch() {

        return entity.getPitch();
    }

    /**
     * Get the player's position.
     *
     * @return point
     */
    @Override
    public Vector getPosition() {

        return new Vector(entity.getX(), entity.getY(), entity.getZ());
    }

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    @Override
    public double getYaw() {

        return entity.getRotation();
    }

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {

        Location loc = new Location();
        loc.x = pos.getX();
        loc.y = pos.getY();
        loc.z = pos.getZ();
        loc.rotX = (float) yaw;
        loc.rotY = (float) pitch;
        entity.teleportTo(loc);
    }

    public void setYaw(double yaw) {

        entity.setRotation((float) yaw);
    }

    public void setPitch(double pitch) {

        entity.setPitch((float) pitch);
    }

    public double getX() {

        return entity.getX();
    }

    public double getY() {

        return entity.getY();
    }

    public double getZ() {

        return entity.getZ();
    }

    public void setX(double x) {

        entity.setX(x);
    }

    public void setY(double y) {

        entity.setY(y);
    }

    public void setZ(double z) {

        entity.setZ(z);
    }

    public void remove() {

        etc.getServer().getMCServer().e.e(entity.getEntity());
    }

    public int getEntityId() {

        return entity.getId();
    }

    public double getXSpeed() {

        return entity.getEntity().s;
    }

    public double getYSpeed() {

        return entity.getEntity().t;
    }

    public double getZSpeed() {

        return entity.getEntity().u;
    }

    public void setXSpeed(double s) {

        entity.getEntity().s = s;
    }

    public void setYSpeed(double s) {

        entity.getEntity().t = s;
    }

    public void setZSpeed(double s) {

        entity.getEntity().u = s;
    }
}
