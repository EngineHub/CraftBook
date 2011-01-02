/**
 * Takes in a clock input, and outputs whether a specified player is online.
 *
 * @author Tom (tmhrtly)
 */

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;
import java.util.List;

public class MC1500 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "IS PLAYER ONLINE";
    }
	
	/**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
	
	public String validateEnvironment(Vector pos, SignText sign) {
        String id = sign.getLine3();

        if (id.length() == 0 || id.contains(" ")) {
            return "Put a player's name on line 3, with no spaces.";
        }

        if (!sign.getLine4().equals("")) {
            return "Line 4 must be blank";
        }
        return null;
    }
    
	
    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
		String thePlayer = chip.getText().getLine3();
        if (isPlayerOnline(thePlayer))
            chip.getOut(1).set(true);
        else 
            chip.getOut(1).set(false);
    }

	private boolean isPlayerOnline(String playerName) {
		List players = etc.getServer().getPlayerList();
		for (int i=0; i< players.size(); i++) {
			Player aPlayer = (Player) players.get(i);
	  		if (aPlayer.getName().equals(playerName)) {
				return true;
			}
		}
		return false;
	}
}
