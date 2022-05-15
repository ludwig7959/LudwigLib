package xyz.ludwicz.library.command.parameter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.ludwicz.library.command.ParameterType;
import xyz.ludwicz.library.visibility.LudwigVisibilityHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerParameterType
        implements ParameterType<Player> {
    @Override
    public Player transform(CommandSender sender, String value) {
        if (sender instanceof Player && (value.equalsIgnoreCase("self") || value.equals(""))) {
            return (Player) sender;
        }
        Player player = Bukkit.getServer().getPlayer(value);
        if (player == null || sender instanceof Player && !LudwigVisibilityHandler.treatAsOnline(player, (Player) sender)) {
            sender.sendMessage(ChatColor.RED + "No player with the name \"" + value + "\" found.");
            return null;
        }
        return player;
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

