package com.oracle.test.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public final class OrderNoUtil {

    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final SimpleDateFormat DAY = new SimpleDateFormat("yyyyMMddHHmmss");

    private OrderNoUtil() {}

    public static synchronized String generate(String prefix) {
        long seq = COUNTER.incrementAndGet() % 1000;
        return prefix + DAY.format(new Date()) + String.format("%03d", seq);
    }
}
