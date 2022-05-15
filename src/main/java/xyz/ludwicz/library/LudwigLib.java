package xyz.ludwicz.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import xyz.ludwicz.library.command.LudwigCommandHandler;
import xyz.ludwicz.library.redis.RedisCommand;
import xyz.ludwicz.library.serialization.BlockVectorAdapter;
import xyz.ludwicz.library.serialization.ItemStackAdapter;
import xyz.ludwicz.library.serialization.LocationAdapter;
import xyz.ludwicz.library.serialization.PotionEffectAdapter;
import xyz.ludwicz.library.serialization.VectorAdapter;
import xyz.ludwicz.library.visibility.LudwigVisibilityHandler;
import xyz.ludwicz.library.xpacket.LudwigXPacketHandler;

public class LudwigLib extends JavaPlugin {

    public static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter()).registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter()).registerTypeHierarchyAdapter(Location.class, new LocationAdapter()).registerTypeHierarchyAdapter(Vector.class, new VectorAdapter()).registerTypeAdapter(BlockVector.class, new BlockVectorAdapter()).setPrettyPrinting().serializeNulls().create();
    public static final Gson PLAIN_GSON = new GsonBuilder().registerTypeHierarchyAdapter(PotionEffect.class, new PotionEffectAdapter()).registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter()).registerTypeHierarchyAdapter(Location.class, new LocationAdapter()).registerTypeHierarchyAdapter(Vector.class, new VectorAdapter()).registerTypeAdapter(BlockVector.class, new BlockVectorAdapter()).serializeNulls().create();

    @Getter
    private static LudwigLib instance;

    @Getter
    private boolean redisEnabled;
    @Getter
    private JedisPool localJedisPool;
    @Getter
    private JedisPool backboneJedisPool;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        setupJedis();

        LudwigCommandHandler.init();
        LudwigVisibilityHandler.init();
        if (redisEnabled)
            LudwigXPacketHandler.init();
    }

    private void setupJedis() {
        FileConfiguration config = getConfig();

        redisEnabled = config.getBoolean("REDIS.ENABLED", false);
        if (!redisEnabled) {
            getLogger().warning("Redis is disabled, some features won't be working.");
            return;
        }

        try {
            localJedisPool = new JedisPool(new JedisPoolConfig(), config.getString("REDIS.LOCAL.HOST", "localhost"), config.getInt("REDIS.LOCAL.PORT", 6379), 20000, config.getBoolean("REDIS.LOCAL.AUTH.ENABLED", false) ? config.getString("REDIS.LOCAL.AUTH.PASSWORD") : null, config.getInt("REDIS.LOCAL.DBID", 3));
        } catch (Exception e) {
            localJedisPool = null;
            e.printStackTrace();
            getLogger().warning("Couldn't connect to a Redis instance.");
            redisEnabled = false;
        }

        try {
            backboneJedisPool = new JedisPool(new JedisPoolConfig(), config.getString("REDIS.BACKBONE.HOST", "localhost"), config.getInt("REDIS.BACKBONE.PORT", 6379), 20000, config.getBoolean("REDIS.BACKBONE.AUTH.ENABLED", false) ? config.getString("REDIS.BACKBONE.AUTH.PASSWORD") : null, config.getInt("REDIS.BACKBONE.DBID", 4));
        } catch (Exception e) {
            backboneJedisPool = null;
            e.printStackTrace();
            getLogger().warning("Couldn't connect to a Backbone Redis instance.");
            redisEnabled = false;
        }
    }


    public <T> T runRedisCommand(RedisCommand<T> redisCommand) {
        if (!redisEnabled)
            return null;

        Jedis jedis = this.localJedisPool.getResource();
        T result = null;
        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            if (jedis != null) {
                this.localJedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                this.localJedisPool.returnResource(jedis);
            }
        }
        return result;
    }

    public <T> T runBackboneRedisCommand(RedisCommand<T> redisCommand) {
        if (!redisEnabled)
            return null;

        Jedis jedis = this.backboneJedisPool.getResource();
        T result = null;
        try {
            result = redisCommand.execute(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            if (jedis != null) {
                this.backboneJedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            if (jedis != null) {
                this.backboneJedisPool.returnResource(jedis);
            }
        }
        return result;
    }
}
