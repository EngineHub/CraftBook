// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.craftbook.ic.set;

import com.sk89q.craftbook.ic.core.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * A base abstract IC that all ICs can inherit from.
 * 
 * @author sk89q
 */
public abstract class AbstractICTemplate implements ICTemplate {

    private Server server;

    public AbstractICTemplate(Server server) {
        this.server = server;
    }

    /**
     * Recieve a new state to process on.
     *
     * @param chip chip state.
     */
    public abstract void trigger(ChipState chip, Sign block);

    /**
     * @return the title of the IC.
     */
    public abstract String getTitle();

    /**
     * @return the title that is shown on the sign.
     */
    public abstract String getSignTitle();

    public void verify(Sign sign) throws ICVerificationException {
        // No default check needed; if the sign just has the right ID string,
        // that's good enough in most cases.
    }

    public Server getServer() {
        return server;
    }

    public static class TemplateIC extends AbstractIC {
        private ICTemplate template;
        private Sign sign;

        public TemplateIC(ICTemplate template, Sign sign) {
            super(template.getServer(), sign);
            this.template = template;
            this.sign = sign;
        }

        @Override
        public String getTitle() {
            return template.getTitle();
        }
        @Override
        public String getSignTitle() {
            return template.getSignTitle();
        }
        @Override
        public void trigger(ChipState s) {
            template.trigger(s, sign);
        }
    }
    public static class TemplateFactory extends AbstractICFactory {
        private ICTemplate template;

        public TemplateFactory(ICTemplate template) {
            super(template.getServer());
            this.template = template;
        }

        @Override
        public IC create(Sign s) {
            return new TemplateIC(template, s);
        }

        @Override
        public void verify(Sign s) throws ICVerificationException {
            template.verify(s);
        }
    }
}