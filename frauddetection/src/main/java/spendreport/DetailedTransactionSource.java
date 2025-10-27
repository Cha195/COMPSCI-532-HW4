package spendreport;

import org.apache.flink.streaming.api.functions.source.SourceFunction;


public class DetailedTransactionSource implements SourceFunction<DetailedTransaction> {
    private static final long serialVersionUID = 1L;

    // Flag to control the running state of the source
    private volatile boolean running = true;

    public DetailedTransactionSource() {}

    /**
     * Runs the source function
     */
    @Override
    public void run(SourceContext<DetailedTransaction> ctx) throws Exception {
        // Create iterator to generate random transaction data
        DetailedTransactionIterator it = new DetailedTransactionIterator();

        // Continuously emit transactions until cancelled
        while(running) {
            // Generate next transaction
            DetailedTransaction tx = it.next();
            
            // Synchronize on checkpoint lock to ensure proper checkpointing
            synchronized (ctx.getCheckpointLock()) {
                ctx.collect(tx);
            }
            
            // Wait 1 second before generating next transaction
            Thread.sleep(1000L);
        }
    }

    public void cancel() {
        running = false;
    }
}
