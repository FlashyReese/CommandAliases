package me.flashyreese.mods.commandaliases.storage.database.redis;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class RedisImpl implements AbstractDatabase<String, String> {
    private final String host;
    private final int port;
    private final int database;
    private final String user;
    private final String password;
    private JedisPool jedisPool;

    public RedisImpl(String host, int port, int database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public boolean open() {
        if (this.jedisPool == null) {
            this.jedisPool = new JedisPool(new JedisPoolConfig(), this.host, this.port, 0, this.user, this.password, this.database);
            return true;
        }
        return false;
    }

    @Override
    public boolean close() {
        if (this.jedisPool != null) {
            this.jedisPool.close();
            return true;
        }
        return false;
    }

    @Override
    public boolean write(String key, String value) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            return jedis.set(key, value).equals("OK");
        }
    }

    @Override
    public String read(String key) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public boolean delete(String key) {
        try (Jedis jedis = this.jedisPool.getResource()) {
            return jedis.del(key) == 1;
        }
    }

    @Override
    public Map<String, String> map() {
        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.keys("*").forEach(key -> map.put(key, jedis.get(key)));
        }
        return map;
    }
}
