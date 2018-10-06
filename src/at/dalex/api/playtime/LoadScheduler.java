package at.dalex.api.playtime;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/*
 * Copyright 2018 David Kraus. All rights reserved.
 */
public class LoadScheduler implements Runnable, IPlayTimes {

    private Configuration databaseConfig;
    private final String DATABASE_FILE = "players.db";

    /**
     * Creates a new LoadScheduler using the plugin's instance,
     * the list of players and their played time.
     *
     * @param pluginInstance The plugin's instance
     */
    LoadScheduler(Main pluginInstance) {
        try {
            if (!pluginInstance.getDataFolder().exists()) {
                pluginInstance.getDataFolder().mkdir();
            }

            File databaseFile = new File(pluginInstance.getDataFolder(), DATABASE_FILE);
            if (databaseFile.exists()) {
                databaseConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(databaseFile);
            }
        } catch (IOException e) {
            System.err.println(Main.prefix_noColor + "Error retrieving player database file!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //Check if database has been loaded
        if (databaseConfig != null) {
            for (String playerId : databaseConfig.getSection("players").getKeys()) {
                playerPlayTimes.put(UUID.fromString(playerId), databaseConfig.getInt("players." + playerId + ".timePlayed"));
            }
        }
    }
}
