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

import com.sk89q.craftbook.*;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener {
    private PropertiesFile properties;

    public BookReader books;
    public Cauldron cauldron;
    public Elevator elevator;
    public GateSwitch gateSwitch;
    public LightSwitch lightSwitch;
    public boolean dropBookshelves = true;
    public float dropAppleChance = 0;

    /**
     * Called when a block is hit with the primary attack.
     * 
     * @param player
     * @param block
     * @return
     */
    @Override
    public boolean onBlockDestroy(Player player, Block block) {
        // Random apple drops
        if (dropAppleChance > 0 && block.getType() == BlockType.LEAVES) {
            if (block.getStatus() == 3) {
                if (Math.random() <= dropAppleChance) {
                    player.giveItemDrop(ItemType.APPLE, 1);
                }
            }

        // Bookshelf drops
        } else if (dropBookshelves && block.getType() == BlockType.BOOKCASE) {
            if (block.getStatus() == 3) {
                player.giveItemDrop(BlockType.BOOKCASE, 1);
            }
        }

        return false;
    }

    /**
     * Called when a block is being attempted to be placed.
     * 
     * @param player
     * @param blockPlaced
     * @param blockClicked
     * @param itemInHand
     * @return
     */
    @Override
    public boolean onBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) {

        // Discriminate against attempts that would actually place blocks
        boolean isPlacingBlock = blockPlaced.getType() != -1
                && blockPlaced.getType() <= 256;
        
        // Book reading
        if (books != null
                && blockClicked.getType() == BlockType.BOOKCASE
                && !isPlacingBlock) {
            books.readBook(player);
            return true;

        // Sign buttons
        } else if (blockClicked.getType() == BlockType.WALL_SIGN) {
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            Vector pt = new Vector(x, y, z);

            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;
                String line2 = sign.getText(1);

                // Gate
                if (gateSwitch != null && line2.equalsIgnoreCase("[Gate]")) {
                    // A gate may toggle or not
                    if (gateSwitch.toggleGates(pt)) {
                        player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
                    }
                
                // Light switch
                } else if (lightSwitch != null && line2.equalsIgnoreCase("[|]")) {
                    return lightSwitch.toggleLights(pt);

                // Elevator
                } else if (elevator != null
                        && (line2.equalsIgnoreCase("[Lift Up]")
                        || line2.equalsIgnoreCase("[Lift Down]"))) {
                    
                    // Go up or down?
                    boolean up = line2.equalsIgnoreCase("[Lift Up]");
                    elevator.performLift(player, pt, up);
                    return true;
                }
            }

        // Cauldron
        } else if (cauldron != null
                && (blockPlaced.getType() == -1 || blockPlaced.getType() >= 256)
                && blockPlaced.getType() != BlockType.STONE) {
            
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            cauldron.preCauldron(new Vector(x, y, z), player);
        }

        return false;
    }
}