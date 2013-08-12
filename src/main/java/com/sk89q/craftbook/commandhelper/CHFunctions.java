package com.sk89q.craftbook.commandhelper;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.AbstractFunction;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class CHFunctions {

    @api
    public static class GetVariable extends AbstractFunction {

        @Override
        public Construct exec (Target arg0, Environment arg1, Construct... arg2) throws ConfigRuntimeException {

            String varName = arg2[0].val();
            String namespace = "global";
            if(arg2.length == 2)
                namespace = arg2[1].val();

            String varValue = CraftBookPlugin.inst().getVariable(varName, namespace);
            if(varValue == null)
                varValue = "Unknown Variable!";

            return new CString(varValue, arg0);
        }

        @Override
        public boolean isRestricted () {
            return false;
        }

        @Override
        public Boolean runAsync () {
            return null;
        }

        @Override
        public ExceptionType[] thrown () {
            return new ExceptionType[]{

            };
        }

        @Override
        public String docs() {
            return "array {variable, namespace} Get's a CraftBook Variable. If no namespace is provided, the global namespace will be used, otherwise the provided namespace will be used.";
        }

        @Override
        public String getName () {
            return "cb_getvar";
        }

        @Override
        public Integer[] numArgs () {
            return new Integer[]{1,2};
        }

        @Override
        public Version since () {
            return CHVersion.V3_0_1;
        }
    }
}