package com.atguigu.spzx.common.constant;

public class RedisConst {

    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_SUFFIX = ":info";
    //单位：秒
    public static final long SKUKEY_TIMEOUT = 24 * 60 * 60;

    public static final long SKUKEY_EMPTY_TIMEOUT = 30;

    public static final String PRODUCT_BLOOM_FILTER = "product:bloom:filter";

    public static final String PRODUCT_NEW_BLOOM_FILTER = "product:new:bloom:filter";

    public static final String CATEGORY_ONE = "category:one";

    public static final String PRODUCT_LOCK_SUFFIX = "product:lock";

    public static final String CATEGORY_ONE_LOCK = "category:one:lock";
}
