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

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * Listens for ban-related events.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 30.8.14
 */
public class BanListener implements Listener {
    private final BanModule module;

    public BanListener(BanModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(LoginEvent evt) {
        BanInfo banInfo = module.forceGetBanInfo(evt.getConnection().getUniqueId());

        if (banInfo != null) {
            evt.setCancelled(true);
            evt.setCancelReason(TextComponent.toLegacyText(banInfo.createKickMessage())); //TODO: BungeeCord needs a PR for this
        }
    }
}
