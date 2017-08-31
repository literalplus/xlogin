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

package li.l1t.xlogin.bungee.dynlist;

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
