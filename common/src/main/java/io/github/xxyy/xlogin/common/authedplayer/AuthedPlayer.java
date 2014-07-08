package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.common.util.ToShortStringable;
import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.common.api.XLoginProfile;
import io.github.xxyy.xlogin.common.ips.IpAddress;
import io.github.xxyy.xlogin.common.ips.IpAddressFactory;
import io.github.xxyy.xlogin.common.ips.SessionHelper;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Represents a player that is authenticated with xLogin.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
//@Entity
//@Table(name = AuthedPlayer.AUTH_DATA_TABLE_NAME)
//TODO common interface w/ PlayerWrapper
public final class AuthedPlayer implements ToShortStringable, XLoginProfile {
    public static final String AUTH_DATA_TABLE_NAME = "mt_main.xlogin_data";

    private String uuid;
    private UUID uniqueId;

    private String name;
    private boolean premium;
    private boolean disabledPremiumMessage;

    private String password; //Yes, encrypted...
    private String salt;
    private String lastIp;
    private Timestamp registrationDate;
    private int lastLogoutBlockX;
    private int lastLogoutBlockY;
    private int lastLogoutBlockZ;
    private String lastWorldName;
    private boolean sessionsEnabled;

    private boolean valid = true;
    private AuthenticationProvider authenticationProvider = null;
    private boolean authenticated = false;

    protected AuthedPlayer(String uuid, String name, String password, String salt, String lastIp,
                        boolean premium, boolean disabledPremiumMessage, Timestamp registrationDate,
                        int lastLogoutBlockX, int lastLogoutBlockY, int lastLogoutBlockZ, String lastWorldName,
                        boolean sessionsEnabled) {
        this.uuid = uuid;
        this.uniqueId = UUID.fromString(uuid);
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.lastIp = lastIp;
        this.premium = premium;
        this.disabledPremiumMessage = disabledPremiumMessage;
        this.registrationDate = registrationDate;
        this.lastLogoutBlockX = lastLogoutBlockX;
        this.lastLogoutBlockY = lastLogoutBlockY;
        this.lastLogoutBlockZ = lastLogoutBlockZ;
        this.lastWorldName = lastWorldName;
        this.sessionsEnabled = sessionsEnabled;
    }

    public void setValid(boolean newValid) {
//        Validate.isTrue(isValid() || !newValid, "Cannot re-validate player!");

        if (isValid() == newValid) {
            return;
        }

        this.valid = newValid;

        this.setAuthenticated(false); //If we change validation state, we need to drop the authentication
        this.setAuthenticationProvider(null); //to prevent cracked users from hijacking premium sessions.

        AuthedPlayerFactory.save(this);
    }

