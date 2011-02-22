

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.sk89q.craftbook.access.Action;
import com.sk89q.craftbook.access.WorldInterface;

/**
 * Thread for delaying redstone inputs.
 * 
 * @author Lymia
 * @author sk89q
 */
public class TickDelayer implements Runnable {
    /**
     * List of actions to delay.
     */
    private Set<Action> delayedActions = Collections.synchronizedSet(new LinkedHashSet<Action>());

    private WorldInterface world;
    
    public TickDelayer(WorldInterface world) {
        this.world = world;
    }
    
    /**
     * Delay an action.
     * 
     * @param action
     */
    public void delayAction(Action action) {
        delayedActions.add(action);
    }

    /**
     * Run thread.
     */
    public void run() {
        long currentTick = world.getTime();
        
        ArrayList<Action> actionQueue = new ArrayList<Action>();
        
        for (Iterator<Action> it = delayedActions.iterator(); it.hasNext(); ) {
            Action action = it.next();
            if (action.getRunAt() <= currentTick) {
                it.remove();
                actionQueue.add(action);
            }
        }
        
        for (Action action : actionQueue) {
            try {
                action.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}
