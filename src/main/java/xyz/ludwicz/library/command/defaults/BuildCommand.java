package xyz.ludwicz.library.command.defaults;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import xyz.ludwicz.library.LudwigLib;
import xyz.ludwicz.library.command.Command;

public class BuildCommand {
    @Command(names = {"build"}, permission = "op")
    public static void build(Player sender) {
        if (sender.hasMetadata("build")) {
            sender.removeMetadata("build", LudwigLib.getInstance());
        } else {
            sender.setMetadata("build", new FixedMetadataValue(LudwigLib.getInstance(), true));
        }
        sender.sendMessage(ChatColor.YELLOW + "You are " + (sender.hasMetadata("build") ? ChatColor.GREEN + "now" : ChatColor.RED + "no longer") + ChatColor.YELLOW + " in build mode.");
    }
}

