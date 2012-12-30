package com.sk89q.craftbook.circuits.gates.world.miscellaneous;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitCore;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import com.sk89q.craftbook.util.GeneralUtil;
import com.sk89q.craftbook.util.RegexUtil;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

            if (sign.getLine(2).trim().isEmpty() && new File(CircuitCore.inst().getFireworkFolder(),
                    sign.getLine(2).trim() + ".txt").exists())
                throw new ICVerificationException("A valid firework show is required on line 3!");
        }

        @Override
        public String getDescription() {

            return "Plays a firework show from a file.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "Name of firework show", null
            };
            return lines;
        }
    }

    public class FireworkShowHandler {

        String show;

        int position;

        List<String> lines = new ArrayList<String>();

        BukkitTask task;

        public FireworkShowHandler(String show) {

            this.show = show;
            try {
                readShow();
            } catch (IOException e) {
                Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
            }
        }

        public void readShow() throws IOException {

            lines.clear();
            File firework = new File(CircuitCore.inst().getFireworkFolder(), show + ".txt");
            BufferedReader br = new BufferedReader(new FileReader(firework));
            String line = "";
            while ((line = br.readLine()) != null) {

                lines.add(line);
            }

            br.close();
        }

        public void startShow() {

            position = 0;
            if (task != null)
                task.cancel();
            FireworkShow show = new FireworkShow();
            task = Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), show);
        }

        private class FireworkShow implements Runnable {

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
                        FireworkShow show = new FireworkShow();
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

                            FireworkEffect.Builder builder = FireworkEffect.builder();

                            //Duration data (1)
                            errorLocation = "Duration";
                            int duration = Integer.parseInt(data[1]); //1 duration = 1 second.

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

                            builder.with(type);

                            //Colour Data (3)
                            errorLocation = "Colour";
                            String[] rgb = RegexUtil.COMMA_PATTERN.split(data[3]);
                            builder.withColor(org.bukkit.Color.fromRGB(Integer.parseInt(rgb[0]),
                                    Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2])));

                            if (data.length > 4) {

                                //Shape data (4)
                                errorLocation = "Effects";
                                boolean flicker = false;
                                boolean trail = false;
                                if (data[4].equalsIgnoreCase("twinkle"))
                                    flicker = true;
                                else if (data[4].equalsIgnoreCase("trail"))
                                    trail = true;

                                builder.flicker(flicker);
                                builder.trail(trail);
                            }

                            errorLocation = "Creation";
                            FireworkEffect effect = builder.build();

                            Firework firework = (Firework) location.getWorld().spawnEntity(location,
                                    EntityType.FIREWORK);
                            firework.getFireworkMeta().addEffect(effect);
                            firework.getFireworkMeta().setPower(duration * 2);
                        } catch (Exception e) {
                            Bukkit.getLogger().severe("Error occured while doing: " + errorLocation + ". Whilst " +
                                    "reading line " + position + " of the firework file " + show + "!");
                            Bukkit.getLogger().severe(GeneralUtil.getStackTrace(e));
                        }
                    }
                }
            }
        }
    }
}