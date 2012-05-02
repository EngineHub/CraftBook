package com.sk89q.craftbook.gates.world;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;
import com.sk89q.jinglenote.JingleNoteComponent;
import com.sk89q.jinglenote.MidiJingleSequencer;
import com.sk89q.minecraft.util.commands.CommandException;

public class Melody extends AbstractIC{

	public Melody(Server server, Sign block) {
		super(server, block);
	}

	@Override
	public String getTitle() {
		return "Melody Player";
	}

	@Override
	public String getSignTitle() {
		return "MELODY";
	}

	@Override
	public void trigger(ChipState chip) {
		try {
			if(chip.getInput(0))
			{
				String midiName = getSign().getLine(2);

	            File[] trialPaths = {
	                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName),
	                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName + ".mid"),
	                    new File(CircuitsPlugin.getInst().getDataFolder(), "midi/" + midiName + ".midi"),
	                    new File("midi", midiName),
	                    new File("midi", midiName + ".mid"),
	                    new File("midi", midiName + ".midi"),
	            };

	            File file = null;

	            for (File f : trialPaths) {
	                if (f.exists()) {
	                    file = f;
	                    break;
	                }
	            }

	            if (file == null) {
	            	getServer().getLogger().log(Level.SEVERE, "Midi file not found!");
	                return;
	            }
				
				MidiJingleSequencer sequencer = new MidiJingleSequencer(file);
				for (Player player : getServer().getOnlinePlayers()) {
					JingleNoteComponent jNote = new JingleNoteComponent();
					jNote.getJingleNoteManager().play(player, sequencer, 0);
					player.sendMessage(ChatColor.YELLOW + "Playing " + midiName + "...");
				}
			}
		}
		catch(Exception e){
			getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: Midi Failed To Play!");
			final Writer result = new StringWriter();
		    final PrintWriter printWriter = new PrintWriter(result);
		    e.printStackTrace(printWriter);
			getServer().getLogger().log(Level.SEVERE, "[CraftBookCircuits]: " + result.toString());
		}
	}
	
    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new Melody(getServer(), sign);
        }
    }

}
