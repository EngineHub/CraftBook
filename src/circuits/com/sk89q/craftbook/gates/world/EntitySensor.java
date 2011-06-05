//$Id$
/*
 * Copyright (C) 2011 purpleposeidon@gmail.com
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

package com.sk89q.craftbook.gates.world;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;

/* 
 * Checks an area for entities.
 * 
 * USAGE EXAMPLES:
 * 
 * 	[MC1246]
 *  p:c
 *  @5
 *  Looks for players in a radius of 5
 *  
 *  
 *  [MC1246]
 *  i:357
 *  @5
 *  Will detect a dropped cookie
 *  
 *  c:p|i:357 checks for players or for cookies
 *  v:*Cart checks for all types of minecarts
 *  p:notch checks for Notch
 * 
 * 
 * SYNTAX:
 * 
 * 	Line 2: Pattern of entities to match. Syntax in BNF:
 *  	line2 := pattern ('|' pattern)*
 *  	pattern := letter ':' (matchString | number | classCharacter)
 *  	matchString := '*' string_to_start_with | string_to_be
 * 
 * 	Line 3: Search area. Syntax:
 *  	@radius - search in that radius (Actually, the box containing the sphere of that radius) 
 *  	x1,y1,z1;x2,y2,z2 - Specifies a cuboid to search in. They extend out from opposite sides of the sign,
 *  						so if both are positive, then the sign will be included. If one has all negative components,
 *  						the sign will not be included. 
 *  	x1,y1,z1Xx2,y2,z2 - Like above, except using hexadecimal.
 * 
 * 	The following options for letter are available:
 *  	p: player name
 *  	i: Item ID#
 *  	a: Alive entities (Mobs, Animals, Players)
 *  	v: Vehicle
 *  	c: Entity class: Either 'm' for Monster, 'a' for Animal, 'p' for Player, 'v' for Vehicle
 *  
 *  @author purpleposeidon
 */


public class EntitySensor extends AbstractIC {

    protected boolean risingEdge;
    protected final static int maxArea = 16*16*4, maxDistance = 16*3;

    public EntitySensor(Server server, Sign sign, boolean risingEdge) {
        super(server, sign);
        this.risingEdge = risingEdge;
    }

    @Override
    public String getTitle() {
        return "Thing Sensor";
    }

    @Override
    public String getSignTitle() {
        return "THING SENSOR";
    }
    
    @Override
    public void trigger(ChipState chip) {
        if (risingEdge && chip.getInput(0)
                || (!risingEdge && !chip.getInput(0))) {
            try {
                boolean r = doMatch(getSign(), false);
                chip.set(1, r);
            } catch (ICVerificationException e) {
                chip.set(1, false);
            }
        }
    }

