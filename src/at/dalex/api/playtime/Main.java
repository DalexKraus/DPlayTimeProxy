package at.dalex.api.playtime;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*
 * Copyright 2018 David Kraus. All rights reserved.
 */
public class Main extends Plugin implements Listener, IPlayTimes {

    private Main instance;
    public static String prefix_noColor = "[DPlayTimeAPI] ";
    private static String prefix = "§8[§bPlayTime§8]§7: ";

    /*
     * Login and disconnect times, stored in milliseconds
     * according to the system's time
     */
    private HashMap<UUID, Long> playerLoginTimes = new HashMap<>();
    private HashMap<UUID, Long> playerDisconnectTimes = new HashMap<>();

    @Override
    public void onEnable() {
        this.instance = this;

        new PluginMessager(this);

        getProxy().getPluginManager().registerListener(this, this);

        //Load player database
        getProxy().getScheduler().schedule(this, this::runLoadThread, 0, TimeUnit.SECONDS);
        //Start save job
        getProxy().getScheduler().schedule(this, this::runSaveThread, 30, Long.MAX_VALUE, TimeUnit.MINUTES);

        getProxy().getConsole().sendMessage(prefix + "§aPlugin geladen.");
    }

    @Override
    public void onDisable() {
        //Save
        getProxy().getConsole().sendMessage(prefix + "§7Speichere Spieler ...");
        runSaveThread();
        getProxy().getConsole().sendMessage(prefix + "§8Fertig.");
        getProxy().getConsole().sendMessage(prefix + "§4Plugin deaktiviert.");
    }

    /**
     * Saves all players and their played time
     * in a configuration file using the SaveScheduler.
     *
     * This allows us to run the job asynchronously.
     */
    private void runSaveThread() {
        Thread saveThread = new Thread(new SaveScheduler(instance));
        saveThread.start();
    }

    /**
     * Loads all players and their played time
     * from a configuration file using the LoadScheduler.
     *
     * This allows us to run the job asynchronously.
     */
    private void runLoadThread() {
        Thread loadThread = new Thread(new LoadScheduler(instance));
        loadThread.start();
    }

    /**
     * Calculates the time a player has played since the last
     * successful login.
     *
     * Returns -1 if the player has not logged in yet.
     *
     * @param playerId The player's {@link UUID}
     */
    public int calculateSessionPlayTime(UUID playerId) {
        //Check if the specified player has logged in in this session
        if (playerLoginTimes.get(playerId) != null) {
            long postLoginTime = playerLoginTimes.get(playerId);

            long lastKnownTime;
            if (playerDisconnectTimes.get(playerId) == null) {
                lastKnownTime = System.currentTimeMillis();
            }
            else lastKnownTime = playerDisconnectTimes.get(playerId);

            //Calculate session time
            long deltaTime = lastKnownTime - postLoginTime;
            /*
             * Switch from milliseconds to seconds
             * and store the result in the list
             */
            deltaTime /= 1000L;
            return (int) deltaTime;
        }
        else return -1;
    }

    /**
     * Updates the total time in seconds a player
     * has played on this server.
     *
     * This method should be called if a player disconnects.
     *
     * @param playerId The players's {@link UUID}
     */
    private void updateTotalPlayTime(UUID playerId) {
        int sessionPlayTime = calculateSessionPlayTime(playerId);
        Integer totalPlayTime = playerPlayTimes.get(playerId);

        if (totalPlayTime == null)
            totalPlayTime = 0;

        totalPlayTime += sessionPlayTime;

        //Update with new value
        playerPlayTimes.put(playerId, totalPlayTime);
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
    public int getTimePlayed(UUID playerId) {
        Integer timePlayed = playerPlayTimes.get(playerId);

        if (timePlayed == null)
            timePlayed = calculateSessionPlayTime(playerId);

        //If the player hasn't disconnected yet
        //add the session time to the player's
        //total playtime
        else if (playerDisconnectTimes.get(playerId) == null)
            timePlayed += calculateSessionPlayTime(playerId);

        return timePlayed;
    }

    /*
     * Login and disconnect events
     */

    @EventHandler
    public void onPostLogin(PostLoginEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        playerLoginTimes.put(playerId, System.currentTimeMillis());
        playerDisconnectTimes.remove(playerId);
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        playerDisconnectTimes.put(playerId, System.currentTimeMillis());
        updateTotalPlayTime(playerId);
        playerLoginTimes.remove(playerId);
    }
}
