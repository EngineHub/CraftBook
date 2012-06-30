package com.sk89q.craftbook.gates.world;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.SignUtil;

public class ChestCollector extends AbstractIC{

    public ChestCollector(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
	return "Chest Collector";
    }

    @Override
    public String getSignTitle() {
	return "CHEST COLLECT";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            chip.setOutput(0, collect());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     * 
     * @return
     */
    protected boolean collect() {

	Block b = SignUtil.getBackBlock(getSign().getBlock());

	int x = b.getX();
	int y = b.getY()+1;
	int z = b.getZ();
	Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
	if (bl.getType() == Material.CHEST) 
	{
	    World w = getSign().getBlock().getWorld();
	    for(Item item : w.getEntitiesByClass(Item.class))
	    {
		int ix = item.getLocation().getBlockX();
		int iy = item.getLocation().getBlockY();
		int iz = item.getLocation().getBlockZ();
		if(ix == getSign().getX() && iy == getSign().getY() && iz == getSign().getZ())
		{
		    if(((Chest) bl.getState()).getInventory().firstEmpty()!=-1)
		    {
			int id = -1;
			int idmeta = -1;
			int exid = -1;
			int exidmeta = -1;
			try
			{
			    if(getSign().getLine(2).contains(":"))
			    {
				id = Integer.parseInt(getSign().getLine(2).split(":")[0]);
				idmeta = Integer.parseInt(getSign().getLine(2).split(":")[1]);
			    }
			    else
				id = Integer.parseInt(getSign().getLine(2));
			}
			catch(Exception e){}
			try
			{
			    if(getSign().getLine(3).contains(":"))
			    {
				exid = Integer.parseInt(getSign().getLine(3).split(":")[0]);
				exidmeta = Integer.parseInt(getSign().getLine(3).split(":")[1]);
			    }
			    else
				exid = Integer.parseInt(getSign().getLine(3));
			}
			catch(Exception e){}
			checks:
			{
			    if(exid!=-1)
			    {
				if(exid==item.getItemStack().getTypeId())
				{
				    if(exidmeta!=-1)
				    {
					if(item.getItemStack().getDurability() == exidmeta)
					    continue;
					else
					    break checks;
				    }
				    else
					continue;
				}
				else
				    break checks;
			    }

			    if(id!=-1)
			    {
				if(id==item.getItemStack().getTypeId())
				{
				    if(idmeta!=-1)
				    {
					if(item.getItemStack().getDurability() == idmeta)
					    break checks;
					else
					    continue;
				    }
				    else
					break checks;
				}
				else
				    continue;
			    }
			}
			((Chest) bl.getState()).getInventory().addItem(item.getItemStack());
			item.remove();
			return true;
		    }
		}
	    }
	}
	return false;
    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new ChestCollector(getServer(), sign);
        }
    }
}
