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

import li.l1t.xlogin.bungee.XLoginBungee;
import li.l1t.xlogin.common.module.XLoginModule;
import li.l1t.xlogin.common.module.annotation.CanHasPotato;
import li.l1t.xlogin.common.module.annotation.Module;
import lombok.Getter;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages various aspects of Dynlist, xLogin's dynamic whitelist system.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28/12/14
 */
@Module(enableByDefault = true)
public class DynlistModule extends XLoginModule {
    @Getter
    private final List<DynlistEntry> entries = new ArrayList<>();
    @Getter @CanHasPotato(XLoginBungee.class)
    private XLoginBungee plugin;

    @Override
    public void enable() {
        reload();
        plugin.getProxy().getPluginManager().registerCommand(plugin, new CommandDynlist(this));
        plugin.getProxy().getPluginManager().registerListener(plugin, new DynlistListener(this));
    }

    public void reload() {
        entries.clear();
        for(String serialized : plugin.getConfig().getDynlistEntries()) {
            DynlistEntry entry = DynlistEntry.deserialize(serialized);
            entries.add(entry);
        }
    }

    public List<DynlistEntry> getMatches(ServerInfo input) {
        return getMatches(input.getName());
    }

    public boolean exists(String name) {
        for(DynlistEntry entry : entries) {
            if(entry.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public List<DynlistEntry> getMatches(String input) {
        List<DynlistEntry> matches = new LinkedList<>();

        for(DynlistEntry entry : entries) {
            if(entry.matches(input)) {
                matches.add(entry);
            }
        }

        return matches;
    }

    public DynlistEntry delete(String name) {
        DynlistEntry removed = null;

        Iterator<DynlistEntry> iterator = entries.iterator();
        while(iterator.hasNext()) {
            DynlistEntry entry = iterator.next();
            if(entry.getName().equalsIgnoreCase(name)) {
                iterator.remove();
                removed = entry;
                break;
            }
        }

        if(removed != null) {
            plugin.getConfig().getDynlistEntries().remove(removed.serialize());
            try {
                plugin.getConfig().save();
            } catch (InvalidConfigurationException e) {
                throw new IllegalStateException(e);
            }
        }

        return removed;
    }

    public DynlistEntry addEntry(String name, String regex) {
        DynlistEntry entry = new DynlistEntry(name, Pattern.compile(regex));
        entries.add(entry);
        plugin.getConfig().getDynlistEntries().add(entry.serialize());

        try {
            plugin.getConfig().save();
        } catch (InvalidConfigurationException e) {
            throw new IllegalStateException(e);
        }

        return entry;
    }

}
