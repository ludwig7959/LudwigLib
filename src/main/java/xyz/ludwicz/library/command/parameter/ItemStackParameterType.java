package xyz.ludwicz.library.command.parameter;

import xyz.ludwicz.library.command.ParameterType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemStackParameterType
        implements ParameterType<ItemStack> {

    private static final List<String> ITEM_TYPES = Arrays.stream(Material.values()).map(material -> material.toString().toLowerCase()).collect(Collectors.toList());

    @Override
    public ItemStack transform(CommandSender sender, String source) {
        Material type = Material.getMaterial(source.toUpperCase());

        ItemStack item = new ItemStack(type);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "No item with the name " + source + " found.");
            return null;
        }
        return item;
    }

    @Override
    public List<String> tabComplete(Player sender, Set<String> flags, String prefix) {
        return ITEM_TYPES;
    }
}

