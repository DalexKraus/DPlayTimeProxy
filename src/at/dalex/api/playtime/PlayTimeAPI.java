package at.dalex.api.playtime;

import java.util.UUID;

/*
 * Copyright 2018 David Kraus. All rights reserved.
 */
public class PlayTimeAPI {

    private static Main pluginInstance;

    public PlayTimeAPI(Main instance) {
        pluginInstance = instance;
    }

    /**
     * Get the playtime of a specific player in seconds.
     *
     * A session is meant to be the interval between the
     * login an disconnect event of a player.
     *
     * @param playerId The {@link net.md_5.bungee.api.connection.ProxiedPlayer}'s id
     * @return The playtime in seconds
     */
    public static int getSessionPlayTime(UUID playerId) {
        return pluginInstance.calculateSessionPlayTime(playerId);
    }

    /**
     * Returns the total time in seconds a player
     * has played on this server.
     *
     * Returns -1 if the specified player has
     * never played on this server before.
     *
     * @param playerId The player's {@link UUID}
     */
    public static int getTotalPlayTime(UUID playerId) {
        return pluginInstance.getTimePlayed(playerId);
    }
}
