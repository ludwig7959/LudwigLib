package xyz.ludwicz.library.uuid.impl;

import xyz.ludwicz.library.LudwigLib;
import xyz.ludwicz.library.uuid.UUIDCache;

import java.util.UUID;

public final class BukkitUUIDCache
        implements UUIDCache {
    @Override
    public UUID uuid(String name) {
        return LudwigLib.getInstance().getServer().getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public String name(UUID uuid) {
        return LudwigLib.getInstance().getServer().getOfflinePlayer(uuid).getName();
    }

    @Override
    public void ensure(UUID uuid) {
    }

    @Override
    public void update(UUID uuid, String name) {
    }
}

