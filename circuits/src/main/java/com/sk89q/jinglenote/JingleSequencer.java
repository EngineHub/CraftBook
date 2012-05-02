// $Id$
/*
 * Tetsuuuu plugin for SK's Minecraft Server
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 * All rights reserved.
*/

package com.sk89q.jinglenote;

/**
 * Interface for a sequencer.
 * 
 * @author sk89q
 */
public interface JingleSequencer {
    public void run(JingleNotePlayer player) throws InterruptedException;
    public void stop();
}
