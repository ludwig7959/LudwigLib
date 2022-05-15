package xyz.ludwicz.library.uuid;

import com.google.common.base.Preconditions;
import lombok.Getter;
import xyz.ludwicz.library.LudwigLib;

import java.util.UUID;

public final class LudwigUUIDCache {

    @Getter
    private static UUIDCache impl = null;
    private static boolean initiated = false;

    private LudwigUUIDCache() {
    }

    public static void init() {
        Preconditions.checkState((!initiated ? 1 : 0) != 0);
        initiated = true;
        try {
            impl = (UUIDCache) Class.forName(LudwigLib.getInstance().getConfig().getString("UUIDCache.Backend", "xyz.ludwicz.library.uuid.impl.BukkitUUIDCache")).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LudwigLib.getInstance().getServer().getPluginManager().registerEvents(new UUIDListener(), LudwigLib.getInstance());
    }

    public static UUID uuid(String name) {
        return impl.uuid(name);
    }

    public static String name(UUID uuid) {
        return impl.name(uuid);
    }

    public static void ensure(UUID uuid) {
        impl.ensure(uuid);
    }

    public static void update(UUID uuid, String name) {
        impl.update(uuid, name);
    }
}