    /**
     * Registers that this user has a Minecraft Premium account and is logged in.
     *
     * @return true if the user has opted in to use premium authentication and was logged in. False otherwise.
     */
    public boolean authenticatePremium() {
        if (this.isPremium()) {
            this.setAuthenticated(true);
            this.authenticationProvider = AuthenticationProvider.MINECRAFT_PREMIUM;
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
    public boolean authenticatePassword(String password, String newIp) {
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

        setLastIp(newIp);

        this.authenticationProvider = AuthenticationProvider.XLOGIN_SQL;
        this.authenticated = true;

        AuthedPlayerFactory.save(this);

        return true;
    }

    public boolean authenticateSession(IpAddress ipAddress) {
        if(SessionHelper.hasValidSession(this, ipAddress)) {
            this.authenticationProvider = AuthenticationProvider.XLOGIN_SESSION;
            this.authenticated = true;

            return true;
        }

        return false;
    }

    public void setLastIp(String newIpString) {
        if (newIpString == null || !newIpString.equals(getLastIp())) {
            this.lastIp = newIpString;
            if (IpAddressFactory.exists(newIpString)) {
                IpAddress oldIp = IpAddress.fromIpString(this.lastIp);
                IpAddress newIp = IpAddress.fromIpString(newIpString);
                newIp.adaptToProperties(oldIp);
                IpAddressFactory.save(newIp);
            }
        }
    }

    @NotNull
    public String getUuid() {
        return this.uuid;
    }

    @NotNull @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @NotNull @Override
    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public String getSalt() {
        return this.salt;
    }

    @Override
    public String getLastIp() {
        return this.lastIp;
    }

    public boolean isPremium() {
        return this.premium;
    }

    public boolean isDisabledPremiumMessage() {
        return this.disabledPremiumMessage;
    }

    public Timestamp getRegistrationTimestamp() {
        return this.registrationDate;
    }

    public int getLastLogoutBlockX() {
        return this.lastLogoutBlockX;
    }

    public int getLastLogoutBlockY() {
        return this.lastLogoutBlockY;
    }

    public int getLastLogoutBlockZ() {
        return this.lastLogoutBlockZ;
    }

    public String getLastWorldName() {
        return this.lastWorldName;
    }

    public boolean isSessionsEnabled() {
        return this.sessionsEnabled;
    }

    public boolean isValid() {
        return this.valid;
    }

    public AuthenticationProvider getAuthenticationProvider() {
        return this.authenticationProvider;
    }

    public boolean isAuthenticated() {
        return this.authenticated;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void registerPassword(String password, String ip) {
        String salt = PasswordHelper.generateSalt();

        setSalt(salt);
        setPassword(PasswordHelper.encrypt(password, salt));

        setValid(true);

        Validate.isTrue(authenticatePassword(password, ip), "Setting password failed for registration!");
    }

    public void setDisabledPremiumMessage(boolean disabledPremiumMessage) {
        this.disabledPremiumMessage = disabledPremiumMessage;
    }

    public void setLastLogoutBlockX(int lastLogoutBlockX) {
        this.lastLogoutBlockX = lastLogoutBlockX;
    }

    public void setLastLogoutBlockY(int lastLogoutBlockY) {
        this.lastLogoutBlockY = lastLogoutBlockY;
    }

    public void setLastLogoutBlockZ(int lastLogoutBlockZ) {
        this.lastLogoutBlockZ = lastLogoutBlockZ;
    }

    public void setLastWorldName(String lastWorldName) {
        this.lastWorldName = lastWorldName;
    }

    public void setSessionsEnabled(boolean sessionsEnabled) {
        this.sessionsEnabled = sessionsEnabled;
    }

    public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof AuthedPlayer)) return false;
        final AuthedPlayer other = (AuthedPlayer) o;
        if (!other.canEqual(this)) return false;
        final Object this$uuid = this.getUuid();
        final Object other$uuid = other.getUuid();
        if (this$uuid == null ? other$uuid != null : !this$uuid.equals(other$uuid)) return false;
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
        result = result * PRIME + ($uuid == null ? 0 : $uuid.hashCode());
        final Object $authenticationProvider = this.getAuthenticationProvider();
        result = result * PRIME + ($authenticationProvider == null ? 0 : $authenticationProvider.hashCode());
        return result;
    }

    public boolean canEqual(Object other) {
        return other instanceof AuthedPlayer;
    }

    public String toString() {
        return "AuthedPlayer(uuid=" + this.getUuid() +
                ", name=" + this.getName() + ", lastIp=" + this.getLastIp() + ", premium=" + this.isPremium() +
                ", disabledPremiumMessage=" + this.isDisabledPremiumMessage() + ", registrationDate=" + this.getRegistrationTimestamp() +
                ", lastLogoutBlockX=" + this.getLastLogoutBlockX() + ", lastLogoutBlockY=" + this.getLastLogoutBlockY() +
                ", lastLogoutBlockZ=" + this.getLastLogoutBlockZ() + ", lastWorldName=" + this.getLastWorldName() +
                ", sessionsEnabled=" + this.isSessionsEnabled() + ", valid=" + this.isValid() +
                ", authenticationProvider=" + this.getAuthenticationProvider() + ", authenticated=" + this.isAuthenticated() + ")";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    } //TODO used by recv msg

    @Override
    public String toShortString() {
        return "AuthedPlayer{uuid="+this.getUuid()+", name="+this.getName()+"}";
    }

    public enum AuthenticationProvider {
        MINECRAFT_PREMIUM, XLOGIN_SQL, XLOGIN_SESSION
    }
}