    public static boolean doMatch(Sign sign, boolean verify_only) throws ICVerificationException {
        List<String> matchPlayer = new LinkedList<String>();
        List<Integer> matchItem = new LinkedList<Integer>(); //honestly, it'd be just as easy to have it match name instead of ID.
        List<String> matchAlive = new LinkedList<String>();
        List<String> matchVehicle = new LinkedList<String>();
        List<Character> matchClass = new LinkedList<Character>();
        
        //parse
        //Line 2: Get search info
        if (sign.getLine(2).length() == 0) {
            throw new ICVerificationException("Example usage: Line 1: p:*Notch|v:Cart  Line 2: @10  or  2,2,2;4,4,4");
        }
        String pattern_syntax_msg = "Pattern syntax is searchType:matchItem. Use ?:? to get searchTypes.";
        for (String pattern : sign.getLine(2).split("\\|")) {
            pattern = pattern.toLowerCase();
            if (pattern.length() < 3) {
                throw new ICVerificationException("Line 2: Empty or incomplete search pattern. " + pattern_syntax_msg);
            }
            String searchType = pattern.substring(0, 3);
            String item = pattern.substring(2);
            if (searchType.charAt(1) != ':') throw new ICVerificationException("Line 2: " + pattern_syntax_msg);
            switch (searchType.charAt(0)) {
            case 'p':
                matchPlayer.add(item);
                break;
            case 'i':
                try {
                    matchItem.add(Integer.parseInt(item));
                }
                catch (NumberFormatException n) {
                    throw new ICVerificationException("Line 2: Item ID required after i:");
                }
                break;
            case 'a':
                matchAlive.add(item);
                break;
            case 'v':
                matchVehicle.add(item);
                break;
            case 'c':
                for (char c: item.toCharArray()) {
                    String i = ""+c;
                    if (!"mcpv".contains(i)) {
                        throw new ICVerificationException("Line 2: Unknown class " + i + ". Valid are m(onster), c(reature), p(layer), v(ehicle)");
                    }
                    matchClass.add(item.charAt(0));
                }
                break;
            default:
                throw new ICVerificationException("Line 2: Unknown searchType "+searchType.charAt(0)+":, use  p(layer) i(tem) a(live) v(ehicle) c(lass). Eg, p:Notch");
            }
        }
        CuboidRegion cube;
        String line3 = sign.getLine(3);
        String line3_clueless = "Line 3: Search area. '@5' for a 10x10x10, or '-3,-3,-3;2,2,5' to look in that cuboid";
        if (line3.length() == 0) throw new ICVerificationException(line3_clueless);
        Vector here = BukkitUtil.toVector(sign.getBlock());
        if (line3.contains(";") || line3.contains("X")) {
        	int radix;
        	if (line3.contains("X")) radix = 16;
        	else radix = 10;
        	
            String[] vectors = line3.split("[X;]");;
            if (vectors.length != 2) throw new ICVerificationException("Line 3: Need 2 points to define cuboid. 2,2,2;3,3,3");
            Vector p1, p2;
            try {
            	p1 = parseVector(vectors[0], radix);
            	p2 = parseVector(vectors[1], radix);
            }
            catch (NumberFormatException e) {
            	throw new ICVerificationException("Line 3: Invalid number, " + e);
            }
            p1 = here.add(p1);
            p2 = here.subtract(p2);
            cube = new CuboidRegion(p1, p2);
        }
        else if (line3.startsWith("@")) {
            int i;
            try {
                i = Integer.parseInt(line3.substring(1));
            }
            catch (NumberFormatException e) {
                throw new ICVerificationException("Line 3: @ must be followed by a number indicating the radius");
            }
            Vector v = BukkitUtil.toVector(sign.getBlock());
            cube = new CuboidRegion(v.add(i, i, i), v.add(-i, -i, -i));
        }
        else {
            throw new ICVerificationException(line3_clueless);
        }
        
        int area = cube.getWidth()*cube.getHeight();
        if (area > maxArea) {
        	throw new ICVerificationException("Search area of "+area+" square meters is larger than the maximum permitted, " + maxArea);
        }
        
        Vector cubeCenter = cube.getPos1().add(cube.getPos2()).divide(2);
        
        if (cubeCenter.distanceSq(here) > maxDistance*maxDistance) {
        	throw new ICVerificationException("The center of the search area is too far away from the sign");
        }
        
        if (verify_only) return false;

        //now search. Finally.
        World world = sign.getWorld();
        
        for (Vector2D chunkVector : cube.getChunks()) {
            for (Entity entity : world.getChunkAt(chunkVector.getBlockX(), chunkVector.getBlockZ()).getEntities()) {
                Vector entityVector = BukkitUtil.toVector(entity.getLocation());
                if (!cube.contains(entityVector)) continue;
                
                if (entity instanceof Player) {
                    String name = ((Player)entity).getName();
                    if (checkMatch(name, matchPlayer)) return true;
                }
                if (entity instanceof Item) {
                    int typeId = ((Item)entity).getItemStack().getTypeId();
                    if (matchItem.contains(typeId)) return true;
                }
                if (entity instanceof LivingEntity) {
                    String name = ((LivingEntity)entity).toString();
                    if (checkMatch(name, matchAlive)) return true;
                }
                if (entity instanceof Vehicle) {
                    String name = ((Vehicle)entity).toString();
                    if (checkMatch(name, matchVehicle)) return true;
                }
                for (Character c : matchClass) {
                    if (c == 'm' && entity instanceof Monster) return true;
                    if (c == 'c' && entity instanceof Creature) return true;
                    if (c == 'p' && entity instanceof Player) return true;
                    if (c == 'v' && entity instanceof Vehicle) return true;
                }
            }
        }
        return false; //found nothing
    }
    
    static Vector parseVector(String src, int radix) throws NumberFormatException {
        Vector result = new Vector();
        String[] comps = src.split(",");
        if (comps.length != 3) throw new NumberFormatException("Must have 3 comma-seperated components in vector");
        result = result.setX(Integer.parseInt(comps[0], radix));
        result = result.setY(Integer.parseInt(comps[1], radix));
        result = result.setZ(Integer.parseInt(comps[2], radix));
        return result;
    }
    
    static boolean checkMatch(String name, List<String> options) {
        if (options.size() == 0) return false;
        name = name.toLowerCase();
        System.out.println("Checking " + name + " against " + options);
        if (name.startsWith("craft")) name = name.substring("craft".length());
        System.out.println("-->" + name);
        System.out.println(name.trim() == options.get(0).trim());
        for (String o : options) {
            if (o.charAt(0) == '*') {
                if (name.contains(o.substring(1))) return true;
            }
            else {
                if (name == o) return true;
            }
        }
        return false;
    }

    public static class Factory extends AbstractICFactory {

        protected boolean risingEdge;

        public Factory(Server server, boolean risingEdge) {
            super(server);
            this.risingEdge = risingEdge;
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {
            doMatch(sign, true);
        }
        
        @Override
        public IC create(Sign sign) {
            return new EntitySensor(getServer(), sign, risingEdge); 
        }
    }

}

