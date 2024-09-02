package com.ershi.dahu.constant;

public class RedisCacheConstant {

    /**
     *AI生成评价缓存前缀
     */
    public static final String AI_TEST_SCORING_RESULT = "cache:ai:test:";

    /**
     *AI生成评价缓存有效时间
     */
    public static final Long AI_TEST_SCORING_RESULT_TTL = 7L;

    /**
     *AI生成评价缓存重建锁
     */
    public static final String LOCK_AI_TEST = "lock:ai:test:";

    /**
     *APP应用缓存
     */
    public static final String APP_CACHE = "cache:app:";

    /**
     *APP应用缓存逻辑过期时间(2小时)
     */
    public static final Long APP_CACHE_EXPIRE_HOURS = 2L;

    /**
     *APP应用缓存重建锁
     */
    public static final String LOCK_APP = "lock:app:";
}
