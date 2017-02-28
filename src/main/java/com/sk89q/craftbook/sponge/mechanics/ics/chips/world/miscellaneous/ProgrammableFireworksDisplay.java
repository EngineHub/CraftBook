/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.world.miscellaneous;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.craftbook.core.util.RegexUtil;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.InvalidICException;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import com.sk89q.craftbook.sponge.util.SignUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.FireworkShapes;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class ProgrammableFireworksDisplay extends IC {

    private static File fireworksFolder;

    static {
        fireworksFolder = new File(CraftBookPlugin.inst().getWorkingDirectory(), "fireworks");
        if (!fireworksFolder.exists()) {
            fireworksFolder.mkdirs();
        }
    }

    private FireworkShowHandler handler;
    private boolean stopOnLow;

    public ProgrammableFireworksDisplay(ICFactory<ProgrammableFireworksDisplay> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void create(Player player, List<Text> lines) throws InvalidICException {
        super.create(player, lines);

        String showName = SignUtil.getTextRaw(lines.get(2));

        if (showName.trim().isEmpty()
                || (!new File(fireworksFolder, showName.trim() + ".txt").exists()
                && !new File(fireworksFolder, showName.trim() + ".fwk").exists())) {
            throw new InvalidICException("Unknown firework show on line 3!");
        }
    }

    @Override
    public void load() {
        super.load();

        String showName = getLine(2);
        handler = new FireworkShowHandler(showName);

        String[] bits = RegexUtil.COMMA_PATTERN.split(getLine(3));
        if(bits.length > 0)
            stopOnLow = Boolean.getBoolean(bits[0]);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this) && !handler.isShowRunning())
            handler.startShow();
        else if (handler.isShowRunning() && stopOnLow)
            handler.stopShow();
    }

    public class FireworkShowHandler {
        ShowInterpreter show;
        String showName;
        int position;
        List<String> lines = Lists.newArrayList();
        Task task;
        boolean fyrestone = false;

        FireworkShowHandler(String showName) {
            this.showName = showName;
            try {
                readShow();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void readShow() throws IOException {
            lines.clear();
            File firework = new File(fireworksFolder, showName + ".txt");
            if(!firework.exists()) {
                fyrestone = true;
                firework = new File(fireworksFolder, showName + ".fwk");
                if (!firework.exists()) {
                    CraftBookPlugin.inst().getLogger().warn("Firework File Not Found: " + firework.getName());
                    return;
                }
            } else {
                fyrestone = false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(firework), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.trim().isEmpty())
                    continue;
                lines.add(line);
            }

            br.close();
        }

        boolean isShowRunning() {
            return show != null && show.isRunning();
        }

        void stopShow() {
            show.setRunning(false);
        }

        void startShow() {
            position = 0;
            if (task != null)
                task.cancel();
            ShowInterpreter show;
            if(!fyrestone)
                show = new BasicShowInterpreter();
            else
                show = new FyrestoneInterpreter();
            task = Sponge.getScheduler().createTaskBuilder().execute(show).submit(CraftBookPlugin.spongeInst().getContainer());
        }

        private class FyrestoneInterpreter implements ShowInterpreter {
            Map<String, List<FireworkEffect>> effects = Maps.newHashMap();
            String currentBuilding = null;
            Location<World> location = getBlock();
            float duration = 0.5f;
            boolean preciseDuration = false;
            FireworkEffect.Builder builder = FireworkEffect.builder();
            boolean isRunning = false;

            FyrestoneInterpreter() {
                isRunning = true;
            }

            FyrestoneInterpreter(Map<String, List<FireworkEffect>> effects, String currentBuilding, Location<World> location, float duration, FireworkEffect.Builder builder) {
                this.effects = effects;
                this.currentBuilding = currentBuilding;
                this.location = location;
                this.duration = duration;
                this.builder = builder;
            }

            @Override
            public void run () {
                while (isRunning && position < lines.size()) {
                    String line = lines.get(position);
                    position++;
                    if (line.trim().startsWith("#") || line.trim().isEmpty())
                        continue;

                    if (line.startsWith("set.")) {
                        if (currentBuilding == null) {
                            continue;
                        }

                        line = line.replace("set.", "").trim();
                        String[] args = RegexUtil.SPACE_PATTERN.split(line);

                        if (args[0].equalsIgnoreCase("shape")) {
                            FireworkShape shape = Sponge.getRegistry().getType(FireworkShape.class, args[1]).orElse(null);
                            if (shape != null) {
                                builder.shape(shape);
                            } else {
                                CraftBookPlugin.inst().getLogger().warn("Unknown shape in firework file " + showName + ". Shape: " + args[1]);
                            }
                        } else if (args[0].equalsIgnoreCase("color")) {
                            String[] rgb = RegexUtil.COMMA_PATTERN.split(args[1]);
                            builder.color(Color.ofRgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
                        } else if (args[0].equalsIgnoreCase("fade")) {
                            String[] rgb = RegexUtil.COMMA_PATTERN.split(args[1]);
                            builder.fade(Color.ofRgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));
                        } else if (args[0].equalsIgnoreCase("flicker") || args[0].equalsIgnoreCase("twinkle")) {
                            builder.flicker(true);
                        } else if (args[0].equalsIgnoreCase("trail")) {
                            builder.trail(true);
                        }
                    } else if (line.startsWith("location ")) {
                        double x,y,z;
                        String[] args = RegexUtil.COMMA_PATTERN.split(line.replace("location ", ""));
                        x = Double.parseDouble(args[0]);
                        y = Double.parseDouble(args[1]);
                        z = Double.parseDouble(args[2]);

                        location = getBlock().add(x, y, z);
                    } else if (line.startsWith("duration ")) {
                        String[] bits = RegexUtil.SPACE_PATTERN.split(line.replace("duration ", ""));
                        duration = Float.parseFloat(bits[0]);
                        preciseDuration = bits.length > 1 && bits[1].equalsIgnoreCase("precise");
                    } else if (line.startsWith("wait ")) {
                        FyrestoneInterpreter nshow = new FyrestoneInterpreter(effects,currentBuilding,location,duration,builder);
                        nshow.isRunning = isRunning;
                        task = Sponge.getScheduler().createTaskBuilder().delayTicks(Long.parseLong(line.replace("wait ", ""))).execute(nshow).submit(CraftBookPlugin.spongeInst().getContainer());
                        show = nshow;
                        return;
                    } else if (line.startsWith("sound ")) {
                        String[] bits = RegexUtil.SPACE_PATTERN.split(line.replace("sound ", ""));
                        SoundType sound = Sponge.getRegistry().getType(SoundType.class, bits[0]).orElse(null);
                        if (sound != null) {
                            Location<World> sloc = getBlock().copy();
                            float volume = 1.0f, pitch = 1.0f;
                            if (bits.length > 1) {
                                double x, y, z;
                                String[] args = RegexUtil.COMMA_PATTERN.split(bits[1]);
                                x = Double.parseDouble(args[0]);
                                y = Double.parseDouble(args[1]);
                                z = Double.parseDouble(args[2]);

                                sloc = sloc.add(x, y, z);
                                if (bits.length > 2) {
                                    volume = Float.parseFloat(bits[2]);
                                    if (bits.length > 3)
                                        pitch = Float.parseFloat(bits[3]);
                                }
                            }
                            sloc.getExtent().playSound(sound, sloc.getPosition(), volume, pitch);
                        } else {
                            CraftBookPlugin.inst().getLogger().warn("Unknown sound in firework file " + showName + ". Sound: " + bits[0]);
                        }
                    } else if (line.startsWith("start ")) {
                        currentBuilding = line.replace("start ", "");
                        builder = FireworkEffect.builder();
                    } else if (line.startsWith("build")) {
                        if (currentBuilding == null)
                            continue;
                        if (effects.containsKey(currentBuilding)) {

                            List<FireworkEffect> effectList = effects.get(currentBuilding);
                            effectList.add(builder.build());
                            effects.put(currentBuilding, effectList);
                        } else {
                            List<FireworkEffect> effectList = Lists.newArrayList();
                            effectList.add(builder.build());
                            effects.put(currentBuilding, effectList);
                        }
                        currentBuilding = null;
                    } else if (line.startsWith("launch ")) {
                        if(effects.containsKey(line.replace("launch ", ""))) {
                            final Firework firework = (Firework) location.getExtent().createEntity(EntityTypes.FIREWORK, location.getPosition());
                            List<FireworkEffect> fireworkEffects = Lists.newArrayList();
                            for(FireworkEffect effect : effects.get(line.replace("launch ", "")))
                                fireworkEffects.add(effect);
                            firework.offer(Keys.FIREWORK_EFFECTS, fireworkEffects);
                            if (preciseDuration) {
                                firework.offer(Keys.FUSE_DURATION, (int) duration * 2);
                            } else {
                                firework.offer(Keys.FIREWORK_FLIGHT_MODIFIER, (int) duration * 2);
                            }
                            location.getExtent().spawnEntity(firework, Cause.source(CraftBookPlugin.spongeInst().getContainer()).build());
                        }
                    }
                }

                isRunning = false;
            }

            @Override
            public void setRunning (boolean isRunning) {
                this.isRunning = isRunning;
            }

            @Override
            public boolean isRunning () {
                return isRunning;
            }
        }

        private class BasicShowInterpreter implements ShowInterpreter {

            @Override
            public void run() {

                isRunning = true;

                while (isRunning && position < lines.size()) {

                    String line = lines.get(position);
                    position++;
                    if (line.startsWith("#"))
                        continue;

                    String[] bits = RegexUtil.COLON_PATTERN.split(line, 2);
                    if (bits.length < 2)
                        continue;

                    if (bits[0].equalsIgnoreCase("wait")) {
                        BasicShowInterpreter show = new BasicShowInterpreter();
                        task = Sponge.getScheduler().createTaskBuilder().delayTicks(Long.parseLong(line.replace("wait ", ""))).execute(show).submit(CraftBookPlugin.spongeInst().getContainer());
                        return;
                    } else if (bits[0].equalsIgnoreCase("launch")) {
                        String errorLocation = "Unknown";
                        try {
                            String[] data = RegexUtil.SEMICOLON_PATTERN.split(bits[1]);

                            //Offset data (0)
                            errorLocation = "Offset";
                            String[] offset = RegexUtil.COMMA_PATTERN.split(data[0]);
                            Location location = getBlock().add(Double.parseDouble(offset[0]), Double.parseDouble(offset[1]), Double.parseDouble(offset[2]));

                            //Duration data (1)
                            errorLocation = "Duration";
                            double duration = Double.parseDouble(data[1]); //1 duration = 1 second.

                            //Shape data (2)
                            errorLocation = "Shape";
                            FireworkShape shape = Sponge.getRegistry().getType(FireworkShape.class, data[2]).orElse(null);
                            if (shape == null) {
                                CraftBookPlugin.inst().getLogger().warn("Unknown shape in firework file " + showName + ". Shape: " + data[2]);
                                shape = FireworkShapes.BALL;
                            }

                            //Colour Data (3)
                            errorLocation = "Colour";
                            String[] rgb = RegexUtil.COMMA_PATTERN.split(data[3]);
                            Color colour = Color.ofRgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

                            //Colour Data (4)
                            errorLocation = "Fade";
                            rgb = RegexUtil.COMMA_PATTERN.split(data[4]);
                            Color fade = Color.ofRgb(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

                            boolean flicker = false;
                            boolean trail = false;

                            if (data.length > 5) {
                                //Shape data (5)
                                errorLocation = "Effects";
                                if (data[5].equalsIgnoreCase("twinkle"))
                                    flicker = true;
                                else if (data[5].equalsIgnoreCase("trail"))
                                    trail = true;
                            }

                            errorLocation = "Creation";

                            final Firework firework = (Firework) location.getExtent().createEntity(EntityTypes.FIREWORK, location.getPosition());

                            List<FireworkEffect> fireworkEffects = Lists.newArrayList();
                            fireworkEffects.add(FireworkEffect.builder().shape(shape).color(colour).fade(fade).flicker(flicker).trail(trail).build());
                            firework.offer(Keys.FIREWORK_EFFECTS, fireworkEffects);

                            firework.offer(Keys.FIREWORK_FLIGHT_MODIFIER, (int) duration * 2);
                            location.getExtent().spawnEntity(firework, Cause.source(CraftBookPlugin.spongeInst().getContainer()).build());
                        } catch (Exception e) {
                            CraftBookPlugin.inst().getLogger().warn("Error occured while doing: " + errorLocation + ". Whilst reading line " + position + " of the firework file " + showName + '!');
                            e.printStackTrace();
                        }
                    }
                }

                isRunning = false;
            }

            @Override
            public void setRunning (boolean isRunning) {
                this.isRunning = isRunning;
            }

            @Override
            public boolean isRunning () {
                return isRunning;
            }

            private boolean isRunning = false;
        }
    }

    public interface ShowInterpreter extends Runnable {

        void setRunning(boolean isRunning);

        boolean isRunning();
    }

    public static class Factory implements ICFactory<ProgrammableFireworksDisplay> {

        @Override
        public ProgrammableFireworksDisplay createInstance(Location<World> location) {
            return new ProgrammableFireworksDisplay(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "Firework Script Name",
                    "Stop on Low (true/false)"
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Runs script on high"
                    },
                    new String[] {
                            "None"
                    }
            };
        }
    }
}
