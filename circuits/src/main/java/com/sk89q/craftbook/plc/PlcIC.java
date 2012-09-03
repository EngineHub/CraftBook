// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.plc;

import com.sk89q.craftbook.ic.*;
import org.bukkit.*;
import org.bukkit.block.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PlcIC<StateT extends PlcState, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements IC {
    private static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    private Lang lang;
    private StateT state;
    private CodeT code;

    private Sign sign;

    PlcIC(Sign s, Lang l) throws ICVerificationException {
        sign = s;
        String codeString = getCode();
        if(codeString == null)
            throw new ICVerificationException("Code block not found.");
        lang.compile(codeString);
    }
    public PlcIC(Server sv, Sign s, Lang l) {
        lang = l;
        sign = s;
        String codeString = getCode();
        try {
            code = lang.compile(codeString);
        } catch(ICVerificationException e) {
            throw new RuntimeException("inconsistent compile check!", e);
        }
        state = lang.initState();
    }

    private String getCode() {
        Location l = sign.getLocation();
        World w = l.getWorld();

        int x = l.getBlockX();
        int z = l.getBlockZ();

        for(int y=0;y<w.getMaxHeight();y++) {
            if(y!=l.getBlockY())
                if(w.getBlockAt(x,y,z) instanceof Sign) {
                    Sign s = (Sign) w.getBlockAt(x,y,z);
                    if(s.getLine(1).equalsIgnoreCase("[Code Block]")) {
                        y--;
                        Block b = w.getBlockAt(x, y, z);
                        String code = "";
                        while(b instanceof Sign) {
                            s = (Sign) b;
                            for(int li=0;li<4;li++)
                                code += s.getLine(li)+"\n";
                            b = w.getBlockAt(x, --y, z);
                        }
                        return code;
                    }
                }
        }
        return null;
    }

    @Override
    public String getTitle() {
        return lang.getName()+" PLC";
    }

    @Override
    public String getSignTitle() {
        return "PLC:"+lang.getName().toUpperCase();
    }

    @Override
    public void trigger(ChipState chip) {
        try {
            lang.execute(chip, state, code);
        } catch(PlcException e) {
            sign.setLine(1, ChatColor.RED+sign.getLine(1));
            sign.setLine(2, "error encountered");
            sign.setLine(3, e.getMessage());
            sign.update();
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Internal error while executing PLC", e);

            sign.setLine(1, ChatColor.RED+sign.getLine(1));
            sign.setLine(2, "error encountered");
            sign.setLine(3, e.getClass().getName());
            sign.update();
        }
    }

    public IC selfTriggered() {
        final IC self = this;
        return new SelfTriggeredIC() {
            @Override
            public String getTitle() {
                return self.getTitle();
            }

            @Override
            public String getSignTitle() {
                return self.getSignTitle();
            }

            @Override
            public void trigger(ChipState chip) {}

            @Override
            public void think(ChipState chip) {
                self.trigger(chip);
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public void unload() {}
        };
    }

    @Override
    public void unload() {}
}
