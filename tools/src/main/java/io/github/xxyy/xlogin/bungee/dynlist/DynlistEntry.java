/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.bungee.dynlist;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * An entry of Dynlist, xLogin's dynamic whitelisting system.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28/12/14
 */
@Data @AllArgsConstructor
class DynlistEntry {
    private final String name;
    private final Pattern regex;

    public boolean matches(ServerInfo server) {
        return matches(server.getName());
    }

    public boolean matches(String input) {
        return regex.matcher(input).matches();
    }

    public String serialize() {
        return name + "/" + regex;
    }

    public List<String> getMatchedServers() {
        List<String> servers = new LinkedList<>();
        for(ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            if(matches(serverInfo)) {
                servers.add(serverInfo.getName());
            }
        }
        return servers;
    }

    public static DynlistEntry deserialize(String input) {
        String[] parts = input.split("/", 2);
        Preconditions.checkArgument(input.length() >= 2, "Input must contain at least one slash!");
        return new DynlistEntry(parts[0], Pattern.compile(parts[1], Pattern.CASE_INSENSITIVE));
    }


}
