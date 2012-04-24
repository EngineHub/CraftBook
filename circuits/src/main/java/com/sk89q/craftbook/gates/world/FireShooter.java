package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.util.SignUtil;

public class FireShooter extends AbstractIC{

	protected boolean risingEdge;

    public FireShooter(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }
	
	@Override
	public String getTitle() {
		return "Fire Shooter";
	}

	@Override
	public String getSignTitle() {
		 return "FIRE SHOOTER";
	}

	@Override
	public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0) || (!risingEdge && !chip.getInput(0))) {
        	shootFire(1);
        }
	}
	
	public void shootFire(int n) {
    	float speed = 0.6F;
    	float spread = 12;
    	float vert = 0;
    	try {
    		String[] velocity = getSign().getLine(2).trim().split(":");
    		speed = Float.parseFloat(velocity[0]);
    		spread = Float.parseFloat(velocity[1]);
    		vert = Float.parseFloat(getSign().getLine(3).trim());
    	} catch (Exception e) {  }
    	
    	if(speed > 2.0) speed = 2F;
    	if(speed < 0.2) speed = 0.2F;
    	if(spread > 50) spread = 50;
    	if(spread < 0) spread = 0;
    	if(vert > 1) vert = 1;
    	if(vert < -1) vert = -1;
    	
    	
        Block signBlock = getSign().getBlock();
    	BlockFace face = SignUtil.getBack(signBlock);
        Block targetDir =  signBlock.getRelative(face).getRelative(face);
        
        float x = targetDir.getX() - signBlock.getX();
        float z = targetDir.getZ() - signBlock.getZ();
    	Vector velocity = new Vector(x, vert, z);
    	Location shootLoc = new Location(getSign().getWorld(), targetDir.getX() + 0.5, targetDir.getY() + 0.5, targetDir.getZ() + 0.5);

    	for(int i = 0; i < n; i++)
    	    getSign().getWorld().spawn(shootLoc, org.bukkit.entity.SmallFireball.class).setVelocity(velocity);
    }

    public static class Factory extends AbstractICFactory implements
            RestrictedIC {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public IC create(Sign sign) {
            return new FireShooter(getServer(), sign, risingEdge);
        }
    }

}
