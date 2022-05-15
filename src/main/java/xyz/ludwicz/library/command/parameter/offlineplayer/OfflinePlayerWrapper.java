package xyz.ludwicz.library.command.parameter.offlineplayer;

import com.mojang.authlib.GameProfile;
import xyz.ludwicz.library.LudwigLib;
import xyz.ludwicz.library.util.Callback;
import xyz.ludwicz.library.util.UUIDUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class OfflinePlayerWrapper {
    private String source;
    private UUID uniqueId;
    private String name;

    public OfflinePlayerWrapper(String source) {
        this.source = source;
    }

    public void loadAsync(final Callback<Player> callback) {
        new BukkitRunnable() {

            public void run() {
                final Player player = OfflinePlayerWrapper.this.loadSync();
                new BukkitRunnable() {

                    public void run() {
                        callback.callback(player);
                    }
                }.runTask(LudwigLib.getInstance());
            }
        }.runTaskAsynchronously(LudwigLib.getInstance());
    }

    public Player loadSync() {
        if (!(this.source.charAt(0) != '\"' && this.source.charAt(0) != '\'' || this.source.charAt(this.source.length() - 1) != '\"' && this.source.charAt(this.source.length() - 1) != '\'')) {
            this.source = this.source.replace("'", "").replace("\"", "");
            this.uniqueId = UUIDUtils.uuid(this.source);
            if (this.uniqueId == null) {
                this.name = this.source;
                return null;
            }
            this.name = UUIDUtils.name(this.uniqueId);
            if (Bukkit.getPlayer(this.uniqueId) != null) {
                return Bukkit.getPlayer(this.uniqueId);
            }
            if (!Bukkit.getOfflinePlayer(this.uniqueId).hasPlayedBefore()) {
                return null;
            }
            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            EntityPlayer entity = new EntityPlayer(server, server.getWorlds().iterator().next(), new GameProfile(this.uniqueId, this.name));
            CraftPlayer player = entity.getBukkitEntity();
            if (player != null) {
                player.loadData();
            }
            return player;
        }
        if (Bukkit.getPlayer(this.source) != null) {
            return Bukkit.getPlayer(this.source);
        }
        this.uniqueId = UUIDUtils.uuid(this.source);
        if (this.uniqueId == null) {
            this.name = this.source;
            return null;
        }
        this.name = UUIDUtils.name(this.uniqueId);
        if (Bukkit.getPlayer(this.uniqueId) != null) {
            return Bukkit.getPlayer(this.uniqueId);
        }
        if (!Bukkit.getOfflinePlayer(this.uniqueId).hasPlayedBefore()) {
            return null;
        }
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        EntityPlayer entity = new EntityPlayer(server, server.getWorlds().iterator().next(), new GameProfile(this.uniqueId, this.name));
        CraftPlayer player = entity.getBukkitEntity();
        if (player != null) {
            player.loadData();
        }
        return player;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getName() {
        return this.name;
    }
}

