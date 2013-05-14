package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.AbstractIC;
import com.sk89q.craftbook.circuits.ic.AbstractICFactory;
import com.sk89q.craftbook.circuits.ic.ChipState;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.circuits.ic.RestrictedIC;
import com.sk89q.craftbook.util.RegexUtil;

public class ProgrammableFireworkShow extends AbstractIC {

    public ProgrammableFireworkShow(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Programmable Firework Show";
    }

    @Override
    public String getSignTitle() {

        return "FIREWORKS";
    }

    String show;
    FireworkShowHandler handler;

    @Override
    public void load() {

        show = getLine(2).trim();
        handler = new FireworkShowHandler(show);
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0))
            handler.startShow();
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new ProgrammableFireworkShow(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (sign.getLine(2).trim().isEmpty() || !new File(CircuitCore.inst().getFireworkFolder(),
                    sign.getLine(2).trim() + ".txt").exists() && !new File(CircuitCore.inst().getFireworkFolder(),
                            sign.getLine(2).trim() + ".fwk").exists())
                throw new ICVerificationException("A valid firework show is required on line 3!");
        }

        @Override
        public String getShortDescription() {

            return "Plays a firework show from a file.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Name of firework show", null};
        }
    }

    public class FireworkShowHandler {

        String show;

        int position;

        List<String> lines = new ArrayList<String>();

        BukkitTask task;

        boolean fyrestone = false;

        public FireworkShowHandler(String show) {

            this.show = show;
            try {
                readShow();
            } catch (IOException e) {
                BukkitUtil.printStacktrace(e);
            }
        }

        public void readShow() throws IOException {

            lines.clear();
            File firework = new File(CircuitCore.inst().getFireworkFolder(), show + ".txt");
            if(!firework.exists()) {
                fyrestone = true;
                firework = new File(CircuitCore.inst().getFireworkFolder(), show + ".fwk");
                if (!firework.exists()) {
                    CraftBookPlugin.logger().severe("Firework File Not Found! " + firework.getName());
                    return;
                }
            }
            else
                fyrestone = false;
            BufferedReader br = new BufferedReader(new FileReader(firework));
            String line = "";
            while ((line = br.readLine()) != null) {

                if(line.trim().isEmpty())
                    continue;
                lines.add(line);
            }

            br.close();
        }

        public void startShow() {

            position = 0;
            if (task != null)
                task.cancel();
            Runnable show;
            if(!fyrestone)
                show = new BasicShowInterpreter();
            else
                show = new FyrestoneInterpreter();
            task = Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), show);
        }

        private class FyrestoneInterpreter implements Runnable {

            Map<String, List<FireworkEffect>> effects = new HashMap<String, List<FireworkEffect>>();
            String currentBuilding = null;
            Location location = BukkitUtil.toSign(getSign()).getLocation();
            float duration = 0.5f;
            FireworkEffect.Builder builder = FireworkEffect.builder();

            public FyrestoneInterpreter() {

            }

            public FyrestoneInterpreter(Map<String, List<FireworkEffect>> effects, String currentBuilding, Location location, float duration, FireworkEffect.Builder builder) {

                this.effects = effects;
                this.currentBuilding = currentBuilding;
                this.location = location;
                this.duration = duration;
                this.builder = builder;
            }

            @Override
            public void run () {

                while (position < lines.size()) {

                    String line = lines.get(position);
                    position++;
                    if (line.trim().startsWith("#") || line.trim().isEmpty())
                        continue;

                    if (line.startsWith("set.")) {

                        if (currentBuilding == null)
                            continue;

                        line = line.replace("set.", "").trim();
                        String[] args = RegexUtil.SPACE_PATTERN.split(line);

                        if (args[0].equalsIgnoreCase("shape")) {

                            FireworkEffect.Type type;
                            if (args[1].equalsIgnoreCase("sball") || args[1].equalsIgnoreCase("smallball"))
                                type = FireworkEffect.Type.BALL;
                            else if (args[1].equalsIgnoreCase("lball") || args[1].equalsIgnoreCase("largeball"))
                                type = FireworkEffect.Type.BALL_LARGE;
                            else if (args[1].equalsIgnoreCase("burst"))
                                type = FireworkEffect.Type.BURST;
                            else if (args[1].equalsIgnoreCase("creeper"))
                                type = FireworkEffect.Type.CREEPER;
                            else if (args[1].equalsIgnoreCase("star"))
                                type = FireworkEffect.Type.STAR;
                            else
                                type = FireworkEffect.Type.BALL;
                            builder.with(type);
                        } else if (args[0].equalsIgnoreCase("color")) {

                            String[] rgb = RegexUtil.COMMA_PATTERN.split(args[1]);
                            Color color = org.bukkit.Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
                            builder.withColor(color);
                        } else if (args[0].equalsIgnoreCase("fade")) {

                            String[] rgb = RegexUtil.COMMA_PATTERN.split(args[1]);
                            Color fade = org.bukkit.Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
                            builder.withFade(fade);
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

                        location = BukkitUtil.toSign(getSign()).getLocation().add(x, y, z);
                    } else if (line.startsWith("duration ")) {

                        duration = Float.parseFloat(line.replace("duration ", ""));
                    } else if (line.startsWith("wait ")) {

                        FyrestoneInterpreter show = new FyrestoneInterpreter(effects,currentBuilding,location,duration,builder);
                        task = Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), show,
                                Long.parseLong(line.replace("wait ", "")));
                        return;
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
                            List<FireworkEffect> effectList = new ArrayList<FireworkEffect>();
                            effectList.add(builder.build());
                            effects.put(currentBuilding, effectList);
                        }
                        currentBuilding = null;
                    } else if (line.startsWith("launch ")) {

                        if(effects.containsKey(line.replace("launch ", ""))) {

                            if(!location.getBlock().getChunk().isLoaded())
                                continue;
                            Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
                            FireworkMeta meta = firework.getFireworkMeta();
                            for(FireworkEffect effect : effects.get(line.replace("launch ", "")))
                                meta.addEffect(effect);
                            meta.setPower((int) duration * 2);
                            firework.setFireworkMeta(meta);
                        }
                    }
                }
            }
        }

        private class BasicShowInterpreter implements Runnable {

            @Override
            public void run() {

                while (position < lines.size()) {

                    String line = lines.get(position);
                    position++;
                    if (line.startsWith("#"))
                        continue;

                    String[] bits = RegexUtil.COLON_PATTERN.split(line, 2);
                    if (bits.length < 2)
                        continue;

                    if (bits[0].equalsIgnoreCase("wait")) {
                        BasicShowInterpreter show = new BasicShowInterpreter();
                        task = Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), show,
                                Long.parseLong(bits[1]));
                        return;
                    } else if (bits[0].equalsIgnoreCase("launch")) {

                        String errorLocation = "Unknown";

                        try {
                            String[] data = RegexUtil.SEMICOLON_PATTERN.split(bits[1]);

                            //Offset data (0)
                            errorLocation = "Offset";
                            String[] offset = RegexUtil.COMMA_PATTERN.split(data[0]);
                            Location location = BukkitUtil.toSign(getSign()).getLocation();
                            location.add(Double.parseDouble(offset[0]), Double.parseDouble(offset[1]),
                                    Double.parseDouble(offset[2]));

                            //Duration data (1)
                            errorLocation = "Duration";
                            double duration = Double.parseDouble(data[1]); //1 duration = 1 second.

                            //Shape data (2)
                            errorLocation = "Shape";
                            FireworkEffect.Type type;
                            if (data[2].equalsIgnoreCase("sball") || data[2].equalsIgnoreCase("smallball"))
                                type = FireworkEffect.Type.BALL;
                            else if (data[2].equalsIgnoreCase("lball") || data[2].equalsIgnoreCase("largeball"))
                                type = FireworkEffect.Type.BALL_LARGE;
                            else if (data[2].equalsIgnoreCase("burst"))
                                type = FireworkEffect.Type.BURST;
                            else if (data[2].equalsIgnoreCase("creeper"))
                                type = FireworkEffect.Type.CREEPER;
                            else if (data[2].equalsIgnoreCase("star"))
                                type = FireworkEffect.Type.STAR;
                            else
                                type = FireworkEffect.Type.BALL;

                            //Colour Data (3)
                            errorLocation = "Colour";
                            String[] rgb = RegexUtil.COMMA_PATTERN.split(data[3]);
                            Color colour = org.bukkit.Color.fromRGB(Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));

                            //Colour Data (4)
                            errorLocation = "Fade";
                            rgb = RegexUtil.COMMA_PATTERN.split(data[4]);
                            Color fade = org.bukkit.Color.fromRGB(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]),
                                    Integer.parseInt(rgb[2]));

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

                            FireworkEffect effect = FireworkEffect.builder().with(type).withColor(colour).withFade
                                    (fade).flicker(flicker).trail(trail).build();

                            if(!location.getBlock().getChunk().isLoaded())
                                continue;

                            Firework firework = (Firework) location.getWorld().spawnEntity(location,
                                    EntityType.FIREWORK);
                            FireworkMeta meta = firework.getFireworkMeta();
                            meta.addEffect(effect);
                            meta.setPower((int) duration * 2);
                            firework.setFireworkMeta(meta);
                        } catch (Exception e) {
                            CraftBookPlugin.logger().severe("Error occured while doing: " + errorLocation + ". Whilst " +
                                    "reading line " + position + " of the firework file " + show + "!");
                            BukkitUtil.printStacktrace(e);
                        }
                    }
                }
            }
        }
    }
}