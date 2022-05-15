package xyz.ludwicz.library.command.defaults;

import xyz.ludwicz.library.command.Command;
import xyz.ludwicz.library.command.Param;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class EvalCommand {
    @Command(names={"eval"}, permission="console", description="Evaluates a command")
    public static void eval(CommandSender sender, @Param(name="command", wildcard=true) String commandLine) {
        if (!(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage(ChatColor.RED + "This is a console-only utility command. It cannot be used from in-game.");
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandLine);
    }
}

