package com.sk89q.craftbook.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.BukkitPlayer;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.SignUtil;
import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class BlockBreaker extends AbstractIC {

    boolean above;

    public BlockBreaker(Server server, ChangedSign block, boolean above, ICFactory factory) {

        super(server, block, factory);
        this.above = above;
    }

    @Override
    public String getTitle() {

        return "Block Breaker";
    }

    @Override
    public String getSignTitle() {

        return "BLOCK BREAK";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, breakBlock());
        }
    }

    Block broken,chest;

    int id;
    byte data;

    @Override
    public void load () {
        Block bl = SignUtil.getBackBlock(BukkitUtil.toSign(getSign()).getBlock());
        if (above) {
            chest = bl.getRelative(0, 1, 0);
            broken = bl.getRelative(0, -1, 0);
        } else {
            chest = bl.getRelative(0, -1, 0);
            broken = bl.getRelative(0, 1, 0);
        }

        try {
            String[] split = ICUtil.COLON_PATTERN.split(getSign().getLine(2));
            id = Integer.parseInt(split[0]);
            data = Byte.parseByte(split[1]);
        } catch (Exception ignored) {}
    }

    public boolean breakBlock() {

        boolean hasChest = false;
        if (chest != null && chest.getTypeId() == BlockID.CHEST) {
            hasChest = true;
        }
        if (broken == null
                || broken.getTypeId() == 0
                || broken.getTypeId() == BlockID.BEDROCK
                || broken.getTypeId() == BlockID.PISTON_MOVING_PIECE) return false;

        if (id > 0 && id != broken.getTypeId()) return false;

        if (data > 0 && data != broken.getData()) return false;

        for (ItemStack blockStack  : broken.getDrops()) {

            if (hasChest) {
                Chest c = (Chest) chest.getState();
                HashMap<Integer, ItemStack> overflow = c.getInventory().addItem(blockStack);
                if (overflow.isEmpty()) {
                    continue;
                } else {
                    for (Map.Entry<Integer, ItemStack> bit : overflow.entrySet()) {
                        dropItem(bit.getValue());
                    }
                    continue;
                }
            }

            dropItem(blockStack);
        }
        broken.setTypeId(0);

        return true;
    }

    public void dropItem(ItemStack item) {

        BukkitUtil.toSign(getSign()).getWorld().dropItem(BlockUtil.getBlockCentre(BukkitUtil.toSign(getSign()).getBlock()), item);
    }

    public static class Factory extends AbstractICFactory {

        boolean above;

        public Factory(Server server, boolean above) {

            super(server);
            this.above = above;
        }

        @Override
        public IC create(ChangedSign sign) {

            return new BlockBreaker(getServer(), sign, above, this);
        }

        @Override
        public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {

            Block tb = SignUtil.getBackBlock(BukkitUtil.toSign(sign).getBlock());
            if (above) tb = tb.getRelative(0, -1, 0);
            else tb = tb.getRelative(0, 1, 0);

            BlockBreakEvent e = new BlockBreakEvent(tb, ((BukkitPlayer) player).getPlayer());
            getServer().getPluginManager().callEvent(e);
            if (e.isCancelled()) throw new ICVerificationException("You can't build that here.");
        }

        @Override
        public String getDescription() {

            return "Breaks blocks above/below block sign is on.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "Optional block ID",
                    null
            };
            return lines;
        }
    }
}