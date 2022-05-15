package xyz.ludwicz.library.visibility;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.ludwicz.library.LudwigLib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LudwigVisibilityHandler {
    private static final Map<String, VisibilityHandler> handlers = new LinkedHashMap<>();
    private static final Map<String, OverrideHandler> overrideHandlers = new LinkedHashMap<>();
    private static boolean initiated = false;

    private LudwigVisibilityHandler() {
    }

    public static void init() {
        Preconditions.checkState((!initiated ? 1 : 0) != 0);
        initiated = true;
        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler(priority = EventPriority.LOWEST)
            public void onPlayerJoin(PlayerJoinEvent event) {
                LudwigVisibilityHandler.update(event.getPlayer());
            }

            @EventHandler(priority = EventPriority.LOWEST)
            public void onTabComplete(PlayerChatTabCompleteEvent event) {
                String token = event.getLastToken();
                Collection<String> completions = event.getTabCompletions();
                completions.clear();
                for (Player target : Bukkit.getOnlinePlayers()) {
                    if (!LudwigVisibilityHandler.treatAsOnline(target, event.getPlayer()) || !StringUtils.startsWithIgnoreCase(target.getName(), token))
                        continue;
                    completions.add(target.getName());
                }
            }
        }, LudwigLib.getInstance());
    }

    public static void registerHandler(String identifier, VisibilityHandler handler) {
        handlers.put(identifier, handler);
    }

    public static void registerOverride(String identifier, OverrideHandler handler) {
        overrideHandlers.put(identifier, handler);
    }

    public static void update(Player player) {
        if (handlers.isEmpty() && overrideHandlers.isEmpty()) {
            return;
        }
        LudwigVisibilityHandler.updateAllTo(player);
        LudwigVisibilityHandler.updateToAll(player);
    }

    @Deprecated
    public static void updateAllTo(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (!LudwigVisibilityHandler.shouldSee(target, viewer)) {
                viewer.hidePlayer(target);
                continue;
            }
            viewer.showPlayer(target);
        }
    }

    @Deprecated
    public static void updateToAll(Player target) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!LudwigVisibilityHandler.shouldSee(target, viewer)) {
                viewer.hidePlayer(target);
                continue;
            }
            viewer.showPlayer(target);
        }
    }

    public static boolean treatAsOnline(Player target, Player viewer) {
        return viewer.canSee(target) || !target.hasMetadata("invisible") || viewer.hasPermission("basic.staff");
    }

    private static boolean shouldSee(Player target, Player viewer) {
        for (OverrideHandler overrideHandler : overrideHandlers.values()) {
            if (overrideHandler.getAction(target, viewer) != OverrideAction.SHOW) continue;
            return true;
        }
        for (VisibilityHandler visibilityHandler : handlers.values()) {
            if (visibilityHandler.getAction(target, viewer) != VisibilityAction.HIDE) continue;
            return false;
        }
        return true;
    }

    public static List<String> getDebugInfo(final Player target, final Player viewer) {
        final List<String> debug = new ArrayList<>();
        Boolean canSee = null;
        for (final Map.Entry<String, OverrideHandler> entry : LudwigVisibilityHandler.overrideHandlers.entrySet()) {
            final OverrideHandler handler = entry.getValue();
            final OverrideAction action = handler.getAction(target, viewer);
            ChatColor color = ChatColor.GRAY;
            if (action == OverrideAction.SHOW && canSee == null) {
                canSee = true;
                color = ChatColor.GREEN;
            }
            debug.add(color + "Overriding Handler: \"" + entry.getKey() + "\": " + action);
        }
        for (final Map.Entry<String, VisibilityHandler> entry2 : LudwigVisibilityHandler.handlers.entrySet()) {
            final VisibilityHandler handler2 = entry2.getValue();
            final VisibilityAction action2 = handler2.getAction(target, viewer);
            ChatColor color = ChatColor.GRAY;
            if (action2 == VisibilityAction.HIDE && canSee == null) {
                canSee = false;
                color = ChatColor.GREEN;
            }
            debug.add(color + "Normal Handler: \"" + entry2.getKey() + "\": " + action2);
        }
        if (canSee == null) {
            canSee = true;
        }
        debug.add(ChatColor.AQUA + "Result: " + viewer.getName() + " " + (canSee ? "can" : "cannot") + " see " + target.getName());
        return debug;
    }
}

