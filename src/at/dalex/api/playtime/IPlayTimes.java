package at.dalex.api.playtime;

import java.util.HashMap;
import java.util.UUID;

/**
 * This interface is marked as package-private.
 * It is used to store every player's played time.
 *
 * Copyright 2018 David Kraus. All rights reserved.
 */
interface IPlayTimes {

    /* Player's playtime, in seconds */
    HashMap<UUID, Integer> playerPlayTimes = new HashMap<>();
}
