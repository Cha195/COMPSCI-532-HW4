package spendreport;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * DetailedTransactionSource - Flink source function for generating transaction data
 * 
 * This source function simulates a stream of financial transactions by:
 * 1. Creating a DetailedTransactionIterator to generate random transaction data
 * 2. Emitting transactions at regular intervals (1 second)
 * 3. Providing proper synchronization for checkpointing
 * 4. Supporting graceful cancellation
 * 
 * The source is designed to run continuously until cancelled, making it suitable
 * for testing fraud detection algorithms with realistic data patterns.
 */
public class DetailedTransactionSource implements SourceFunction<DetailedTransaction> {
    private static final long serialVersionUID = 1L;

    // Flag to control the running state of the source
    private volatile boolean running = true;

    /**
     * Default constructor
     */
    public DetailedTransactionSource() {}

    /**
     * Main execution method - runs the source function
     * 
     * This method:
     * 1. Creates a transaction iterator to generate random data
     * 2. Continuously emits transactions while running flag is true
     * 3. Uses proper synchronization for checkpointing
     * 4. Adds a 1-second delay between transactions to simulate real-time data
     * 
     * @param ctx The source context for emitting data and checkpointing
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
            // This simulates realistic transaction timing
            Thread.sleep(1000L);
        }
    }

    /**
     * Cancel method - gracefully stops the source
     * 
     * This method is called by Flink when the job is cancelled or stopped.
     * It sets the running flag to false, which will cause the run() method
     * to exit its main loop and terminate cleanly.
     */
    public void cancel() {
        running = false;
    }
}
