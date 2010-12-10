package lymia.customic;

import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;

import lymia.plc.PlcBase;
import lymia.plc.PlcException;
import lymia.plc.PlcLang;

public class CustomICBase extends PlcBase {
    private final String name, code;
    public CustomICBase(PlcLang language, String name, String code) {
        super(language);
        this.name = name;
        this.code = code;
    }

    public String getTitle() {
        return name;
    }
    
    protected String getCode(Vector v) throws PlcException {
        return code;
    }
    protected String validateEnviromentEx(Vector v, SignText t) {
        return null;
    }
}
