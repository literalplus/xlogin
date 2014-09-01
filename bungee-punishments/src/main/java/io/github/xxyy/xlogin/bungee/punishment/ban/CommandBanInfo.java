package io.github.xxyy.xlogin.bungee.punishment.ban;

import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;
import static net.md_5.bungee.api.ChatColor.YELLOW;

/**
 * Command which displays information about bans.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30.8.14
 */
class CommandBanInfo extends Command implements TabExecutor {
    private final BanModule module;

    public CommandBanInfo(BanModule module) {
        super("baninfo", "xlogin.baninfo", "bi");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(new ComponentBuilder("/baninfo <Spieler>").color(GOLD).create());
            return;
        }

        List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[0]);
        if (matchedPlayers.isEmpty()) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium ist uns kein Benutzer bekannt!").color(ChatColor.RED).create());
        } else if (matchedPlayers.size() > 1) {
            sender.sendMessage(new ComponentBuilder("Für dein Suchkriterium sind zu viele Benutzer vorhanden: " + matchedPlayers.size())
                    .color(ChatColor.RED).create());
        }

        AuthedPlayer match = matchedPlayers.get(0);
        BanInfo banInfo = module.getBanInfo(match.getUniqueId());

        if (banInfo == null) {
            ComponentBuilder cb = new ComponentBuilder(match.getName()).color(ChatColor.DARK_GREEN)
                    .append(" ist nicht gebannt!").color(ChatColor.GREEN);

            if (sender.hasPermission(CommandBan.PERMISSION)) {
                cb.append(" [bannen]").color(RED).underlined(true)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ban " + match.getName() + " "))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken zum Bannen!").create()));
            }

            sender.sendMessage(cb.create());
            return;
        }

        sender.sendMessage(new ComponentBuilder("==== ").color(GOLD)
                .append("BanInfo: " + match.getName()).color(YELLOW)
                .append(" ====").color(GOLD).create());

        ComponentBuilder uuidBuilder = new ComponentBuilder("UUID: ").color(GOLD)
                .append(match.getUniqueId().toString()).color(YELLOW);

        if (sender.hasPermission("xlogin.admin")) {
            uuidBuilder.append(" [Info]").color(RED).underlined(true)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xlo user " + match.getUniqueId().toString()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Premium? ").color(GOLD)
                                    .append(match.isPremium() ? "ja\n" : "nein\n").color(match.isPremium() ? GREEN : RED)
                                    .append("Authentifiziert: ").color(GOLD)
                                    .append(String.valueOf(match.getAuthenticationProvider()) + "\n").color(YELLOW)
                                    .append("Letzte IP: ").color(GOLD)
                                    .append(String.valueOf(match.getLastIp())).color(YELLOW)
                                    .append("\n[Klick für mehr Info]")
                                    .create()
                    ));
        }
        sender.sendMessage(uuidBuilder.create());

        sender.sendMessage(new ComponentBuilder("gebannt von: ").color(GOLD)
                .append(String.valueOf(banInfo.getSourceName(module.getPlugin().getRepository()))).color(YELLOW).create());
        sender.sendMessage(new ComponentBuilder("gebannt um: ").color(GOLD)
                .append(banInfo.getTimestampString()).color(YELLOW).create());

        ComponentBuilder bannedUntilBuilder = new ComponentBuilder("gebannt bis: ").color(GOLD)
                .append(banInfo.getExpiryString()).color(YELLOW);

        if (sender.hasPermission(CommandUnBan.PERMISSION)) {
            bannedUntilBuilder.append(" [entbannen]").color(GREEN).underlined(true)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/unban " + match.getName()))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/unban " + match.getName()).create()));
        }

        sender.sendMessage(bannedUntilBuilder.create());

        sender.sendMessage(new ComponentBuilder("Grund: ").color(GOLD)
                .append(banInfo.getReason()).color(YELLOW).create());
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
        } else {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                matches.add(player.getName());
            }
        }

        return matches;
    }
}
