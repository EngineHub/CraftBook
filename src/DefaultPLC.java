import lymia.plc.PlcBase;
import lymia.plc.PlcException;
import lymia.plc.PlcLang;

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.VIVOFamilyIC;
import com.sk89q.craftbook.ic._3I3OFamilyIC;

public class DefaultPLC extends PlcBase implements VIVOFamilyIC, _3I3OFamilyIC {
    public DefaultPLC(PlcLang language) {super(language);}
    
    public String getTitle() {
        return "PLC ("+getLanguage().getName()+")";
    }
    protected String validateEnviromentEx(Vector v, SignText t) {return null;}
    protected String getCode(Vector v) throws PlcException { 
        Server s = etc.getServer();
        StringBuilder b = new StringBuilder();
        int x = v.getBlockX();
        int z = v.getBlockZ();
        int x0 = x;
        int y0 = v.getBlockY();
        int z0 = z;
        for(int y=0;y<128;y++) if(CraftBook.getBlockID(x,y,z)==BlockType.WALL_SIGN) { 
            if(((Sign)s.getComplexBlock(x, y, z)).getText(1).equalsIgnoreCase("[CODE BLOCK]"))
                for(y--;y>=0;y--) if(!(x==x0&&y==y0&&z==z0)&&CraftBook.getBlockID(x,y,z)==BlockType.WALL_SIGN) {
                    Sign n = (Sign) s.getComplexBlock(x, y, z);
                    b.append(n.getText(0)+"\n");
                    b.append(n.getText(1)+"\n");
                    b.append(n.getText(2)+"\n");
                    b.append(n.getText(3)+"\n");
                } else return b.toString();
        }
        throw new PlcException("code not found");
    }
}
