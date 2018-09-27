package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.mechanics.pipe.PipePutEvent;

public interface PipeInputIC {

    /**
     * Called when a pipe transfers items into an {@link IC}.
     *
     * @param event The event that the pipe is sending.
     */
    void onPipeTransfer(PipePutEvent event);
}