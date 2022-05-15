package xyz.ludwicz.library.xpacket;

import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;
import xyz.ludwicz.library.LudwigLib;

final class XPacketPubSub
        extends JedisPubSub {
    XPacketPubSub() {
    }

    @Override
    public void onMessage(String channel, String message) {
        Class<?> packetClass;
        int packetMessageSplit = message.indexOf("||");
        String packetClassStr = message.substring(0, packetMessageSplit);
        String messageJson = message.substring(packetMessageSplit + "||".length());
        try {
            packetClass = Class.forName(packetClassStr);
        } catch (ClassNotFoundException ignored) {
            return;
        }
        XPacket packet = (XPacket) LudwigLib.PLAIN_GSON.fromJson(messageJson, packetClass);
        if (LudwigLib.getInstance().isEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    packet.onReceive();
                }
            }.runTask(LudwigLib.getInstance());
        }
    }
}

