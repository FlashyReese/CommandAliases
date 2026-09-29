package me.flashyreese.mods.commandaliases.storage.database.redis;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.flashyreese.mods.commandaliases.CommandAliasesMod;
import me.flashyreese.mods.commandaliases.storage.database.AbstractDatabase;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.ScanParams;

import java.util.Collections;
import java.util.Map;

public class RedisImpl implements AbstractDatabase<String, String> {
    private final String host;
    private final int port;
    private final int database;
    private final String user;
    private final String password;
    private volatile RedisClient redisClient;

    public RedisImpl(String host, int port, int database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public synchronized boolean open() {
        if (this.redisClient != null) {
            return true;
        }

        RedisClient client = null;
        try {
            DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                    .database(this.database)
                    .user(this.user)
                    .password(this.password)
                    .build();
            client = RedisClient.builder()
                    .hostAndPort(this.host, this.port)
                    .clientConfig(clientConfig)
                    .build();
            client.ping();
            this.redisClient = client;
            return true;
        } catch (RuntimeException e) {
            if (client != null) {
                try {
                    client.close();
                } catch (RuntimeException closeException) {
                    e.addSuppressed(closeException);
                }
            }
            CommandAliasesMod.logger().error("Could not open Redis database at {}:{}", this.host, this.port, e);
            return false;
        }
    }

    @Override
    public synchronized boolean close() {
        RedisClient client = this.redisClient;
        this.redisClient = null;
        if (client == null) {
            return true;
        }

        try {
            client.close();
            return true;
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not close Redis database at {}:{}", this.host, this.port, e);
            return false;
        }
    }

    @Override
    public boolean write(String key, String value) {
        RedisClient client = this.redisClient;
        if (key == null || value == null || client == null) {
            return false;
        }

        try {
            return "OK".equals(client.set(key, value));
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not write key to Redis at {}:{}", this.host, this.port, e);
            return false;
        }
    }

    @Override
    public String read(String key) {
        RedisClient client = this.redisClient;
        if (key == null || client == null) {
            return null;
        }

        try {
            return client.get(key);
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not read key from Redis at {}:{}", this.host, this.port, e);
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        RedisClient client = this.redisClient;
        if (key == null || client == null) {
            return false;
        }

        try {
            return client.del(key) == 1;
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not delete key from Redis at {}:{}", this.host, this.port, e);
            return false;
        }
    }

    @Override
    public Map<String, String> map() {
        RedisClient client = this.redisClient;
        if (client == null) {
            return Collections.emptyMap();
        }

        Map<String, String> map = new Object2ObjectOpenHashMap<>();
        try {
            ScanParams scanParams = new ScanParams().match("*").count(256);
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                var result = client.scan(cursor, scanParams);
                for (String key : result.getResult()) {
                    String value = client.get(key);
                    if (value != null) {
                        map.put(key, value);
                    }
                }
                cursor = result.getCursor();
            } while (!ScanParams.SCAN_POINTER_START.equals(cursor));

            return Map.copyOf(map);
        } catch (RuntimeException e) {
            CommandAliasesMod.logger().error("Could not read Redis contents at {}:{}", this.host, this.port, e);
            return Collections.emptyMap();
        }
    }
}
