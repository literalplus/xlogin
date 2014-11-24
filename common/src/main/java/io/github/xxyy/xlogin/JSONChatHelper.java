package io.github.xxyy.xlogin;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.common.authedplayer.AuthedPlayer;

import java.util.Collection;

import static net.md_5.bungee.api.ChatColor.GOLD;
import static net.md_5.bungee.api.ChatColor.GREEN;
import static net.md_5.bungee.api.ChatColor.RED;

/**
 * Provides static utility methods to help daling with JSON chat.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 25/11/14
 */
public final class JSONChatHelper {
    private JSONChatHelper() {

    }

    /**
     * Lists a collection of players in the context of multiple targets being found for a command where only one target
     * is applicable. This sends a readable representation to receiver using JSON chat, with support for suggesting commands
     * and displaying additional call-to-action text.
     * @param receiver the receiver of this method's output
     * @param matches a collection of players to list
     * @param plugin the plugin to use to get the chat prefix for displaying a helpful message
     * @param actionText the text added to every player's tooltip, describing what happens when it is clicked
     * @param suggestedCommand the command to suggest upon click, where %s is being replaced by every player's UUID
     */
    public static void listPossiblePlayers(CommandSender receiver, Collection<AuthedPlayer> matches, XLoginBungee plugin,
                                           String actionText, String suggestedCommand) {
        plugin.getMessages().sendMessageWithPrefix("§cIch habe mehrere Spieler gefunden. Meintest du:", receiver);

        for (AuthedPlayer authedPlayer : matches) {
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("UUID: " + authedPlayer.getUniqueId() + "\n"+actionText).create());
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format(suggestedCommand, authedPlayer.getUniqueId()));
            receiver.sendMessage(new ComponentBuilder(authedPlayer.getName() + " / ").color(GOLD)
                    .event(hoverEvent).event(clickEvent)
                    .append(authedPlayer.isPremium() ? "Premium" : "Cracked").color(authedPlayer.isPremium() ? GREEN : RED)
                    .event(hoverEvent).event(clickEvent)
                    .create());
        }
    }
}
