package io.github.xxyy.xlogin.bungee.dynlist;

import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;

import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.common.module.XLoginModule;
import io.github.xxyy.xlogin.common.module.annotation.CanHasPotato;

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

        return removed;
    }

    public DynlistEntry addEntry(String name, String regex) {
        DynlistEntry entry = new DynlistEntry(name, Pattern.compile(regex));
        entries.add(entry);
        plugin.getConfig().getDynlistEntries().add(entry.serialize());
        return entry;
    }

}
