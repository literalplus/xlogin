package io.github.xxyy.xlogin.common.authedplayer;

import io.github.xxyy.common.util.encryption.PasswordHelper;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.ips.FailedLoginAttempt;
import io.github.xxyy.xlogin.common.ips.IpAddress;
import io.github.xxyy.xlogin.common.ips.SessionHelper;
import io.github.xxyy.xlogin.common.sql.EbeanManager;
import lombok.*;
import org.apache.commons.lang3.Validate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a player that is authenticated with xLogin.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 11.5.14
 */
@EqualsAndHashCode(of = {"uuid", "authenticationProvider"})
@ToString
@Data
@NoArgsConstructor
@Entity
@Table(name = AuthedPlayer.AUTH_DATA_TABLE_NAME)
//TODO common interface w/ PlayerWrapper
public class AuthedPlayer {
    public static final String AUTH_DATA_TABLE_NAME = "mt_main.user";

    @NonNull
    @Id
    @Column(unique = true, nullable = false, length = 36)
    @Setter
    private String uuid;
    @NonNull
    @Column(unique = true, nullable = false, length = 16, name = "username")
    private String name;
    @Setter
    private String password; //Yes, encrypted...
    @Setter
    private String salt;
    @Column(name = "user_lastip", length = 50)
    private String lastIp;
    @OneToMany(targetEntity = IpAddress.class, fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private List<IpAddress> ips;
    @OneToMany(targetEntity = FailedLoginAttempt.class, fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private List<FailedLoginAttempt> loginAttempts;
    //Whether the user has OPTED IN to be considered as premium. This does not mean that they don't own a premium account.
    @Setter
    private boolean premium;
    @Column(name = "ign_p_msg")
    private boolean disabledPremiumMessage;
    @Column(name = "reg_date")
    private Timestamp registrationDate;
    @Column(name = "x")
    private int lastLogoutBlockX;
    @Column(name = "y")
    private int lastLogoutBlockY;
    @Column(name = "z")
    private int lastLogoutBlockZ;
    @Column(name = "world")
    private String lastWorldName;
    @Column(name = "sessions_enabled")
    private boolean sessionsEnabled;

    @Transient
    private boolean valid = true;
    @Transient
    private AuthenticationProvider authenticationProvider = null;
    @Setter(AccessLevel.PRIVATE)
    private boolean authenticated = false;

    public void addIp(IpAddress ipAddress) {
        getIps().add(ipAddress);

        if (ipAddress.getUser() != this) {
            ipAddress.setUser(this);
        }
    }

    public void setValid(boolean newValid) {
        Validate.isTrue(isValid() || !newValid, "Cannot re-validate player!");

        if (isValid() == newValid) {
            return;
        }

        this.valid = newValid;

        this.setAuthenticated(false); //If we change validation state, we need to drop the authentication
        this.setAuthenticationProvider(null); //to prevent cracked users from hijacking premium sessions.
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

            FailedLoginAttempt loginAttempt = new FailedLoginAttempt();
            loginAttempt.setIp(newIp);
            loginAttempt.setUser(this);
            loginAttempt.setTimestamp(new Timestamp(System.currentTimeMillis()));
            EbeanManager.getEbean().save(loginAttempt);

            return false;
        }

        setLastIp(newIp);

        this.authenticationProvider = AuthenticationProvider.XLOGIN_SQL;
        this.authenticated = true;

        EbeanManager.getEbean().save(this);

        return true;
    }

    public boolean authenticateSession() {
        if(SessionHelper.hasValidSession(this)) {
            this.authenticationProvider = AuthenticationProvider.XLOGIN_SESSION;
            this.authenticated = true;

            return true;
        }

        return false;
    }

    public void setLastIp(String newIp) {
        if (!getLastIp().equals(newIp)) {
            this.lastIp = newIp;
        }

        IpAddress existingIp = null;
        for (IpAddress ipAddress : getIps()) {
            if (ipAddress.getIp().equals(newIp)) {
                existingIp = ipAddress;
                break;
            }
        }

        if (existingIp == null) {
            existingIp = new IpAddress();
            existingIp.setIp(newIp);
            existingIp.setMaxUsers(PreferencesHolder.getMaxUsersPerIp());
            this.addIp(existingIp);
        }

        existingIp.setLastUsed(new Timestamp(System.currentTimeMillis()));
    }

    public IpAddress getLastIpObject() {
        IpAddress existingIp = null;
        for (IpAddress ipAddress : getIps()) {
            if (ipAddress.getIp().equals(getLastIp())) {
                existingIp = ipAddress;
                break;
            }
        }

        if(existingIp == null) {
            setLastIp(getLastIp());
            existingIp = getLastIpObject();
        }

        return existingIp;
    }

    public enum AuthenticationProvider {
        MINECRAFT_PREMIUM, XLOGIN_SQL, XLOGIN_SESSION
    }
}
