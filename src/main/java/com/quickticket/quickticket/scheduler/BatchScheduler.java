@Component
@RequiredArgsConstructor
public class BatchScheduler {
    private final JobOperator jobOperator;
    private final Job TicketBatchInsertJob;
  
    @Scheduled(cron = "0 0/30 * * * *")
    public void runJob() throws Exception {
        var props = new JobParametersBuilder()
                .addLong("seatId", seat.getId())
                .toJobParameters();

        jobOperator.start(ticketAllocationJob, props);
    }
}
