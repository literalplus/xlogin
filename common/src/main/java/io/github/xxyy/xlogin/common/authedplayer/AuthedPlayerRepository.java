/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.authedplayer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.xxyy.common.collections.CaseInsensitiveMap;
import io.github.xxyy.common.lib.com.mojang.api.profiles.Profile;
import io.github.xxyy.common.lib.net.minecraft.server.UtilUUID;
import io.github.xxyy.common.shared.uuid.UUIDRepository;
import io.github.xxyy.common.sql.QueryResult;
import io.github.xxyy.lib.intellij_annotations.NotNull;
import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.common.PreferencesHolder;
import io.github.xxyy.xlogin.common.api.XLoginRepository;
import org.bukkit.plugin.ServicePriority;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the creation of {@link AuthedPlayer}s.
 * Either creates them newly or gets them from database.
 * New players are written to database immediately.
 * <p>
 * <b>Warning: This class is not particularly thread-safe and will overwrite any remote changes with local data
 * in various cases. Proceed with care.</b>
 * </p>
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 15.5.14
 */
@SuppressWarnings("UnusedDeclaration") //API declarations are not used by xLogin internally
public class AuthedPlayerRepository implements XLoginRepository {
    @NotNull
    private Map<UUID, Boolean> knownPlayers = new ConcurrentHashMap<>();
    @NotNull
    private Map<String, List<AuthedPlayer>> nameProfilesCache =
            Collections.synchronizedMap(new CaseInsensitiveMap<List<AuthedPlayer>>());
    @NotNull
    private Map<UUID, AuthedPlayer> idProfileCache = new ConcurrentHashMap<>();
    @Nullable
    private UUIDRepository parentUUIDRepo = EmptyUUIDRepository.INSTANCE;
    private final boolean readOnly;

