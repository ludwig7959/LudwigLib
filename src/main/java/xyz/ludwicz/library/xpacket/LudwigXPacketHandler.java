/*
 * Decompiled with CFR 0.150.
 *
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.plugin.Plugin
 */
package xyz.ludwicz.library.xpacket;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import xyz.ludwicz.library.LudwigLib;

public final class LudwigXPacketHandler {
    private static final String GLOBAL_MESSAGE_CHANNEL = "XPacket:All";
    static final String PACKET_MESSAGE_DIVIDER = "||";

    public static void init() {
        FileConfiguration config = LudwigLib.getInstance().getConfig();
        String localHost = config.getString("REDIS.LOCAL.HOST");
        int localDb = config.getInt("REDIS.LOCAL.DBID", 3);
        String remoteHost = config.getString("REDIS.BACKBONE.HOST");
        int remoteDb = config.getInt("REDIS.BACKBONE.DBID", 4);
        boolean sameServer = localHost.equalsIgnoreCase(remoteHost) && localDb == remoteDb;
        LudwigXPacketHandler.connectToServer(LudwigLib.getInstance().getLocalJedisPool());
        if (!sameServer) {
            LudwigXPacketHandler.connectToServer(LudwigLib.getInstance().getBackboneJedisPool());
        }
    }

    public static void connectToServer(JedisPool connectTo) {
        if (!LudwigLib.getInstance().isRedisEnabled())
            return;

        Thread subscribeThread = new Thread(() -> {
            while (LudwigLib.getInstance().isEnabled()) {
                try {
                    Jedis jedis = connectTo.getResource();
                    Throwable throwable = null;
                    try {
                        XPacketPubSub pubSub = new XPacketPubSub();
                        String channel = GLOBAL_MESSAGE_CHANNEL;
                        jedis.subscribe(pubSub, channel);
                    } catch (Throwable throwable2) {
                        throwable = throwable2;
                        throw throwable2;
                    } finally {
                        if (jedis == null) continue;
                        if (throwable != null) {
                            try {
                                jedis.close();
                            } catch (Throwable throwable3) {
                                throwable.addSuppressed(throwable3);
                            }
                            continue;
                        }
                        jedis.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "qLib - XPacket Subscribe Thread");
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    public static void sendToAll(XPacket packet) {
        LudwigXPacketHandler.send(packet, LudwigLib.getInstance().getBackboneJedisPool());
    }

    public static void sendToAllViaLocal(XPacket packet) {
        LudwigXPacketHandler.send(packet, LudwigLib.getInstance().getLocalJedisPool());
    }

    public static void send(XPacket packet, JedisPool sendOn) {
        if (!LudwigLib.getInstance().isEnabled()) {
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {

                try (Jedis jedis = sendOn.getResource()) {
                    String encodedPacket = packet.getClass().getName() + PACKET_MESSAGE_DIVIDER + LudwigLib.PLAIN_GSON.toJson(packet);
                    jedis.publish(GLOBAL_MESSAGE_CHANNEL, encodedPacket);
                }
            }
        }.runTaskAsynchronously(LudwigLib.getInstance());
    }

    private LudwigXPacketHandler() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

