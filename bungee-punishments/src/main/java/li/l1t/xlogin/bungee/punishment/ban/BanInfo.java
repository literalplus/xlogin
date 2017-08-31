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

package li.l1t.xlogin.bungee.punishment.ban;

import li.l1t.xlogin.bungee.config.LocalisedMessageConfig;
import li.l1t.xlogin.bungee.punishment.AbstractPunishment;
import li.l1t.xlogin.common.api.punishments.XLoginBan;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a ban list entry for a specific user.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 27.8.14
 */
public class BanInfo extends AbstractPunishment implements XLoginBan {
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    public static String BAN_TABLE_NAME = "mt_main.xlogin_bans";
    private final BanModule manager;
    @Nullable
    private Timestamp expiryTime;

    protected BanInfo(@Nonnull BanModule manager, @Nonnull UUID targetId, @Nonnull UUID sourceId, @Nonnull String reason, @Nullable String sourceServerName,
                      @Nonnull Timestamp timestamp, @Nullable Timestamp expiryTime) {
        super(targetId, sourceId, timestamp, sourceServerName, reason);
        this.expiryTime = expiryTime;
        this.manager = manager;
    }

    @Override
    @Nullable
    public Timestamp getExpiryTime() {
        return expiryTime;
    }

    @Override
    @Nonnull
    public BanInfo save() {
        BanInfoFactory.save(this);
        return this;
    }

    @Override
    public void delete() {
        BanInfoFactory.delete(this);
        expiryTime = new Timestamp(System.currentTimeMillis() - 1);
        manager.setBannedCache(getTargetId(), null);
    }

    @Override
    public boolean isValid() {
        return expiryTime == null || System.currentTimeMillis() < expiryTime.getTime();
    }

    public BaseComponent[] createKickMessage() {
        ComponentBuilder cb = new ComponentBuilder("[WARNUNG]\n").color(ChatColor.DARK_RED).bold(true)
                .append("Du bist ").color(ChatColor.RED).bold(false);

        appendExpiryTime(cb);

        return cb.append(" gebannt:\n\n").bold(false).color(ChatColor.RED)
                .append(ChatColor.stripColor(getReason())).color(ChatColor.YELLOW)
                .append("\n\nUnfair? ").color(ChatColor.RED)
                .append("Entbannantrag im Forum: ").color(ChatColor.GOLD)
                .append("http://www.minotopia.me/forum/").color(ChatColor.YELLOW).underlined(true)
                .create();
    }

    private ComponentBuilder appendExpiryTime(ComponentBuilder cb) {
        cb.append(getExpiryString());
        if (expiryTime == null) {
            cb.bold(true).color(ChatColor.DARK_RED);
        } else {
            cb.color(ChatColor.YELLOW);
        }
        return cb;
    }

    public void announce(BanModule module) {
        LocalisedMessageConfig messages = module.getPlugin().getMessages();
        BaseComponent[] adminComponents = messages.parseMessageWithPrefix(
                "§7§oBan von " + getSourceName(module.getPlugin().getRepository()) + ":");
        BaseComponent[] headerComponents = messages.parseMessageWithPrefix(
                messages.banBroadcastHeader,
                getTargetName(module.getPlugin().getRepository()),
                (expiryTime == null ? "§4§l" : "§e") + getExpiryString());
        BaseComponent[] bodyComponents = messages.parseMessageWithPrefix(
                messages.banBroadcastBody,
                getReason());

        for (ProxiedPlayer plr : module.getPlugin().getProxy().getPlayers()) {
            if (plr.hasPermission("xlogin.adminmsg")) {
                plr.sendMessage(adminComponents);
            }
            plr.sendMessage(headerComponents);
            plr.sendMessage(bodyComponents);
        }
    }

    @Override
    public String getExpiryString() {
        if (expiryTime == null) {
            return "permanent";
        } else {
            return "bis zum " + SIMPLE_DATE_FORMAT.format(new Date(getExpiryTime().getTime()));
        }
    }

    public String getTimestampString() {
        return SIMPLE_DATE_FORMAT.format(getDate());
    }
}
