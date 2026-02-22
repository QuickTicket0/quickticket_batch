package com.quickticket.quickticket.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BulkInsertBatchConfig extends DefaultBatchConfiguration {
    private final BulkInsertTasklet bulkInsertTasklet;

    @Bean
    public Job batchInsertJob(JobRepository jobRepository, Step batchInsertStep) {
        return new JobBuilder("BatchInsertJob", jobRepository)
                .start(batchInsertStep)
                .build();
    }

    @Bean
    public Step batchInsertStep(JobRepository jobRepository) {
        return new StepBuilder("BatchInsertStep", jobRepository)
                .tasklet(bulkInsertTasklet, getTransactionManager())
                .build();
    }
}
