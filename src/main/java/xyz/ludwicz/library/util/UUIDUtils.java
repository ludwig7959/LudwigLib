package xyz.ludwicz.library.util;

import xyz.ludwicz.library.uuid.LudwigUUIDCache;

import java.util.UUID;

public final class UUIDUtils {
    private UUIDUtils() {
    }

    public static String name(UUID uuid) {
        String name = LudwigUUIDCache.name(uuid);
        return name == null ? "null" : name;
    }

    public static UUID uuid(String name) {
        return LudwigUUIDCache.uuid(name);
    }

    public static String formatPretty(UUID uuid) {
        return UUIDUtils.name(uuid) + " [" + uuid + "]";
    }
}

