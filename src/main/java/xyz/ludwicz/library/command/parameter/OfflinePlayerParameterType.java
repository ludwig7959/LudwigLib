package xyz.ludwicz.library.command.parameter;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.ludwicz.library.LudwigLib;
import xyz.ludwicz.library.command.ParameterType;
import xyz.ludwicz.library.visibility.LudwigVisibilityHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OfflinePlayerParameterType
        implements ParameterType<OfflinePlayer> {
    @Override
    public OfflinePlayer transform(CommandSender sender, String source) {
        if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals(""))) {
            return (Player) sender;
        }
        return LudwigLib.getInstance().getServer().getOfflinePlayer(source);
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        ArrayList<String> completions = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!LudwigVisibilityHandler.treatAsOnline(player, sender)) continue;
            completions.add(player.getName());
        }
        return completions;
    }
}

