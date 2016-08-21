/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee;

import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import javax.annotation.Nonnull;
import java.util.Collection;

import static net.md_5.bungee.api.ChatColor.*;

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
    public static void listPossiblePlayers(@Nonnull CommandSender receiver, @Nonnull Collection<AuthedPlayer> matches, @Nonnull XLoginBungee plugin,
                                           String actionText, @Nonnull String suggestedCommand) {
        plugin.getMessages().sendMessageWithPrefix("§cIch habe mehrere Spieler gefunden. Meintest du:", receiver);

        for (AuthedPlayer authedPlayer : matches) {
            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("UUID: " + authedPlayer.getUniqueId() + "\n"+actionText).create());
            ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format(suggestedCommand, authedPlayer.getUniqueId()));
            receiver.sendMessage(new ComponentBuilder(authedPlayer.getName() + " / ").color(GOLD).underlined(true)
                    .event(hoverEvent).event(clickEvent)
                    .append(authedPlayer.isPremium() ? "Premium" : "Cracked").color(authedPlayer.isPremium() ? GREEN : RED).underlined(true)
                    .event(hoverEvent).event(clickEvent)
                    .append(" (klick!)").color(YELLOW).underlined(true)
                    .event(hoverEvent).event(clickEvent)
                    .create());
        }
    }
}
