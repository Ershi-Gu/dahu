package com.ershi.dahu.config;


import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vip线程池
 *
 * @author Ershi
 * @date 2024/08/06
 */
@Configuration
@Data
public class VipSchedulerConfig {

    private static final int VIP_CORE_POOL_SIZE = 10;

    @Bean
    public Scheduler vipScheduler() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(0);
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r, "VipThreadPool-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // 设置非守护线程
                return thread;
            }
        };

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(VIP_CORE_POOL_SIZE, threadFactory);
        return Schedulers.from(scheduledExecutorService);
    }
}
