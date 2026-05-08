package com.oracle.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 虚拟时钟：所有业务时间获取的唯一入口。
 *
 * - 数据库侧：F_NOW() = SYSDATE + OFFSET_DAYS（mapper xml 全部使用 F_NOW()）
 * - Java 侧：clock.now() = new Date(System.currentTimeMillis() + offsetDays * 86400_000)
 *
 * 偏移持久化在 SYS_CLOCK 单行表（ID=1）；管理员可通过 SystemClockController 调整。
 * Java 端缓存 30 秒，避免每个 new Date 都查库；advance/setOffset/reset 后会立即刷新。
 */
@Service
public class SystemClock {

    @Autowired private JdbcTemplate jdbc;

    private volatile double offsetDays = 0d;
    private volatile long   loadedAt   = 0L;
    private static final long CACHE_TTL_MS = 30_000L;

    /** 当前虚拟时间 */
    public Date now() {
        ensureLoaded();
        long ms = System.currentTimeMillis() + (long)(offsetDays * 86_400_000d);
        return new Date(ms);
    }

    public double getOffsetDays() {
        ensureLoaded();
        return offsetDays;
    }

    /** 在当前偏移基础上前进 days 天（可为负数回退）。 */
    @Transactional
    public double advance(double days) {
        ensureLoaded();
        double newOff = offsetDays + days;
        jdbc.update("UPDATE SYS_CLOCK SET OFFSET_DAYS = ? WHERE ID = 1", newOff);
        offsetDays = newOff;
        loadedAt = System.currentTimeMillis();
        return newOff;
    }

    /** 直接设定虚拟时间为指定时刻（计算并写入对应 offset）。 */
    @Transactional
    public double setVirtualTime(Date target) {
        if (target == null) throw new IllegalArgumentException("target 不能为空");
        double newOff = (target.getTime() - System.currentTimeMillis()) / 86_400_000d;
        jdbc.update("UPDATE SYS_CLOCK SET OFFSET_DAYS = ? WHERE ID = 1", newOff);
        offsetDays = newOff;
        loadedAt = System.currentTimeMillis();
        return newOff;
    }

    /** 重置为真实时间（offset = 0）。 */
    @Transactional
    public void reset() {
        jdbc.update("UPDATE SYS_CLOCK SET OFFSET_DAYS = 0 WHERE ID = 1");
        offsetDays = 0d;
        loadedAt = System.currentTimeMillis();
    }

    private void ensureLoaded() {
        long now = System.currentTimeMillis();
        if (now - loadedAt > CACHE_TTL_MS) {
            try {
                Double v = jdbc.queryForObject(
                        "SELECT OFFSET_DAYS FROM SYS_CLOCK WHERE ID = 1", Double.class);
                offsetDays = v == null ? 0d : v;
            } catch (Exception ignore) {
                offsetDays = 0d;
            }
            loadedAt = now;
        }
    }
}
