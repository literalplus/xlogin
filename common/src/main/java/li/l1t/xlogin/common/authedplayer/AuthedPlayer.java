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

package li.l1t.xlogin.common.authedplayer;

import li.l1t.common.chat.XyComponentBuilder;
import li.l1t.common.util.ToShortStringable;
import li.l1t.common.util.encryption.PasswordHelper;
import li.l1t.xlogin.common.api.XLoginProfile;
import li.l1t.xlogin.common.ips.IpAddress;
import li.l1t.xlogin.common.ips.IpAddressFactory;
import li.l1t.xlogin.common.ips.SessionHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;

/**
 * Represents a player that is authenticated with xLogin.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
//TODO common interface w/ PlayerWrapper
public class AuthedPlayer implements ToShortStringable, XLoginProfile {
    public static final String AUTH_DATA_TABLE_NAME = "mt_main.xlogin_data";
    private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final AuthedPlayerRepository repository;
    private String uuid;
    private UUID uniqueId;

    private String name;
    private boolean premium;
    private boolean disabledPremiumMessage;

    private String password; //Yes, encrypted...
    private String salt;
    @Nullable
    private String lastIp;
    private Timestamp registrationDate;
    @Nullable
    private Timestamp lastLoginDate;
    private boolean sessionsEnabled;

    private boolean valid = true;
    @Nullable
    private AuthenticationProvider authenticationProvider = null;
    private boolean authenticated = false;

    protected AuthedPlayer(AuthedPlayerRepository repository, @Nonnull String uuid, String name, String password,
                           String salt, @Nullable String lastIp, boolean premium, boolean disabledPremiumMessage,
                           Timestamp registrationDate, @Nullable Timestamp lastLoginDate, boolean sessionsEnabled) {
        this.repository = repository;
        this.uuid = uuid;
        this.uniqueId = UUID.fromString(uuid);
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.lastIp = lastIp;
        this.premium = premium;
        this.disabledPremiumMessage = disabledPremiumMessage;
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
        this.sessionsEnabled = sessionsEnabled;
    }

    /**
     * Registers that this user has a Minecraft Premium account and is logged in.
     *
     * @return true if the user has opted in to use premium authentication and was logged in. False otherwise.
     */
    public boolean authenticatePremium(@Nonnull String currentIp) {
        if (this.isPremium()) {
            authenticate(AuthenticationProvider.MINECRAFT_PREMIUM, currentIp);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Authenticates this user by a given password.
     * This also updates their IP address if login succeeded.
     * This saves a failed login attempt if the authentication fails.
     *
     * @param password Password to use to authenticate
     * @param newIp    Current IP of this user
     * @return whether the authentication succeeded.
     */
    public boolean authenticatePassword(@Nullable String password, @Nonnull String newIp) {
        if (password == null || getPassword() == null ||
                password.isEmpty() || getPassword().isEmpty() ||
                !PasswordHelper.passwordsEqual(password, getSalt(), getPassword())) {

//            FailedLoginAttempt loginAttempt = new FailedLoginAttempt();
//            loginAttempt.setIp(newIp);
//            loginAttempt.setUser(this);
//            loginAttempt.setTimestamp(new Timestamp(System.currentTimeMillis()));
//            EbeanManager.getEbean().save(loginAttempt);

            return false;
        }

        authenticate(AuthenticationProvider.XLOGIN_SQL, newIp);

        AuthedPlayerFactory.save(this);

        return true;
    }

    public boolean authenticateSession(@Nonnull IpAddress ipAddress) {
        if (SessionHelper.hasValidSession(this, ipAddress)) {
            authenticate(AuthenticationProvider.XLOGIN_SESSION, ipAddress.getIp());

            return true;
        }

        return false;
    }

    private void authenticate(@Nonnull AuthenticationProvider provider, @Nonnull String currentIp) {
        setAuthenticationProvider(provider);
        setAuthenticated(true);
        setLastIp(currentIp);
        setLastLoginDate(new Timestamp(System.currentTimeMillis()));
        AuthedPlayerFactory.save(this); //refactor - too strong coupling of objects
    }

    /**
     * Gets the last location of this player for a given server.
     *
     * @param serverName the name of the server to get the location for
     * @return the location or NULL if no location was saved for given server
     */
    @Nullable
    public LocationInfo getLastLocation(String serverName) {
        Validate.notNull(serverName, "Cannot get location for null server name!");

        return LocationInfo.load(this, serverName);
    }

    public void setLastLocation(@Nonnull LocationInfo toSave) {
        toSave.save();
    }

    public void setLastLocation(String serverName, double x, double y, double z, String worldName) {
        Validate.notNull(serverName, "Cannot set null server name!");

        setLastLocation(new LocationInfo(this.getUniqueId(), serverName, worldName, x, y, z));
    }

    @Nonnull
    public String getUuid() {
        return this.uuid;
    }

    @Nonnull
    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Nullable
    @Override
    public String getLastIp() {
        return this.lastIp;
    }

    public void setLastIp(@Nullable String newIpString) {
        if (newIpString == null || !newIpString.equals(getLastIp())) {
            this.lastIp = newIpString;
            if (newIpString != null && IpAddressFactory.exists(newIpString)) {
                IpAddress oldIp = IpAddress.fromIpString(this.lastIp);
                IpAddress newIp = IpAddress.fromIpString(newIpString);
                newIp.adaptToProperties(oldIp); //TODO: Do we need the old IP here?
                IpAddressFactory.save(newIp);
            }
        }
    }

    public boolean isPremium() {
        return this.premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    /**
     * Returns a color representing this player's premium state. If their account is Mojang Premium, returns a green
     * color, otherwise a red color.
     *
     * @return a color representing this player's premium state
     */
    public ChatColor getPremiumColor() {
        return isPremium() ? ChatColor.GREEN : ChatColor.RED;
    }

    /**
     * Creates a hover event describing important data about this player.
     *
     * @return a hover event describing this player
     */
    public HoverEvent buildHoverInfo() {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new XyComponentBuilder("Spieler: ").color(ChatColor.GOLD)
                        .append(getName(), ChatColor.YELLOW)
                        .append("\nUUID:\n", ChatColor.GOLD) //takes up too much space in auto GUI scale
                        .append(getUuid(), ChatColor.YELLOW)
                        .append("\nPremium? ", ChatColor.GOLD)
                        .append(isPremium() ? "ja" : "nein", getPremiumColor())
                        .append("\nLetzte IP: ", ChatColor.GOLD)
                        .append(getLastIp(), ChatColor.YELLOW)
                        .append("\nLetzter Login: ", ChatColor.GOLD)
                        .append(ISO_DATE_FORMAT.format(lastLoginDate), ChatColor.YELLOW)
                        .create()
        );
    }

    public boolean isDisabledPremiumMessage() {
        return this.disabledPremiumMessage;
    }

    public void setDisabledPremiumMessage(boolean disabledPremiumMessage) {
        this.disabledPremiumMessage = disabledPremiumMessage;
    }

    public Timestamp getRegistrationTimestamp() {
        return this.registrationDate;
    }

    public boolean isSessionsEnabled() {
        return this.sessionsEnabled;
    }

    public void setSessionsEnabled(boolean sessionsEnabled) {
        this.sessionsEnabled = sessionsEnabled;
    }

    public AuthedPlayerRepository getRepository() {
        return repository;
    }

    public boolean isValid() {
        return this.valid;
    }

    public void setValid(boolean newValid) {
        setValid(newValid, false);
    }

    public void setValid(boolean newValid, boolean save) {
//        Validate.isTrue(isValid() || !newValid, "Cannot re-validate player!");

        if (isValid() == newValid) {
            return;
        }

        this.valid = newValid;

        if (save && isAuthenticated()) {
            AuthedPlayerFactory.save(this);
        }

        this.setAuthenticated(false); //If we change validation state, we need to drop the authentication
        this.setAuthenticationProvider(null); //to prevent cracked users from hijacking premium sessions.
    }

    @Nullable
    public AuthenticationProvider getAuthenticationProvider() {
        return this.authenticationProvider;
    }

    public void setAuthenticationProvider(@Nullable AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    } //TODO used by recv msg

    @Nonnull
    @Override
    public String getId() {
        return getUuid();
    }

    @Override
    public boolean isDemo() {
        return !isPremium();
    }

    public void registerPassword(@Nonnull String password, @Nonnull String ip) {
        String salt = PasswordHelper.generateSalt();

        setSalt(salt);
        setPassword(PasswordHelper.encrypt(password, salt));

        setValid(true, true);

        Validate.isTrue(authenticatePassword(password, ip), "Setting password failed for registration!");
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthedPlayer)) return false;
        final AuthedPlayer other = (AuthedPlayer) o;
        if (!other.canEqual(this)) return false;
        final Object this$uuid = this.getUuid();
        final Object other$uuid = other.getUuid();
        if (!this$uuid.equals(other$uuid)) return false;
        final Object this$authenticationProvider = this.getAuthenticationProvider();
        final Object other$authenticationProvider = other.getAuthenticationProvider();
        if (this$authenticationProvider == null ? other$authenticationProvider != null : !this$authenticationProvider.equals(other$authenticationProvider))
            return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $uuid = this.getUuid();
        result = result * PRIME + $uuid.hashCode();
        final Object $authenticationProvider = this.getAuthenticationProvider();
        result = result * PRIME + ($authenticationProvider == null ? 0 : $authenticationProvider.hashCode());
        return result;
    }

    public boolean canEqual(Object other) {
        return other instanceof AuthedPlayer;
    }

    @Nonnull
    public String toString() {
        return "AuthedPlayer(uuid=" + this.getUuid() +
                ", name=" + this.getName() + ", lastIp=" + this.getLastIp() + ", premium=" + this.isPremium() +
                ", disabledPremiumMessage=" + this.isDisabledPremiumMessage() + ", registrationDate=" + this.getRegistrationTimestamp() +
                ", sessionsEnabled=" + this.isSessionsEnabled() + ", valid=" + this.isValid() +
                ", authenticationProvider=" + this.getAuthenticationProvider() + ", authenticated=" + this.isAuthenticated() + ")";
    }

    @Nonnull
    @Override
    public String toShortString() {
        return "AuthedPlayer{uuid=" + this.getUuid() + ", name=" + this.getName() + "}";
    }

    @Nullable
    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(@Nullable Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public enum AuthenticationProvider {
        MINECRAFT_PREMIUM, XLOGIN_SQL, XLOGIN_SESSION
    }
}
