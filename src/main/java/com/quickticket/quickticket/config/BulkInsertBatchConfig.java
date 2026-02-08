@Configuration
@AllArgsConstructor
public class BulkInsertBatchConfig extends DefaultBatchConfiguration {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job BatchInsertJob() {
        return new JobBuilder("BatchInsertJob", jobRepository)
                .start(ticketAllocationStep())
                .build();
    }

    @Bean
    public Step BatchInsertStep() {
        return new StepBuilder("BatchInsertStep", jobRepository)
                .<TicketIssueEntity, TicketIssueEntity>chunk(100)
                .reader(ticketReader())
                .processor(ticketAllocationProcessor())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public JpaPagingItemReader<TicketIssueEntity> ticketReader() {
        return new JpaPagingItemReaderBuilder<TicketIssueEntity>()
                .name("ticketReader")
                .entityManagerFactory(emf)
                .queryString("SELECT m FROM Member m WHERE m.active = true")
                .pageSize(100)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<MyEntity> ticketWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<MyEntity>()
                .dataSource(dataSource)
                .sql("INSERT INTO my_table (name, age) VALUES (:name, :age)")
                .beanMapped()
                .build();
    }
  
    @Bean
    public ItemProcessor<TicketIssueEntity, TicketIssueEntity> ticketAllocationProcessor() {
        return ticket -> {

            return ticket;
        };
}
