package xyz.ludwicz.library.command.parameter.offlineplayer;

import xyz.ludwicz.library.command.ParameterType;
import xyz.ludwicz.library.visibility.LudwigVisibilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OfflinePlayerWrapperParameterType
        implements ParameterType<OfflinePlayerWrapper> {
    @Override
    public OfflinePlayerWrapper transform(CommandSender sender, String source) {
        return new OfflinePlayerWrapper(source);
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        ArrayList<String> completions=new ArrayList<>();
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            if (!LudwigVisibilityHandler.treatAsOnline(player, sender)) continue;
            completions.add(player.getName());
        }
        return completions;
    }
}

