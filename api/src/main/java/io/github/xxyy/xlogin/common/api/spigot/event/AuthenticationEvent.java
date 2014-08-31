package io.github.xxyy.xlogin.common.api.spigot.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import io.github.xxyy.xlogin.common.api.XLoginProfile;

/**
 * This event is fired when a player authenticates.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public class AuthenticationEvent extends PlayerEvent {
    private static HandlerList HANDLERS = new HandlerList();

    private final XLoginProfile profile;

    public AuthenticationEvent(Player who, XLoginProfile profile) {
        super(who);
        this.profile = profile;
    }

    //Apparently Bukkit needs that AND IT'S NOT DOCUMENTED
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
