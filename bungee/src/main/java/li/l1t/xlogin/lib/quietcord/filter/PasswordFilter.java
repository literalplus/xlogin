/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
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
