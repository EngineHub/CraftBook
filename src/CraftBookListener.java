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
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.io.*;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CraftBookListener extends PluginListener {
    private PropertiesFile properties;

    public boolean useBookshelf = true;
    public boolean useLightSwitch = true;
    public boolean useGate = true;
    public boolean useElevators = true;
    public float dropAppleChance = 0;

    private final static int BOOKSHELF = 47;
    private final static int LEVER = 69;
    private final static int TORCH = 50;
    private final static int REDSTONE_TORCH_OFF = 75;
    private final static int REDSTONE_TORCH_ON = 76;
    private final static int WOOD = 5;
    private final static int FENCE = 85;
    private final static int WATER = 8;
    private final static int STATIONARY_WATER = 9;
    private final static int BUTTON = 77;
    private final static int WALL_SIGN = 68;
    private final static int LEAVES = 18;
    private final static int APPLE = 260;

    private Random rand = new Random();
    private HistoryHashMap<BlockVector,Long> recentLightToggles
            = new HistoryHashMap<BlockVector,Long>(20);

    private static int getBlockID(int x, int y, int z) {
        return etc.getServer().getBlockIdAt(x, y, z);
    }

    private static void setBlockID(int x, int y, int z, int type) {
        etc.getServer().setBlockAt(type, x, y, z);
    }

    @Override
    public boolean onBlockDestroy(Player player, Block block) {
        if (dropAppleChance > 0 && block.getType() == LEAVES) {
            if (block.getStatus() == 3) {
                if (Math.random() <= dropAppleChance) {
                    player.giveItemDrop(APPLE, 1);
                }
            }
        }

        return false;
    }

    @Override
    public boolean onBlockCreate(Player player, Block blockPlaced,
            Block blockClicked, int itemInHand) {
        // Book reading
        if (useBookshelf && blockClicked.getType() == BOOKSHELF
                && (blockPlaced.getType() == -1 || blockPlaced.getType() >= 256)) {
            readBook(player);
            return true;

        // Sign buttons
        } else if (blockClicked.getType() == WALL_SIGN) {
            int x = blockClicked.getX();
            int y = blockClicked.getY();
            int z = blockClicked.getZ();

            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;

                // Gate
                if (useGate && sign.getText(1).equalsIgnoreCase("[Gate]")) {
                    if (toggleClosestGate(blockClicked)) {
                        player.sendMessage(Colors.Gold + "*screeetch* Gate moved!");
                    } else {
                        player.sendMessage(Colors.Rose + "No nearby gate to toggle.");
                    }
                // Light switch
                } else if (useLightSwitch && sign.getText(1).equalsIgnoreCase("[|]")) {
                    return toggleLights(blockClicked);
                // Elevator
                } else if (useElevators
                        && (sign.getText(1).equalsIgnoreCase("[Lift Up]")
                        || sign.getText(1).equalsIgnoreCase("[Lift Down]"))) {
                    performLift(player, blockClicked,
                            sign.getText(1).equalsIgnoreCase("[Lift Up]"));
                    return true;
                }
            }
        }

        return false;
    }

    private void performLift(Player player, Block blockClicked, boolean up) {
        int x = blockClicked.getX();
        int y = blockClicked.getY();
        int z = blockClicked.getZ();

        if (up) {
            for (int y1 = y + 1; y1 <= 127; y1++) {
                if (checkLift(player, x, y1, z, up)) {
                    return;
                }
            }
        } else {
            for (int y1 = y - 1; y1 >= 1; y1--) {
                if (checkLift(player, x, y1, z, up)) {
                    return;
                }
            }
        }
    }

    private boolean checkLift(Player player, int x, int y1, int z, boolean up) {
        if (etc.getServer().getBlockIdAt(x, y1, z) == WALL_SIGN) {
            ComplexBlock cBlock = etc.getServer().getComplexBlock(x, y1, z);

            if (cBlock instanceof Sign) {
                Sign sign = (Sign)cBlock;

                // Found our stop?
                if (sign.getText(1).equalsIgnoreCase("[Lift Up]")
                        || sign.getText(1).equalsIgnoreCase("[Lift Down]")
                        || sign.getText(1).equalsIgnoreCase("[Lift]")) {
                    int plyX = (int)Math.floor(player.getX());
                    int plyY = (int)Math.floor(player.getY());
                    int plyZ = (int)Math.floor(player.getZ());

                    int y2;
                    for (y2 = y1; y2 >= y1 - 5; y2--) {
                        int id = etc.getServer().getBlockIdAt(plyX, y2, plyZ);
                        if (id != 0 && id != TORCH && id != REDSTONE_TORCH_OFF
                                && id != REDSTONE_TORCH_ON && id != WALL_SIGN) {
                            break;
                        }
                    }

                    player.teleportTo(player.getX(), y2 + 1, player.getZ(),
                            player.getRotation(), player.getPitch());
                    String title = sign.getText(0);

                    if (title.length() != 0) {
                        player.sendMessage(Colors.Gold + "Floor: " + title);
                    } else {
                        player.sendMessage(Colors.Gold + "You went "
                                + (up ? "up" : "down") + " a floor.");
                    }

                    return true;
                }
            }
        }

        return false;
    }

    private void readBook(Player player) {
        try {
            String text = getBookLine();

            if (text != null) {
                player.sendMessage(Colors.Gold + "You pick out a book...");
                player.sendMessage(text);
            } else {
                player.sendMessage(Colors.Rose + "Failed to fetch a book line from craftbook-books.txt.");
            }
        } catch (IOException e) {
            player.sendMessage(Colors.Rose + "Failed to read craftbook-books.txt.");
        }
    }

    private String getBookLine() throws IOException {
        RandomAccessFile file = new RandomAccessFile(
                new File("craftbook-books.txt"), "r");
        
        byte[] data = new byte[500];

        for (int tries = 0; tries < 3; tries++) {
            file.seek(rand.nextInt((int) (file.length())));
            file.read(data);

            StringBuilder buffer = new StringBuilder();
            boolean found = false;
            byte last = 0;

            for (int i = 0; i < data.length; i++) {
                if (found) {
                    if (data[i] == 10 || data[i] == 13) {
                        if (last != 10 && last != 13) {
                            return buffer.toString();
                        }
                    } else {
                        buffer.appendCodePoint(data[i]);
                    }
                } else if (data[i] == 10 || data[i] == 13) { // Line feeds
                    found = true;
                }

                last = data[i];
            }
        }

        return null;
    }

    private boolean toggleLights(Block blockClicked) {
        int ox = blockClicked.getX();
        int oy = blockClicked.getY();
        int oz = blockClicked.getZ();
        int aboveID = getBlockID(ox, oy + 1, oz);

        if (aboveID == TORCH || aboveID == REDSTONE_TORCH_OFF
                || aboveID == REDSTONE_TORCH_ON) {
            boolean on = aboveID != TORCH;

            // Prevent spam
            BlockVector bvec = new BlockVector(ox, oy, oz);
            Long lastUse = recentLightToggles.remove(bvec);
            long now = System.currentTimeMillis();
            if (lastUse != null && now - lastUse < 500) {
                recentLightToggles.put(bvec, lastUse);
                return true;
            }
            recentLightToggles.put(bvec, now);

            int changed = 0;

            for (int x = -10 + ox; x <= 10 + ox; x++) {
                for (int y = -10 + oy; y <= 10 + oy; y++) {
                    for (int z = -5 + oz; z <= 5 + oz; z++) {
                        int id = getBlockID(x, y, z);

                        if (id == TORCH || id == REDSTONE_TORCH_OFF
                                || id == REDSTONE_TORCH_ON) {
                            if (changed >= 20) {
                                return true;
                            }

                            if (on) {
                                setBlockID(x, y, z, TORCH);
                            } else {
                                setBlockID(x, y, z, REDSTONE_TORCH_OFF);
                            }

                            changed++;
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean toggleClosestGate(Block blockClicked) {
        int x = blockClicked.getX();
        int y = blockClicked.getY();
        int z = blockClicked.getZ();

        for (int x1 = x - 3; x1 <= x + 3; x1++) {
            for (int y1 = y - 3; y1 <= y + 6; y1++) {
                for (int z1 = z - 3; z1 <= z + 3; z1++) {
                    if (toggleGate(x1, y1, z1)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean toggleGate(int x, int y, int z) {
        if (getBlockID(x, y, z) != FENCE) {
            return false;
        }

        // Find the top most fence
        for (int y1 = y + 1; y1 <= y + 12; y1++) {
            if (getBlockID(x, y1, z) == FENCE) {
                y = y1;
            } else {
                break;
            }
        }

        // Check to see if this is a gate or a fence
        if (getBlockID(x, y + 1, z) == 0) {
            return false;
        }

        boolean close = getBlockID(x, y - 1, z) != FENCE;
        
        performGateToggle(new BlockVector(x, y, z), close,
                new HashSet<BlockVector>());

        return true;
    }

    private void performGateToggle(BlockVector pt, boolean close,
            Set<BlockVector> visited) {
        if (visited.size() >= 8) {
            return;
        }

        if (visited.contains(pt)) {
            return;
        }

        int x = pt.getBlockX();
        int y = pt.getBlockY();
        int z = pt.getBlockZ();

        if (getBlockID(x, y, z) != FENCE) {
            return;
        }

        visited.add(pt);

        performGateToggle(new BlockVector(x - 1, y, z), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z), close, visited);
        performGateToggle(new BlockVector(x, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x, y, z + 1), close, visited);
        performGateToggle(new BlockVector(x - 1, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x - 1, y, z + 1), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z - 1), close, visited);
        performGateToggle(new BlockVector(x + 1, y, z + 1), close, visited);
        
        int minY = Math.max(0, y - 12);
        for (int y1 = y - 1; y1 >= minY; y1--) {
            int cur = getBlockID(x, y1, z);
            
            if (cur != WATER
                    && cur != STATIONARY_WATER
                    && cur != FENCE
                    && cur != 0) {
                break;
            }
            
            setBlockID(x, y1, z, close ? FENCE : 0);
        }
    }
}