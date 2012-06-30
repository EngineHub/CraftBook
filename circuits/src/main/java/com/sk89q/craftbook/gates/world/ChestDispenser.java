package com.sk89q.craftbook.gates.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.SignUtil;

public class ChestDispenser extends AbstractIC{
    public ChestDispenser(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
	return "Chest Dispenser";
    }

    @Override
    public String getSignTitle() {
	return "CHEST DISPENSER";
    }

    @Override
    public void trigger(ChipState chip) {
        if (chip.getInput(0)) {
            chip.setOutput(0, dispense());
        }
    }

    /**
     * Returns true if the sign has water at the specified location.
     * 
     * @return
     */
    protected boolean dispense() {

	Block b = SignUtil.getBackBlock(getSign().getBlock());

	int x = b.getX();
	int y = b.getY()+1;
	int z = b.getZ();
	Block bl = getSign().getBlock().getWorld().getBlockAt(x, y, z);
	int amount = Integer.parseInt(getSign().getLine(2));
	if (bl.getType() == Material.CHEST) 
	{
	    Chest c = ((Chest) bl.getState());
	    ItemStack[] is = c.getInventory().getContents();
	    for(int i = 0; i < is.length; i++)
	    {
		if(ItemUtil.isStackValid(is[i]))
		{
		    int curA = is[i].getAmount();
		    ItemStack stack = new ItemStack(is[i].getTypeId(),is[i].getAmount(),is[i].getData().getData());
		    stack.setAmount(1);
		    getSign().getWorld().dropItemNaturally(new Location(getSign().getWorld(), getSign().getX(), getSign().getY(), getSign().getZ()),stack);
		    is[i].setAmount(curA-amount);
		    break;
		}
	    }
	    c.getInventory().setContents(is);
	}
	return false;
    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new ChestDispenser(getServer(), sign);
        }
    }
}
