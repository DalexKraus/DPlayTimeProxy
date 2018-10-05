package at.dalex.api.playtime;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/*
 * Copyright 2018 David Kraus. All rights reserved.
 */
public class SaveScheduler implements Runnable {

    private HashMap<UUID, Integer> playTimes;

    private Configuration databaseConfig;
    private final String DATABASE_FILE = "players.db";
    private File databaseFile;

    /**
     * Creates a new SaveScheduler using the plugin's instance,
     * the list of players and their played time.
     *
     * @param pluginInstance The plugin's instance
     * @param playTimes The Player's played time in seconds
     */
    SaveScheduler(Main pluginInstance, HashMap<UUID, Integer> playTimes) {
        this.playTimes = playTimes;

        try {
            if (!pluginInstance.getDataFolder().exists()) {
                pluginInstance.getDataFolder().mkdir();
            }

            databaseFile = new File(pluginInstance.getDataFolder(), DATABASE_FILE);
            databaseConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(databaseFile);
        } catch (IOException e) {
            System.err.println(Main.prefix_noColor + "Error retrieving player database file!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        for (UUID playerId : playTimes.keySet()) {
            databaseConfig.set("players." + playerId.toString() + ".timePlayed", playTimes.get(playerId));
        }

        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.databaseConfig, this.databaseFile);
        } catch (IOException e) {
            System.err.println(Main.prefix_noColor + "Unable to save player database file!");
            e.printStackTrace();
        }
    }
}
