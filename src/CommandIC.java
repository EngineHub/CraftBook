import java.util.Hashtable;

/**
 * Command IC stuff. See also: MC0990
 * User: Robhol
 * Date: 13.01.11
 */
public class CommandIC
{

    static Hashtable<String, String> pending = new Hashtable<String,String>();

    public static void addPendingTrigger(String chipID, String code)
    {
        if (pending.containsKey(chipID))
            return;

        pending.put(chipID, code);
    }

    public static String check(String chipID)
    {
        return pending.remove(chipID); // null if not existing, otherwise returning code.
    }

}
