package at.dalex.api.playtime;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.UUID;

public class PluginMessager implements Listener {

    private final String PLAYTIME_REQUEST_CHANNEL   = "pt_request_channel";
    private final String PLAYTIME_RESPONSE_CHANNEL  = "pt_response_channel";
    public static final String PLAYTIME_GET_TOTAL_TIME = "get_total_playtime";
    public static final String PLAYTIME_GET_SESSION_TIME = "get_session_playtime";

    private Main pluginInstance;

    public PluginMessager(Main pluginInstance) {
        this.pluginInstance = pluginInstance;
        BungeeCord.getInstance().registerChannel(PLAYTIME_REQUEST_CHANNEL);
        BungeeCord.getInstance().registerChannel(PLAYTIME_RESPONSE_CHANNEL);
        BungeeCord.getInstance().getPluginManager().registerListener(pluginInstance, this);
    }

    public void sendTimePlayedToSubserver(String channel, String message, ServerInfo server) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);

        try {
            outputStream.writeUTF(channel);
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.sendData(PLAYTIME_RESPONSE_CHANNEL, byteArrayOutputStream.toByteArray());
    }

    @EventHandler
    public void onPluginMessageReceived(PluginMessageEvent e) {
        if (e.getTag().equals(PLAYTIME_REQUEST_CHANNEL)) {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(e.getData()));
            String channel = null;

            try {
                channel = inputStream.readUTF();
                String message = inputStream.readUTF();
                UUID playerId = UUID.fromString(message);
                if (channel.equals(PLAYTIME_GET_TOTAL_TIME)) {
                    //Create time string
                    String timePlayed = "" + pluginInstance.getTimePlayed(playerId);

                    //Send PluginMessage to all sub-servers
                    sendStringToAllServers(PLAYTIME_GET_TOTAL_TIME, playerId.toString() + ";" + timePlayed);
                }
                else if (channel.equals(PLAYTIME_GET_SESSION_TIME)) {
                    //Create time string
                    String timePlayed = "" + pluginInstance.calculateSessionPlayTime(playerId);

                    //Send PluginMessage to all sub-servers
                    sendStringToAllServers(PLAYTIME_GET_SESSION_TIME, playerId.toString() + ";" + timePlayed);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * This method sends a message to all online sub-servers
     * in this BungeeCord network.
     *
     * @param channel The channel in which this message should be sent
     * @param message The message which you want to send
     */
    private void sendStringToAllServers(String channel, String message) {
        for (String server : BungeeCord.getInstance().getServers().keySet()) {
            ServerInfo serverInfo = BungeeCord.getInstance().getServerInfo(server);
            sendTimePlayedToSubserver(channel, message, serverInfo);
        }
    }
}
