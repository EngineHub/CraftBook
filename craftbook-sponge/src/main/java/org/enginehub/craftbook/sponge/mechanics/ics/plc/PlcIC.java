/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.craftbook.sponge.mechanics.ics.plc;

import com.google.common.collect.Lists;
import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.InvalidICException;
import org.enginehub.craftbook.sponge.mechanics.ics.plc.lang.WithLineInfo;
import org.enginehub.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

public class PlcIC<Lang extends PlcLanguage> extends IC {
    private Lang lang;
    public PlcFactory.PlcStateData state;
    private WithLineInfo[] code;

    PlcIC(PlcFactory<Lang> factory, Location<World> location, Lang lang) {
        super(factory, location);
        this.lang = lang;
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        state = new PlcFactory.PlcStateData();

        state.state = lang.initState();
        state.languageName = lang.getName();
    }

    public void load() {
        String codeString;
        try {
            codeString = getCode();
        } catch (CodeNotFoundException e) {
            error("Error Retrieving Code", e.getMessage());
            return;
        }
        try {
            if (codeString != null) {
                code = lang.compile(codeString);
            }
        } catch (InvalidICException e) {
            error("Inconsistent compile check!", e.getMessage());
        }
        state.codeString = codeString;
    }

    private String getBookCode(Location<World> chestBlock) throws CodeNotFoundException {
        Chest c = chestBlock.getTileEntity().map(te -> (Chest) te).get();
        Inventory i = c.getDoubleChestInventory().orElse(c.getInventory());
        ItemStack book = null;
        for (Slot slot : i.query(
                QueryOperationTypes.ITEM_TYPE.of(ItemTypes.WRITABLE_BOOK),
                QueryOperationTypes.ITEM_TYPE.of(ItemTypes.WRITTEN_BOOK)).<Slot>slots()) {
            ItemStack item = slot.peek().orElse(null);
            if (item != null && item.getQuantity() > 0 ) {
                if (book != null) throw new CodeNotFoundException("More than one written book found in chest!!");
                book = item;
            }
        }
        if (book == null) throw new CodeNotFoundException("No written books found in chest.");

        StringBuilder code = new StringBuilder();
        for (Text page : book.get(Keys.BOOK_PAGES).orElse(Lists.newArrayList())) {
            code.append(SignUtil.getTextRaw(page)).append('\n');
        }
        return code.toString();
    }

    private String getCode() throws CodeNotFoundException {
        Location<World> above = getBlock().getRelative(Direction.UP);
        if (above.getBlockType() == BlockTypes.CHEST) return getBookCode(above);
        Location<World> below = getBlock().getRelative(Direction.DOWN);
        if (below.getBlockType() == BlockTypes.CHEST) return getBookCode(below);

        int x = getBlock().getBlockX();
        int z = getBlock().getBlockZ();

        for (int y = 0; y < 256; y++) {
            if (y != getBlock().getBlockY()) {
                Location<World> testBlock = getBlock().getExtent().getLocation(x, y, z);
                if (SignUtil.isSign(testBlock)) {
                    Sign sign = testBlock.getTileEntity().map(te -> (Sign) te).get();
                    if ("[Code Block]".equalsIgnoreCase(SignUtil.getTextRaw(sign, 1))) {
                        y--;
                        Location<World> b = testBlock.getExtent().getLocation(x, y, z);
                        StringBuilder code = new StringBuilder();
                        while (SignUtil.isSign(b)) {
                            sign = b.getTileEntity().map(te -> (Sign) te).get();
                            for (int li = 0; li < 4 && y != getBlock().getBlockY(); li++) {
                                code.append(SignUtil.getTextRaw(sign, li)).append('\n');
                            }
                            b = getBlock().getExtent().getLocation(x, --y, z);
                        }
                        return code.toString();
                    }
                }
            }
        }
        throw new CodeNotFoundException("No code source found.");
    }

    public void error(String shortMessage, String detailedMessage) {
        setLine(2, Text.of(TextColors.RED, "!Error!"));
        setLine(3, Text.of(shortMessage));

        state.error = true;
        state.errorCode = detailedMessage;
    }

    @Override
    public void trigger() {
        try {
            lang.execute(this, state.state, code);
        } catch (PlcException e) {
            error(e.getMessage(), e.detailedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            error(e.getClass().getName(), "Internal error encountered: " + e.getClass().getName());
        }
    }

    /*@Override
    public void onRightClick(Player p) {
        if (CraftBookPlugin.inst().hasPermission(p, "craftbook.plc.debug")) {
            p.sendMessage(ChatColor.GREEN + "Programmable Logic Controller debug information");
            BlockWorldVector l = sign.getBlockVector();
            p.sendMessage(ChatColor.RED + "Status:" + ChatColor.RESET + " " + (error ? "Error Encountered" : "OK"));
            p.sendMessage(ChatColor.RED + "Location:" + ChatColor.RESET + " (" + l.getBlockX() + ", " +
                    "" + l.getBlockY() + ", " + l.getBlockZ() + ")");
            p.sendMessage(ChatColor.RED + "Language:" + ChatColor.RESET + " " + lang.getName());
            p.sendMessage(ChatColor.RED + "Full Storage Name:" + ChatColor.RESET + " " + getFileName());
            if (error) {
                p.sendMessage(errorString);
            } else {
                p.sendMessage(lang.dumpState(state.state));
            }
        } else {
            p.sendMessage(ChatColor.RED + "You do not have the necessary permissions to do that.");
        }
    }*/
}