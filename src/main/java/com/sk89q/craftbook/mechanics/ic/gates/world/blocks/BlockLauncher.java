package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.RegexUtil;

public class BlockLauncher extends AbstractIC {

    Vector velocity;
    ItemStack block;

    public BlockLauncher(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Block Launcher";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK LAUNCH";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            launch();
        }
    }

    @Override
    public void load() {

        block = ItemUtil.makeItemValid(ItemSyntax.getItem(getLine(2)));

        if(getLine(2).isEmpty() || block == null)
            block = new ItemStack(Material.SAND,1);

        if(getLine(3).isEmpty())
            velocity = new Vector(0,0.5,0);
        else {
            String[] split2 = RegexUtil.COLON_PATTERN.split(getSign().getLine(3));
            velocity = new Vector(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));
        }
    }

    public void launch() {

        Block above = getBackBlock().getRelative(0, 1, 0);
        int timeout = 12;
        while (above.getType() != Material.AIR || timeout < 0 || above.getLocation().getY() >= 255) {
            above = above.getRelative(0, 1, 0);
            timeout--;
        }
        if (velocity.getY() < 0) {
            above = getBackBlock().getRelative(0, -1, 0);
            timeout = 12;
            while (above.getType() != Material.AIR || timeout < 0 || above.getLocation().getY() <= 1) {
                above = above.getRelative(0, -1, 0);
                timeout--;
            }
        }
        double y = above.getY() - 0.99D;

        if(!new Location(CraftBookBukkitUtil.toSign(getSign()).getWorld(), above.getX() + 0.5D, y, above.getZ() + 0.5D).getChunk().isLoaded())
            return;

        FallingBlock block = CraftBookBukkitUtil
                .toSign(getSign()).getWorld().spawnFallingBlock(new Location(CraftBookBukkitUtil.toSign(getSign()).getWorld(), above.getX() + 0.5D, y, above.getZ() + 0.5D), this.block.getType(), this.block.getData().getData());
        block.setVelocity(velocity);
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockLauncher(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {
            if(!sign.getLine(3).isEmpty()) {
                try {
                    String[] split2 = RegexUtil.COLON_PATTERN.split(sign.getLine(3));
                    new Vector(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));
                } catch (Exception ignored) {
                    throw new ICVerificationException("Velocity must be in x:y:z format!");
                }
            }
        }

        @Override
        public String getShortDescription() {

            return "Launches set block with set velocity.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"id{:data}", "+ovelocity x:y:z"};
        }
    }
}