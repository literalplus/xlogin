/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.api.spigot.event;

import io.github.xxyy.xlogin.common.api.XLoginProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

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
