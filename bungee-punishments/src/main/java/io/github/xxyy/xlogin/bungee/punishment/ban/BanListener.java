package io.github.xxyy.xlogin.bungee.punishment.ban;

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
