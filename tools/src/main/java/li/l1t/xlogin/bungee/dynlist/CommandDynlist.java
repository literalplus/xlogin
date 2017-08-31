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

import li.l1t.common.util.CommandHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 29/12/14
 */
public class CommandDynlist extends Command {
    private final DynlistModule manager;

    public CommandDynlist(DynlistModule manager) {
        super("dynlist", "xlogin.dynlist", "gdl");
        this.manager = manager;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && !args[0].equalsIgnoreCase("help")) {
            switch (args[0].toLowerCase()) {
                case "list":
                    int i = 0;
                    for (DynlistEntry entry : manager.getEntries()) {
                        sender.sendMessage(new ComponentBuilder(entry.getName() + " -> /")
                                        .color(ChatColor.YELLOW)
                                        .append(entry.getRegex().pattern())
                                        .color(ChatColor.BLUE)
                                        .append("/i")
                                        .color(ChatColor.YELLOW)
                                        .create()
                        );
                        i++;
                    }
                    sender.sendMessage(new ComponentBuilder(i + " Dynlists gesamt").color(ChatColor.RED).create());
                    return;
                case "add":
                    if (args.length < 3) {
                        break;
                    }
                    DynlistEntry entry = manager.addEntry(args[1], args[2]);
                    sender.sendMessage(new ComponentBuilder("Dynlist hinzugefügt. ").color(ChatColor.GREEN).create());
                    sender.sendMessage(new ComponentBuilder("Blockierte Server: ").color(ChatColor.YELLOW)
                            .append(CommandHelper.CSCollection(entry.getMatchedServers(), " - keine - ")).color(ChatColor.GREEN)
                            .create());
                    return;
                case "remove":
                    if (args.length < 2) {
                        break;
                    }
                    DynlistEntry removed = manager.delete(args[1]);
                    if (removed == null) {
                        sender.sendMessage(new ComponentBuilder("Unbekannte Dynlist.").color(ChatColor.RED).create());
                    } else {
                        sender.sendMessage(new ComponentBuilder("Dynlist entfernt: " + removed.toString()).color(ChatColor.YELLOW).create());
                    }
                    return;
                case "test":
                    if (args.length < 2) {
                        break;
                    }
                    sender.sendMessage(new ComponentBuilder("Blockierende Dynlisten: " +
                            CommandHelper.CSCollection(manager.getMatches(args[1]), " - keine - "))
                            .color(ChatColor.YELLOW).create());
                    return;
                case "global":
                    if (args.length < 2) {
                        break;
                    }
                    switch (args[1].toLowerCase()) {
                        case "on":
                            if (manager.exists("~global")) {
                                sender.sendMessage(new ComponentBuilder("Already enabled.").color(ChatColor.GREEN).create());
                            } else {
                                manager.addEntry("~global", ".*");
                                sender.sendMessage(new ComponentBuilder("Enabled global whitelist.").color(ChatColor.GREEN).create());
                            }
                            return;
                        case "off":
                            if (!manager.exists("~global")) {
                                sender.sendMessage(new ComponentBuilder("Not enabled.").color(ChatColor.RED).create());
                            } else {
                                manager.delete("~global");
                                sender.sendMessage(new ComponentBuilder("Disabled global whitelist.").color(ChatColor.RED).create());
                            }
                            return;
                        default:
                            sender.sendMessage(new ComponentBuilder("[on|off]").color(ChatColor.YELLOW).create());
                            break;
                    }
                default:
                    sender.sendMessage(new ComponentBuilder("Unbekannte Aktion. Hilfe:").color(ChatColor.RED).create());
            }
        }
        sendHelpLine(sender, "/dynlist list", " Listet alle Dynlists auf");
        sendHelpLine(sender, "/dynlist add <Name> <RegEx>", " Fügt eine neue Synlist hinzu");
        sendHelpLine(sender, "/dynlist remove <Name>", " Entfernt einen Eintrag");
        sendHelpLine(sender, "/dynlist test <Eingabe>", " Testet, ob die Eingabe von einer Dynlist blockiert werden würde");
        sendHelpLine(sender, "/dynlist global <on|off>", " Aktiviert die globale Whitelist");
        sender.sendMessage(new ComponentBuilder("Die Permission zum Umgehen der Dynlist ist jeweils xlogin.dlby.<Name>").color(ChatColor.YELLOW).create());
        sender.sendMessage(new ComponentBuilder("Die globale Umgehpermission ist xlogin.dynlist").color(ChatColor.RED).create());
    }

    private void sendHelpLine(CommandSender sender, String commandString, String description) {
        //@formatter:off
        sender.sendMessage(new ComponentBuilder(commandString)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken zum Kopieren").create()))
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, commandString))
                    .color(ChatColor.YELLOW).underlined(true)
                .append(description)
                    .color(ChatColor.GOLD).underlined(false)
                .create());
    }
}
