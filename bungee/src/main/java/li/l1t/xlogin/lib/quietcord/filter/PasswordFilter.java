/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * The following file can also be found in QuietCord, which is a F/OSS project by me.
 * You can obtain the full source at https://github.com/xxyy/quietcord .
 */

package li.l1t.xlogin.lib.quietcord.filter;

import net.md_5.bungee.api.plugin.Plugin;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A log that filters /login and /register in the BungeeCord command log for security purposes.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 02/10/15
 */
public class PasswordFilter extends PropagatingFilter {
    public PasswordFilter(Plugin plugin) {
        super(plugin.getProxy().getLogger());
    }

    PasswordFilter(Logger logger) { //for unit tests
        super(logger);
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        //BungeeCord source: https://github.com/SpigotMC/BungeeCord/blob/master/api/src/main/java/net/md_5/bungee/api/plugin/PluginManager.java#L164
        if (!"{0} executed command: /{1}".equals(record.getMessage()) || //wrong message
                record.getParameters().length != 2 || //that message always has exactly two arguments
                !(record.getParameters()[1] instanceof String) //second parameter is raw message
                ){
            return true;
        }

        String param2 = String.valueOf(record.getParameters()[1]);
        return !(param2.startsWith("register") || param2.startsWith("login")); //Don't log
    }

    @Override
    public String toString() {
        return "PasswordFilter{}";
    }
}
