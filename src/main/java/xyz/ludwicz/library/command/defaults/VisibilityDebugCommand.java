package xyz.ludwicz.library.command.defaults;

import com.google.common.collect.Iterables;
import xyz.ludwicz.library.command.Command;
import xyz.ludwicz.library.command.Param;
import xyz.ludwicz.library.visibility.LudwigVisibilityHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class VisibilityDebugCommand {
    @Command(names={"visibilitydebug", "debugvisibility", "visdebug", "cansee"}, permission="")
    public static void visibilityDebug(Player sender, @Param(name="viewer") Player viewer, @Param(name="target") Player target) {
        boolean bukkit;
        List<String> lines= LudwigVisibilityHandler.getDebugInfo(target, viewer);
        for ( String debugLine : lines ) {
            sender.sendMessage(debugLine);
        }
        boolean shouldBeAbleToSee=false;
        if (!Iterables.getLast(lines).contains("cannot")) {
            shouldBeAbleToSee=true;
        }
        if (shouldBeAbleToSee != (bukkit=viewer.canSee(target))) {
            sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Updating was not done correctly: " + viewer.getName() + " should be able to see " + target.getName() + " but cannot.");
        } else {
            sender.sendMessage(ChatColor.GREEN + "Bukkit currently respects this result.");
        }
    }
}

