package xyz.ludwicz.library.uuid;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

final class UUIDListener
        implements Listener {
    UUIDListener() {
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        LudwigUUIDCache.update(event.getUniqueId(), event.getName());
    }
}

