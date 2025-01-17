package net.william278.huskhomes.api;

import de.themoep.minedown.adventure.MineDown;
import net.william278.huskhomes.HuskHomes;
import net.william278.huskhomes.config.Locales;
import net.william278.huskhomes.player.OnlineUser;
import net.william278.huskhomes.player.User;
import net.william278.huskhomes.player.UserData;
import net.william278.huskhomes.position.*;
import net.william278.huskhomes.teleport.Teleport;
import net.william278.huskhomes.teleport.TeleportBuilder;
import net.william278.huskhomes.random.RandomTeleportEngine;
import net.william278.huskhomes.teleport.TeleportResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * The base implementation of the HuskHomes API, containing cross-platform API calls.
 * </p>
 * This class should not be used directly, but rather through platform-specific extending API classes.
 */
@SuppressWarnings("unused")
public abstract class BaseHuskHomesAPI {

    /**
     * <b>(Internal use only)</b> - Instance of the implementing plugin.
     */
    protected final HuskHomes plugin;

    /**
     * <b>(Internal use only)</b> - Constructor, instantiating the base API class
     */
    protected BaseHuskHomesAPI(@NotNull HuskHomes plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns saved {@link UserData} for the given player's account {@link UUID}, if they exist.
     *
     * @param uuid The {@link UUID} of the user to get data for.
     * @return The {@link UserData} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<UserData>> getUserData(@NotNull UUID uuid) {
        return plugin.getDatabase().getUserData(uuid);
    }

    /**
     * Returns saved {@link UserData} for the given player's username (case-insensitive), if they exist.
     *
     * @param username The username of the user to get data for.
     * @return The {@link UserData} of the user.
     * @since 3.0
     */
    public final CompletableFuture<Optional<UserData>> getUserData(@NotNull String username) {
        return plugin.getDatabase().getUserDataByName(username);
    }

    /**
     * Returns the last position, as used in the {@code /back} command, for this user
     *
     * @param user The {@link User} to get the last position for
     * @return The user's last {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserLastPosition(@NotNull User user) {
        return plugin.getDatabase().getLastPosition(user);
    }

    /**
     * Returns where the user last disconnected from a server
     *
     * @param user The {@link User} to get the last disconnect position for
     * @return The user's offline {@link Position}, if there is one
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserOfflinePosition(@NotNull User user) {
        return plugin.getDatabase().getOfflinePosition(user);
    }

    /**
     * Returns where the user last set their spawn point by right-clicking a bed or respawn anchor
     * <p>
     * The optional returned by this method will be empty unless HuskHomes is running cross-server mode
     * and global respawning is enabled
     *
     * @param user The {@link User} to get the respawn position of
     * @return The user's respawn {@link Position} if they have one, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public CompletableFuture<Optional<Position>> getUserRespawnPosition(@NotNull User user) {
        if (!plugin.getSettings().crossServer || plugin.getSettings().globalRespawning) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return plugin.getDatabase().getRespawnPosition(user);
    }

    /**
     * Returns if a user is currently warming up for a timed teleport; that is, they are in the state where they
     * must stand still and not take damage for the set amount of seconds before a teleport can be executed.
     *
     * @param user The {@link User} to check
     * @return {@code true} if the user is currently warming up, otherwise {@code false}
     * @since 3.0.2
     */
    public boolean isUserWarmingUp(@NotNull User user) {
        return plugin.getCache().isWarmingUp(user.uuid);
    }

    /**
     * Save {@link UserData} to the database, updating it if data for the user already exists, or adding new user data if it doesn't.
     *
     * @param userData The {@link UserData} to save
     * @return A {@link CompletableFuture} that will complete when the data has been saved
     * @since 3.0
     */
    public final CompletableFuture<Void> saveUserData(@NotNull UserData userData) {
        return plugin.getDatabase().updateUserData(userData);
    }

    /**
     * Get a list of {@link Home}s set by the given user.
     *
     * @param user The {@link User} to get the homes of
     * @return A {@link CompletableFuture} that will complete with a list of {@link Home}s set by the user
     */
    public final CompletableFuture<List<Home>> getUserHomes(@NotNull User user) {
        return plugin.getDatabase().getHomes(user);
    }

    /**
     * Get a list of {@link Home}s set by the given user that they have made public
     *
     * @param user The {@link User} to get the public homes of
     * @return A {@link CompletableFuture} that will complete with a list of {@link Home}s set by the user that
     * they have made public (where {@link Home#isPublic})
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getUserPublicHomes(@NotNull User user) {
        return getUserHomes(user).thenApply(homes -> homes.stream()
                .filter(home -> home.isPublic)
                .collect(Collectors.toList()));
    }

    /**
     * Get a list of homes that have been made public ("public homes")
     *
     * @return A {@link CompletableFuture} that will complete with a list of public {@link Home}s
     * @since 3.0
     */
    public final CompletableFuture<List<Home>> getPublicHomes() {
        return plugin.getDatabase().getPublicHomes();
    }

    /**
     * Get a {@link Home} from the database owned by a given {@link User} with the specified name
     *
     * @param user     The {@link User} to get the home of
     * @param homeName The name of the home to get
     * @return A {@link CompletableFuture} that will complete with the {@link Home} if it exists, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public final CompletableFuture<Optional<Home>> getHome(@NotNull User user, @NotNull String homeName) {
        return plugin.getDatabase().getHome(user, homeName);
    }

    /**
     * Get a {@link Home} from the database by its' unique id
     *
     * @param homeUuid The {@link UUID} of the home to get
     * @return A {@link CompletableFuture} that will complete with the {@link Home} if it exists, otherwise an empty {@link Optional}
     */
    public final CompletableFuture<Optional<Home>> getHome(@NotNull UUID homeUuid) {
        return plugin.getDatabase().getHome(homeUuid);
    }

    /**
     * Get a list of {@link Warp}s
     *
     * @return A {@link CompletableFuture} that will complete with a list of {@link Warp}s
     * @since 3.0
     */
    public final CompletableFuture<List<Warp>> getWarps() {
        return plugin.getDatabase().getWarps();
    }

    /**
     * Get a {@link Warp} from the database with the specified name
     *
     * @param warpName The name of the warp to get
     * @return A {@link CompletableFuture} that will complete with the {@link Warp} if it exists, otherwise an empty {@link Optional}
     * @since 3.0
     */
    public final CompletableFuture<Optional<Warp>> getWarp(@NotNull String warpName) {
        return plugin.getDatabase().getWarp(warpName);
    }

    /**
     * Get a {@link Warp} from the database by its' unique id
     *
     * @param warpUuid The {@link UUID} of the warp to get
     * @return A {@link CompletableFuture} that will complete with the {@link Warp} if it exists, otherwise an empty {@link Optional}
     */
    public final CompletableFuture<Optional<Warp>> getWarp(@NotNull UUID warpUuid) {
        return plugin.getDatabase().getWarp(warpUuid);
    }

    /**
     * Get the canonical {@link Position} of the spawn point. Note that if cross-server and global spawn
     * are enabled in the config, this may not return a position on this server.
     *
     * @return A {@link CompletableFuture} that will complete with the {@link Position} of the spawn point
     */
    public final CompletableFuture<Optional<? extends Position>> getSpawn() {
        return plugin.getSpawn();
    }

    /**
     * Update the {@link PositionMeta} of a {@link Home}
     *
     * @param home    The {@link Home} to update
     * @param newMeta The new {@link PositionMeta} to set
     * @return A {@link CompletableFuture} that will complete when the {@link Home} has been updated with a
     * {@link SavedPositionManager.SaveResult} indicating if the update was successful
     * @since 3.0
     */
    public final CompletableFuture<SavedPositionManager.SaveResult> updateHomeMeta(@NotNull Home home,
                                                                                   @NotNull PositionMeta newMeta) {
        return plugin.getSavedPositionManager().updateHomeMeta(home, newMeta);
    }

    /**
     * Update the position of a {@link Home}, relocating it
     *
     * @param home        The {@link Home} to update
     * @param newPosition The new {@link Position} to relocate the home to
     * @return A {@link CompletableFuture} that will complete with a boolean indicating if the home was
     * updated ({@code true} if it was updated; otherwise, {@code false})
     * @since 3.0
     */
    public final CompletableFuture<Boolean> updateHomePosition(@NotNull Home home,
                                                               @NotNull Position newPosition) {
        return plugin.getSavedPositionManager().updateHomePosition(home, newPosition);
    }

    /**
     * Update the privacy of a {@link Home}
     *
     * @param home     The {@link Home} to update
     * @param isPublic Whether the home should be set to public or not
     * @return A {@link CompletableFuture} that will complete with a boolean indicating if the home's privacy
     * was updated ({@code true} if it was updated; otherwise, {@code false})
     * @since 3.0
     */
    public final CompletableFuture<Boolean> updateHomePrivacy(@NotNull Home home,
                                                              final boolean isPublic) {
        return plugin.getSavedPositionManager().updateHomePrivacy(home, isPublic);
    }

    /**
     * Update the {@link PositionMeta} of a {@link Warp}
     *
     * @param warp    The {@link Warp} to update
     * @param newMeta The new {@link PositionMeta} to set
     * @return A {@link CompletableFuture} that will complete when the {@link Warp} has been updated with a
     * {@link SavedPositionManager.SaveResult} indicating if the update was successful
     * @since 3.0
     */
    public final CompletableFuture<SavedPositionManager.SaveResult> updateWarpMeta(@NotNull Warp warp,
                                                                                   @NotNull PositionMeta newMeta) {
        return plugin.getSavedPositionManager().updateWarpMeta(warp, newMeta);
    }

    /**
     * Update the position of a {@link Warp}, relocating it
     *
     * @param warp        The {@link Warp} to update
     * @param newPosition The new {@link Position} to relocate the warp to
     * @return A {@link CompletableFuture} that will complete with a boolean indicating if the warp was
     * updated ({@code true} if it was updated; otherwise, {@code false})
     */
    public final CompletableFuture<Boolean> updateWarpPosition(@NotNull Warp warp,
                                                               @NotNull Position newPosition) {
        return plugin.getSavedPositionManager().updateWarpPosition(warp, newPosition);
    }

    /**
     * Get the maximum number of set homes a given {@link OnlineUser} can make
     *
     * @param user The {@link OnlineUser} to get the maximum number of homes for
     * @return The maximum number of homes the user can set
     * @since 3.0
     */
    public final int getMaxHomeSlots(@NotNull OnlineUser user) {
        return user.getMaxHomes(plugin.getSettings().maxHomes, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can set for free
     * <p>
     * This is irrelevant unless the server is using economy features with HuskHomes
     *
     * @param user The {@link OnlineUser} to get the number of free home slots for
     * @return The number of homes the user can set for free
     * @since 3.0
     */
    public final int getFreeHomeSlots(@NotNull OnlineUser user) {
        return user.getFreeHomes(plugin.getSettings().freeHomeSlots, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Get the number of homes an {@link OnlineUser} can make public
     *
     * @param user The {@link OnlineUser} to get the number of public home slots for
     * @return The number of homes the user can make public
     * @since 3.0
     */
    public final int getMaxPublicHomeSlots(@NotNull OnlineUser user) {
        return user.getMaxPublicHomes(plugin.getSettings().maxPublicHomes, plugin.getSettings().stackPermissionLimits);
    }

    /**
     * Get a {@link TeleportBuilder} to construct and dispatch a (timed) teleport
     *
     * @param onlineUser The {@link OnlineUser} to teleport
     * @return A {@link TeleportBuilder} to construct and dispatch a (timed) teleport
     * @since 3.1
     */
    @NotNull
    public final TeleportBuilder teleportBuilder(@NotNull OnlineUser onlineUser) {
        return Teleport.builder(plugin, onlineUser);
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a target {@link Position}
     *
     * @param user          The {@link OnlineUser} to teleport
     * @param position      The {@link Position} to teleport the user to
     * @param timedTeleport Whether the teleport should be timed or not (requiring a warmup where they must stand still
     *                      for a period of time)
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     * @deprecated Use {@link #teleportBuilder(OnlineUser)} to construct a teleport
     */
    @Deprecated(since = "3.1")
    public final CompletableFuture<TeleportResult> teleportPlayer(@NotNull OnlineUser user,
                                                                  @NotNull Position position,
                                                                  final boolean timedTeleport) {
        final TeleportBuilder builder = teleportBuilder(user).setTarget(position);
        return timedTeleport
                ? builder.toTimedTeleport().thenApplyAsync(teleport -> teleport.execute().join().getState())
                : builder.toTeleport().thenApplyAsync(teleport -> teleport.execute().join().getState());
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a target {@link Position}
     *
     * @param user     The {@link OnlineUser} to teleport
     * @param position The {@link Position} to teleport the user to
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     * @deprecated Use {@link #teleportBuilder(OnlineUser)} to construct a teleport
     */
    @Deprecated(since = "3.1")
    public final CompletableFuture<TeleportResult> teleportPlayer(@NotNull OnlineUser user, @NotNull Position position) {
        return teleportPlayer(user, position, false);
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}
     *
     * @param user          The {@link OnlineUser} to teleport
     * @param timedTeleport Whether the teleport should be timed or not (requiring a warmup where they must stand still
     *                      for a period of time)
     * @param rtpArgs       Arguments that will be passed to the implementing {@link RandomTeleportEngine}
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> randomlyTeleportPlayer(@NotNull OnlineUser user,
                                                                          final boolean timedTeleport,
                                                                          @NotNull String... rtpArgs) {
        return CompletableFuture.supplyAsync(() -> plugin.getRandomTeleportEngine()
                .getRandomPosition(user.getPosition().world, rtpArgs)
                .thenApply(position -> {
                    if (position.isPresent()) {
                        final TeleportBuilder builder = Teleport.builder(plugin, user)
                                .setTarget(position.get());

                        return timedTeleport
                                ? builder.toTimedTeleport()
                                .thenApplyAsync(teleport -> teleport.execute().join().getState()).join()
                                : builder.toTeleport()
                                .thenApplyAsync(teleport -> teleport.execute().join().getState()).join();
                    } else {
                        return TeleportResult.CANCELLED;
                    }
                }).join());
    }

    /**
     * Attempt to teleport an {@link OnlineUser} to a randomly generated {@link Position}. The {@link Position} will be
     * generated by the current {@link RandomTeleportEngine}
     *
     * @param user The {@link OnlineUser} to teleport
     * @return A {@link CompletableFuture} that will complete with a {@link TeleportResult} indicating the result of
     * completing the teleport. If the teleport was successful, the {@link TeleportResult#successful} will be {@code true}.
     * @since 3.0
     */
    public final CompletableFuture<TeleportResult> randomlyTeleportPlayer(@NotNull OnlineUser user) {
        return randomlyTeleportPlayer(user, false);
    }

    /**
     * Set the {@link RandomTeleportEngine} to use for processing random teleports. The engine will be used to process all
     * random teleports, including both {@code /rtp} command executions and API ({@link #randomlyTeleportPlayer(OnlineUser)})
     * calls.
     *
     * @param randomTeleportEngine the {@link RandomTeleportEngine} to use to process random teleports
     * @see RandomTeleportEngine
     * @since 3.0
     */
    public final void setRandomTeleportEngine(@NotNull RandomTeleportEngine randomTeleportEngine) {
        plugin.setRandomTeleportEngine(randomTeleportEngine);
    }

    /**
     * Get a {@link MineDown}-formatted locale by key from the plugin {@link Locales} file
     *
     * @param localeKey    The key of the locale to get
     * @param replacements Replacement strings to apply to the locale
     * @return The {@link MineDown}-formatted locale
     * @apiNote Since v3.0.4, this method returns a `adventure.MineDown` object, targeting
     * <a href="https://docs.adventure.kyori.net/">Adventure</a> platforms, rather than a bungee components object.
     * @since 3.0.4
     */
    public final Optional<MineDown> getLocale(@NotNull String localeKey, @NotNull String... replacements) {
        return plugin.getLocales().getLocale(localeKey, replacements);
    }

    /**
     * Get a raw locale string by key from the plugin {@link Locales} file
     *
     * @param localeKey    The key of the locale to get
     * @param replacements Replacement strings to apply to the locale
     * @return The raw locale string
     * @since 3.0
     */
    public final Optional<String> getRawLocale(@NotNull String localeKey, @NotNull String... replacements) {
        return plugin.getLocales().getRawLocale(localeKey, replacements);
    }

}
