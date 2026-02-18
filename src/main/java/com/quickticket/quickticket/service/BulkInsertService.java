package com.quickticket.quickticket.service;

import com.quickticket.quickticket.shared.aspects.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BulkInsertService {
    private final JobOperator jobOperator;
    private final Job BatchInsertJob;
    private final RedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 100)
    public void checkQueue() throws Exception {
        Long size = redisTemplate.opsForList().size("sync:bulk-insert-queue:ticket-issue");
        if (size == null || size == 0) return;

        this.processBulkInsert();
    }

    @DistributedLock(key = "lock:bulk-insert-queue:ticket")
    public void processBulkInsert() throws Exception {
        var params = new JobParametersBuilder().toJobParameters();
        jobOperator.start(BatchInsertJob, params);
    }
}