    /**
     * Constructs a new repository. If read-only, no data will be written back to database which has been retrieved from
     * here. This is mainly intended to unwanted async modifications.
     *
     * @param readOnly whether this repository's data is read-only
     */
    public AuthedPlayerRepository(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Checks if this repository has been marked as read-only. Read-only repositories will not allow any data retrieved
     * from them to be written back to database. This is a security measure which ensures that if you only want to retrieve data,
     * you won't accidentally write anything, thus possibly creating undefined behaviour because full-access repositories
     * expect data in the database not to change. Additionally, you might write back some outdated data which has already
     * been updated by full-access repositories. Respecting this setting is one of the Three Laws statically programmed
     * into every single {@link AuthedPlayerFactory}.
     *
     * @return whether this repository is read-only.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    //////////////////////// XLOGIN REPO API METHODS ///////////////////////////////////////////////////////////////////////

    @Override
    public boolean isPlayerKnown(@NotNull UUID uuid) {
        if (knownPlayers.containsKey(uuid)) {
            return knownPlayers.get(uuid);
        }
        boolean rtrn;

        try (QueryResult qr = PreferencesHolder.getSql().executeQueryWithResult("SELECT COUNT(*) FROM " + AuthedPlayer.AUTH_DATA_TABLE_NAME +
                " WHERE (premium = 1 OR password IS NOT NULL) AND uuid=?", uuid.toString()).assertHasResultSet()) {
            rtrn = qr.rs().next() && qr.rs().getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.knownPlayers.put(uuid, rtrn);

        return rtrn;
    }


    @Override
    @NotNull
    public List<AuthedPlayer> getProfilesByName(@NotNull String name) {
        List<AuthedPlayer> result = nameProfilesCache.get(name);

        if (result == null) {
            result = AuthedPlayerFactory.getProfilesByName(name, this);


            if (result.size() == 1) {
                updateProfile(result.get(0));
            } else {
                nameProfilesCache.put(name, result);
            }
        }

        return result;
    }

    @Override
    @NotNull
    public List<AuthedPlayer> getProfiles(@NotNull String input) {
        List<AuthedPlayer> result = nameProfilesCache.get(input);

        if (result == null) {
            if (UtilUUID.isValidUUID(input)) {
                //Since the check method checks for Mojang-style UUIDs too, we need to treat those as valid.
                result = Collections.singletonList(getProfile(UtilUUID.getFromString(input)));
                nameProfilesCache.put(input, result);
            } else {
                result = getProfilesByName(input);
            }
        }

        return result;
    }

    @Override
    @Nullable
    public AuthedPlayer getProfile(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        AuthedPlayer result = idProfileCache.get(uuid);

        if (result == null) {
            result = overrideProfile(uuid);
        }

        return result;
    }

    @Override
    public void refreshProfile(@NotNull UUID uuid) {
        AuthedPlayer profile = overrideProfile(uuid);

        if (profile == null) {
            this.idProfileCache.remove(uuid);
        } else {
            this.updateProfile(profile);
        }
    }

///////////////////////////// INTERNAL AND PRIVATE UTILITY METHODS /////////////////////////////////////////////////////

    /**
     * Fetches a player from database or creates it if there is no such player.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPlayer instance corresponding to the arguments
     */
    @Nullable
    public AuthedPlayer getProfile(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer authedPlayer = idProfileCache.get(uuid);

        if (authedPlayer == null) {
            authedPlayer = AuthedPlayerFactory.get(uuid, name, this);

            if (!authedPlayer.getName().equals(name)) {
                authedPlayer.setName(name);
//                AuthedPlayerFactory.save(authedPlayer);
            }
        }

        return authedPlayer;
    }

    /**
     * Refreshes the information about a player.
     *
     * @param uuid UUID of the player to get
     * @param name Name of the player to get
     * @return An AuthedPlayer instance corresponding to the arguments
     */
    @Nullable
    public AuthedPlayer refreshPlayer(@NotNull UUID uuid, @NotNull String name) {
        AuthedPlayer oldPlayer = idProfileCache.get(uuid);

        if (oldPlayer != null) {
            oldPlayer.setValid(false, false);
            forgetProfile(oldPlayer);
        }

        AuthedPlayer refreshedPlayer = AuthedPlayerFactory.get(uuid, name, this);

        updateProfile(refreshedPlayer);

        return refreshedPlayer;
    }

    //Gets a profile and overrides cache, if existent.
    @Nullable
    private AuthedPlayer overrideProfile(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        AuthedPlayer result = AuthedPlayerFactory.getProfile(uuid, this);
        if (result != null) {
            idProfileCache.put(uuid, result);
        }

        return result;
    }

    /**
     * Entirely deletes a player from the underlying database and local cache.
     *
     * @param ap Player to delete
     */
    public void deletePlayer(@NotNull AuthedPlayer ap) {
        AuthedPlayerFactory.delete(ap);
        forget(ap.getUniqueId());
    }

    /**
     * Checks if a profile matching given UUID is currently cached.
     *
     * @param uuid the uuid to find
     * @return true if a profile matching given UUID is cached
     */
    public boolean hasCached(@NotNull UUID uuid) {
        return idProfileCache.containsKey(uuid);
    }

    /**
     * Clears this repo's cache.
     */
    public void clear() {
        this.knownPlayers.clear();
        this.idProfileCache.clear();
        this.nameProfilesCache.clear();
    }

    public void forget(UUID uuid) {
        AuthedPlayer authedPlayer = this.idProfileCache.get(uuid);

        if (authedPlayer == null) {
            this.knownPlayers.remove(uuid);
        } else {
            forgetProfile(authedPlayer);
        }
    }

    public void forgetProfile(@NotNull AuthedPlayer profile) {
        this.knownPlayers.remove(profile.getUniqueId());
        this.idProfileCache.remove(profile.getUniqueId());
        this.nameProfilesCache.remove(profile.getName());
    }

    public void updateProfile(@NotNull AuthedPlayer profile) {
        this.knownPlayers.put(profile.getUniqueId(), true);
        this.idProfileCache.put(profile.getUniqueId(), profile);
        //If we have a casing of the name, keep that to prevent inconsistencies
        this.nameProfilesCache.put(profile.getName(), Lists.newArrayList(profile));
    }

//////////////////////////////////////// XYC UUID API PROVIDER METHODS /////////////////////////////////////////////////

    @Nullable
    @Override
    public UUID forName(@NotNull String name) {
        List<AuthedPlayer> profiles = getProfilesByName(name);

        if (profiles.size() == 0) {
            return getParent().forName(name);
        } else {
            return profiles.get(0).getUniqueId();
        }
    }

    @NotNull
    @Override
    public UUID forNameChecked(@NotNull String name) throws UnknownKeyException, InvalidResultException {
        List<AuthedPlayer> profiles = getProfilesByName(name);

        if (profiles.size() == 0) {
            return getParent().forNameChecked(name);
        } else if (profiles.size() == 1) {
            return profiles.get(0).getUniqueId();
        } else {
            throw new InvalidResultException(profiles.toArray(new Profile[profiles.size()]));
        }
    }

    @Nullable
    @Override
    public String getName(@NotNull UUID uuid) {
        AuthedPlayer profile = getProfile(uuid);
        return profile == null ? null : profile.getName();
    }

    @NotNull
    @Override
    public UUIDRepository getParent() {
        //noinspection ConstantConditions
        return parentUUIDRepo; //this won't ever be null, I promise
    }

    @Override
    public void setParent(@Nullable UUIDRepository newParent) {
        if (newParent == null) {
            parentUUIDRepo = EmptyUUIDRepository.INSTANCE;
        } else {
            parentUUIDRepo = newParent;
        }
    }

    @NotNull
    @Override
    public ServicePriority getPriority() {
        return ServicePriority.High;
    }
}
