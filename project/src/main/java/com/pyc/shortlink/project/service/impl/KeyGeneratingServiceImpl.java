package com.pyc.shortlink.project.service.impl;

import com.pyc.shortlink.project.service.KeyGeneratingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class KeyGeneratingServiceImpl implements KeyGeneratingService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 预加载的 Key 数量
    private static final int PRELOAD_COUNT = 50000;

    // 本地内存中的阻塞队列，用于存储预加载的 Key
    private final BlockingQueue<String> validKeys = new LinkedBlockingQueue<>();

    // 异步任务线程池
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // 用于确保预加载任务的原子性
    private final AtomicBoolean isLoading = new AtomicBoolean(false);

    // 布隆过滤器，用于快速判断 Key 是否可能存在
    private final BloomFilter bloomFilter = new BloomFilter(1000000, 3);

    // 线程局部变量，用于优化随机数生成器
    private static final ThreadLocal<Random> threadLocalRandom = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };

    /**
     * 初始化时预加载一批 Key 并初始化布隆过滤器
     */
    public void init() {
        preloadKeys();
        initBloomFilter();
    }

    /**
     * 初始化布隆过滤器，将已存在的 Key 添加到过滤器中
     */
    private void initBloomFilter() {
        String sql = "SELECT `key` FROM t_valid_keys";
        jdbcTemplate.query(sql, rs -> {
            while (rs.next()) {
                String key = rs.getString("key");
                bloomFilter.add(key);
            }
        });
    }

    /**
     * 从数据库中获取一批未使用的 Key 并加载到内存中
     */
    private synchronized void preloadKeys() {
        // 如果已经在加载中，则直接返回
        if (!isLoading.compareAndSet(false, true)) {
            return;
        }

        List<String> validKeysBatch = new ArrayList<>();

        // 先从数据库中获取一批未使用的 Key
        validKeysBatch = loadValidKeysFromDB(PRELOAD_COUNT);

        // 如果从数据库中获取的 Key 不足，则生成新的 Key 并插入数据库
        if (validKeysBatch.size() < PRELOAD_COUNT) {
            int remaining = PRELOAD_COUNT - validKeysBatch.size();
            List<String> generatedKeys = generateKeys(remaining);
            validKeysBatch.addAll(checkValidKeys(generatedKeys));
            insertValidKeys(validKeysBatch.subList(validKeysBatch.size() - remaining, validKeysBatch.size()));
        }

        // 将有效的 Key 添加到内存队列和布隆过滤器
        validKeys.addAll(validKeysBatch);
        validKeysBatch.forEach(bloomFilter::add);

//        // 提交异步任务
//        CompletableFuture.runAsync(() -> {
//            try {
//                List<String> generatedKeys = new ArrayList<>(PRELOAD_COUNT);
//                for (int i = 0; i < PRELOAD_COUNT; i++) {
//                    String key = generateShortKey();
//                    generatedKeys.add(key);
//                }
//
//                // 批量检查生成的 Key 是否有效
//                List<String> validKeysBatch = checkValidKeys(generatedKeys);
//
//                // 批量插入有效的 Key 到数据库
//                insertValidKeys(validKeysBatch);
//
//                // 将有效的 Key 添加到内存队列和布隆过滤器
//                validKeys.addAll(validKeysBatch);
//                validKeysBatch.forEach(bloomFilter::add);
//            } finally {
//                isLoading.set(false);
//            }
//        }, executor);

//        List<String> generatedKeys = new ArrayList<>(PRELOAD_COUNT);
//        for (int i = 0; i < PRELOAD_COUNT; i++) {
//            String key = generateShortKey();
//            generatedKeys.add(key);
//        }
//
//        // 批量检查生成的 Key 是否有效
//        List<String> validKeysBatch = checkValidKeys(generatedKeys);
//
//        // 批量插入有效的 Key 到数据库
//        insertValidKeys(validKeysBatch);
//
//        // 将有效的 Key 添加到内存队列和布隆过滤器
//        validKeys.addAll(validKeysBatch);
//        validKeysBatch.forEach(bloomFilter::add);

        // 提交异步任务
//        CompletableFuture.runAsync(() -> {
//            try {
//                int loaded = 0;
//                while (loaded < PRELOAD_COUNT) {
//                    String key = generateShortKey();
//                    if (isValidKey(key)) {
//                        validKeys.add(key);
//                        bloomFilter.add(key); // 同步到布隆过滤器
//                        loaded++;
//                    }
//                }
//            } finally {
//                isLoading.set(false);
//            }
//        }, executor);
    }

    /**
     * 从数据库中加载一批未使用的 Key
     * @param count 需要加载的 Key 数量
     * @return 从数据库中加载的未使用 Key 列表
     */
    private List<String> loadValidKeysFromDB(int count) {
        String sql = "SELECT `key` FROM t_valid_keys LIMIT ?";
        return jdbcTemplate.queryForList(sql, String.class, count);
    }

    /**
     * 批量生成 Key
     * @param count 需要生成的 Key 数量
     * @return 生成的 Key 列表
     */
    private List<String> generateKeys(int count) {
        List<String> generatedKeys = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String key = generateShortKey();
            generatedKeys.add(key);
        }
        return generatedKeys;
    }

    /**
     * 批量检查生成的 Key 是否有效
     * @param keys 要检查的 Key 列表
     * @return 有效的 Key 列表
     */
    private List<String> checkValidKeys(List<String> keys) {
        if (keys.isEmpty()) {
            return Collections.emptyList();
        }

        String inClause = String.join(",", Collections.nCopies(keys.size(), "?"));
        String sql = "SELECT `key` FROM t_valid_keys WHERE `key` IN (" + inClause + ")";
        List<String> existingKeys = jdbcTemplate.queryForList(sql, String.class, keys.toArray());

        List<String> validKeys = new ArrayList<>();
        for (String key : keys) {
            if (!existingKeys.contains(key)) {
                validKeys.add(key);
            }
        }
        return validKeys;
    }

    /**
     * 批量插入有效的 Key 到数据库
     * @param keys 有效的 Key 列表
     */
    private void insertValidKeys(List<String> keys) {
        if (keys.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO t_valid_keys (`key`) VALUES (?)";
        List<Object[]> batchArgs = new ArrayList<>();
        for (String key : keys) {
            batchArgs.add(new Object[]{key});
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 检查 Key 是否有效（未被使用过）
     * @param key 要检查的 Key
     * @return 如果 Key 有效返回 true，否则返回 false
     */
    private boolean isValidKey(String key) {
        String sql = "SELECT COUNT(*) FROM t_valid_keys WHERE `key` = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, key);
        return count == null || count == 0;
    }

    /**
     * 获取一个未使用的 Key
     * @return 未使用的 Key
     */
    public String getValidKey() {
        // 如果内存中的 Key 不足，则触发预加载
        if (validKeys.size() < PRELOAD_COUNT / 2) {
            preloadKeys();
        }

        String key = validKeys.poll();
        if (key != null) {
            CompletableFuture.runAsync(() -> {
                markKeyAsUsed(key);
            }, executor);
            // 将 Key 标记为已使用
            // markKeyAsUsed(key);
        }
        return key;
    }

    /**
     * 将 Key 标记为已使用
     * @param key 要标记的 Key
     */
    private void markKeyAsUsed(String key) {
        String insertSql = "INSERT INTO t_unvalid_keys (`key`) VALUES (?)";
        String deleteSql = "DELETE FROM t_valid_keys WHERE `key` = ?";
        jdbcTemplate.update(insertSql, key);
        jdbcTemplate.update(deleteSql, key);
    }

    /**
     * 关闭异步任务线程池
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * 生成短链 Key
     * @return 生成的 Key
     */
    private String generateShortKey() {
        return ShortKeyGenerator.generateShortKey();
    }
}

// 布隆过滤器实现
class BloomFilter {
    private BitSet bitSet;
    private int size;
    private int hashFunctionsCount;

    public BloomFilter(int size, int hashFunctionsCount) {
        this.size = size;
        this.hashFunctionsCount = hashFunctionsCount;
        this.bitSet = new BitSet(size);
    }

    public void add(String key) {
        for (int i = 0; i < hashFunctionsCount; i++) {
            int hash = key.hashCode() + i;
            int index = Math.abs(hash % size);
            bitSet.set(index);
        }
    }

    public boolean mightContain(String key) {
        for (int i = 0; i < hashFunctionsCount; i++) {
            int hash = key.hashCode() + i;
            int index = Math.abs(hash % size);
            if (!bitSet.get(index)) {
                return false;
            }
        }
        return true;
    }
}

// 短链 Key 生成器
class ShortKeyGenerator {
    private static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int KEY_LENGTH = 6;

    public static String generateShortKey() {
        Random random = threadLocalRandom.get();
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    // 线程局部变量，用于优化随机数生成器
    private static final ThreadLocal<Random> threadLocalRandom = new ThreadLocal<Random>() {
        @Override
        protected Random initialValue() {
            return new Random();
        }
    };
}