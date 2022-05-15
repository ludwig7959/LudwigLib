package xyz.ludwicz.library.command;

import lombok.Getter;
import lombok.Setter;
import xyz.ludwicz.library.command.bukkit.LudwigCommand;
import xyz.ludwicz.library.command.bukkit.LudwigHelpTopic;
import xyz.ludwicz.library.command.defaults.BuildCommand;
import xyz.ludwicz.library.command.defaults.CommandInfoCommand;
import xyz.ludwicz.library.command.defaults.EvalCommand;
import xyz.ludwicz.library.command.defaults.VisibilityDebugCommand;
import xyz.ludwicz.library.command.parameter.*;
import xyz.ludwicz.library.command.parameter.filter.NormalFilter;
import xyz.ludwicz.library.command.parameter.filter.StrictFilter;
import xyz.ludwicz.library.command.parameter.offlineplayer.OfflinePlayerWrapper;
import xyz.ludwicz.library.command.parameter.offlineplayer.OfflinePlayerWrapperParameterType;
import xyz.ludwicz.library.util.ClassUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LudwigCommandHandler {
    public static CommandNode ROOT_NODE = new CommandNode();
    protected static Map<Class<?>, ParameterType<?>> PARAMETER_TYPE_MAP = new HashMap<>();
    protected static CommandMap commandMap;
    protected static Map<String, Command> knownCommands;
    @Getter
    @Setter
    private static CommandConfiguration config;

    public static void init() {
        LudwigCommandHandler.registerClass(BuildCommand.class);
        LudwigCommandHandler.registerClass(EvalCommand.class);
        LudwigCommandHandler.registerClass(CommandInfoCommand.class);
        LudwigCommandHandler.registerClass(VisibilityDebugCommand.class);
        /* new BukkitRunnable() {
            public void run() {
                try {
                    FrozenCommandHandler.swapCommandMap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(qLib.getInstance(), 5L); */
    }

    public static void registerParameterType(Class<?> clazz, ParameterType<?> type) {
        PARAMETER_TYPE_MAP.put(clazz, type);
    }

    public static ParameterType getParameterType(Class<?> clazz) {
        return PARAMETER_TYPE_MAP.get(clazz);
    }

    public static void registerMethod(Method method) {
        method.setAccessible(true);
        Set<CommandNode> nodes = new MethodProcessor().process(method);
        if (nodes != null) {
            nodes.forEach(node -> {
                if (node != null) {
                    LudwigCommand command = new LudwigCommand(node, JavaPlugin.getProvidingPlugin(method.getDeclaringClass()));
                    LudwigCommandHandler.register(command);
                    node.getChildren().values().forEach(n -> LudwigCommandHandler.registerHelpTopic(n, node.getAliases()));
                }
            });
        }
    }

    protected static void registerHelpTopic(CommandNode node, Set<String> aliases) {
        if (node.method != null) {
            Bukkit.getHelpMap().addTopic(new LudwigHelpTopic(node, aliases));
        }
        if (node.hasCommands()) {
            node.getChildren().values().forEach(n -> LudwigCommandHandler.registerHelpTopic(n, null));
        }
    }

    private static void register(LudwigCommand command) {
        try {
            Map<String, Command> knownCommands = LudwigCommandHandler.getKnownCommands();
            Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Command> entry = iterator.next();
                if (!entry.getValue().getName().equalsIgnoreCase(command.getName())) continue;
                entry.getValue().unregister(commandMap);
                iterator.remove();
            }
            for (String alias : command.getAliases()) {
                knownCommands.put(alias, command);
            }
            command.register(commandMap);
            knownCommands.put(command.getName(), command);
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public static void registerClass(Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            LudwigCommandHandler.registerMethod(method);
        }
    }

    public static void unregisterClass(Class<?> clazz) {
        Map<String, Command> knownCommands = LudwigCommandHandler.getKnownCommands();
        Iterator<Command> iterator = knownCommands.values().iterator();
        while (iterator.hasNext()) {
            CommandNode node;
            Command command = iterator.next();
            if (!(command instanceof LudwigCommand) || ((LudwigCommand) command).getNode().getOwningClass() != clazz)
                continue;
            command.unregister(commandMap);
            iterator.remove();
        }
    }

    public static void registerPackage(Plugin plugin, String packageName) {
        ClassUtils.getClassesInPackage(plugin, packageName).forEach(LudwigCommandHandler::registerClass);
    }

    public static void registerAll(Plugin plugin) {
        LudwigCommandHandler.registerPackage(plugin, plugin.getClass().getPackage().getName());
    }

    private static VarHandle MODIFIERS;

    /* static {
        try {
            MODIFIERS = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup()).findVarHandle(Field.class, "modifiers", int.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    } */

    /* private static void swapCommandMap() throws Exception {
        Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);

        MODIFIERS.set(commandMapField, commandMapField.getModifiers() & ~Modifier.FINAL);

        Object oldCommandMap = commandMapField.get(Bukkit.getServer());

        FrozenCommandMap newCommandMap = new FrozenCommandMap(Bukkit.getServer());
        Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommandsField.setAccessible(true);

        MODIFIERS.set(knownCommandsField, knownCommandsField.getModifiers() & ~Modifier.FINAL);

        knownCommandsField.set(newCommandMap, knownCommandsField.get(oldCommandMap));
        commandMapField.set(Bukkit.getServer(), newCommandMap);
    } */

    protected static CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }

    protected static Map<String, Command> getKnownCommands() {
        return commandMap.getKnownCommands();
    }

    static {
        config = new CommandConfiguration().setNoPermissionMessage("&cNo permission.");
        LudwigCommandHandler.registerParameterType(Boolean.TYPE, new BooleanParameterType());
        LudwigCommandHandler.registerParameterType(Integer.TYPE, new IntegerParameterType());
        LudwigCommandHandler.registerParameterType(Double.TYPE, new DoubleParameterType());
        LudwigCommandHandler.registerParameterType(Float.TYPE, new FloatParameterType());
        LudwigCommandHandler.registerParameterType(String.class, new StringParameterType());
        LudwigCommandHandler.registerParameterType(Player.class, new PlayerParameterType());
        LudwigCommandHandler.registerParameterType(World.class, new WorldParameterType());
        LudwigCommandHandler.registerParameterType(ItemStack.class, new ItemStackParameterType());
        LudwigCommandHandler.registerParameterType(OfflinePlayer.class, new OfflinePlayerParameterType());
        LudwigCommandHandler.registerParameterType(UUID.class, new UUIDParameterType());
        LudwigCommandHandler.registerParameterType(OfflinePlayerWrapper.class, new OfflinePlayerWrapperParameterType());
        LudwigCommandHandler.registerParameterType(NormalFilter.class, new NormalFilter());
        LudwigCommandHandler.registerParameterType(StrictFilter.class, new StrictFilter());
        commandMap = LudwigCommandHandler.getCommandMap();
        knownCommands = LudwigCommandHandler.getKnownCommands();
    }
}

