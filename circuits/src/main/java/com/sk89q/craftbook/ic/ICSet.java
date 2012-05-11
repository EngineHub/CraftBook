// $Id$
/*
 * CraftBook
 * Copyright (C) 2012 Lymia <lymiahugs@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.ic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class ICSet {
    protected static final Logger logger = Logger.getLogger("Minecraft.CraftBook");

    private static Class<?>[] SIGNATURE_TRIGGER = {ChipState.class, Sign.class};
    private static Class<?>[] SIGNATURE_VERIFY  = {Sign.class};

    private final HashMap<String, ICTemplate> ics = new HashMap<String, ICTemplate>();
    private final HashMap<String, ICFamily> icFamilies = new HashMap<String, ICFamily>();
    private boolean init = false;

    private Server server;
    public ICSet(Server server) {
        this.server = server;
    }

    public String[] getIcList() {
        tryInit();
        String[] array = new String[ics.keySet().size()];
        return ics.keySet().toArray(array);
    }
    public ICTemplate getIcTemplate(String name) {
        tryInit();
        return ics.get(name);
    }
    public ICFamily getIcFamily(String name) {
        tryInit();
        return icFamilies.get(name);
    }
    private void tryInit() {
        if (!init) {
            synchronized (this) {
                if(!init) init();
            }
        }
    }
    private void init() {
        ics.clear();

        Class<?> c = getClass();
        HashMap<String, Method> verifyList = new HashMap<String, Method>();
        for(Method m: c.getMethods()) {
            if(Arrays.equals(m.getParameterTypes(), SIGNATURE_VERIFY)) {
                ICSetVerify v = m.getAnnotation(ICSetVerify.class);
                if(v!=null) {
                    if(verifyList.containsKey(v.value())) {
                        logger.warning("Internal error: Repeat verify method in simple IC set.");

                        ics.clear();
                        init = true;
                        return;
                    }
                    verifyList.put(v.value(),m);
                }
            }
        }
        for(Method m: c.getMethods()) {
            if(Arrays.equals(m.getParameterTypes(), SIGNATURE_TRIGGER)) {
                ICSetTrigger s = m.getAnnotation(ICSetTrigger.class);
                if(s!=null) {
                    if(ics.containsKey(s.name())) {
                        logger.warning("Internal error: Repeat trigger method in simple IC set.");

                        ics.clear();
                        init = true;
                        return;
                    }

                    final Method verify = verifyList.get(s.name());
                    final Method trigger = m;
                    final String title = s.title();
                    final String signTitle = s.signTitle();
                    ics.put(s.name(),new AbstractICTemplate(server) {
                        public String getTitle() {
                            return title;
                        }
                        public String getSignTitle() {
                            return signTitle;
                        }
                        public void trigger(ChipState state, Sign sign) {
                            try {
                                trigger.invoke(ICSet.this, state, sign);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Inaccessible method used in IC set", e);
                            } catch (InvocationTargetException e) {
                                throw new RuntimeException("Method invocation failed", e);
                            }
                        }
                        public void verify(Sign sign) throws ICVerificationException {
                            try {
                                if(verify!=null) verify.invoke(ICSet.this, sign);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException("Inaccessible method used in IC set", e);
                            } catch (InvocationTargetException e) {
                                if(e.getCause() instanceof ICVerificationException) {
                                    //Why couldn't we be /normal/ and use a return value?
                                    throw (ICVerificationException) e.getCause();
                                }
                                
                                throw new RuntimeException("Method invocation failed", e);
                            }
                        }
                    });
                    try {
                        icFamilies.put(s.name(),s.family().newInstance());
                    } catch (InstantiationException e) {
                        throw new RuntimeException("Family construction failed", e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Inaccessible IC family used in IC set", e);
                    }
                }
            }
        }

        init = true;
    }
}
