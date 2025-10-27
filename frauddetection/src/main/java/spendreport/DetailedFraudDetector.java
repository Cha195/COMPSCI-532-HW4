package spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * DetailedFraudDetector - Enhanced fraud detection that considers zipcode matching
 * 
 * This detector implements a more sophisticated fraud detection algorithm that:
 * 1. Tracks small transactions (< $1.00) per account
 * 2. Monitors for large transactions (>= $500.00) that occur after small transactions
 * 3. Only triggers alerts if both transactions occur in the same zipcode
 * 4. Uses timers to automatically clean up state after 1 minute
 * 
 * The fraud pattern detected: Small transaction followed by large transaction in same zipcode
 * This could indicate account testing before a large fraudulent transaction.
 */
public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {
    private static final long serialVersionUID = 1L;

    // Threshold amounts for fraud detection
    private static final double SMALL_THRESHOLD = 10.0;   // Transactions under $10.00 are considered "small" (increased from $1.00)
    private static final double LARGE_THRESHOLD = 200.0;  // Transactions >= $200.00 are considered "large" (decreased from $500.00)
    private static final long ONE_MINUTE = 60 * 1000;     // Timer duration: 1 minute in milliseconds

    // State variables to track per-account information
    private transient ValueState<DetailedTransaction> smallTxState; // Stores the last small transaction
    private transient ValueState<Long> timerState;                  // Stores the timer timestamp

    /**
     * Initialize state descriptors when the function is opened
     * This method is called once per parallel instance of the function
     */
    @Override
    public void open(Configuration parameters) {
        // Create state descriptor for storing the last small transaction per account
        ValueStateDescriptor<DetailedTransaction> smallTxStateDescriptor = 
            new ValueStateDescriptor<>("lastSmallTx", DetailedTransaction.class);
        smallTxState = getRuntimeContext().getState(smallTxStateDescriptor);

        // Create state descriptor for storing timer timestamps per account
        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                Types.LONG);
        timerState = getRuntimeContext().getState(timerDescriptor);
    }

    /**
     * Process each incoming transaction to detect potential fraud patterns
     * 
     * Fraud Detection Logic:
     * 1. If transaction amount < SMALL_THRESHOLD: Store transaction and set timer
     * 2. If transaction amount >= LARGE_THRESHOLD: Check if previous small transaction exists
     *    and if both transactions are from the same zipcode
     * 3. If fraud pattern detected: Generate alert and clean up state
     * 
     * @param tx The incoming transaction to process
     * @param ctx The context providing access to timer service and current processing time
     * @param out The collector to emit fraud alerts
     */
    @Override
    public void processElement(DetailedTransaction tx, Context ctx, Collector<DetailedAlert> out) throws Exception {
        final double txAmount = tx.getAmount();

        // Case 1: Small transaction detected - store it and set cleanup timer
        if(txAmount < SMALL_THRESHOLD){
            // Store this small transaction for potential fraud detection
            smallTxState.update(tx);
            // Register a timer to clean up state after 1 minute
            registerTimer(ctx);
            return;
        }

        // Case 2: Large transaction detected - check for fraud pattern
        if(txAmount >= LARGE_THRESHOLD) {
            // Get the previously stored small transaction for this account
            DetailedTransaction smallTx = smallTxState.value();
            
            // Only proceed if we have a small transaction on record
            if(smallTx != null) {
                // Check if both transactions occurred in the same zipcode
                boolean sameZipcode = smallTx.getZipcode().equals(tx.getZipcode());
                
                if(sameZipcode) {
                    // FRAUD DETECTED: Small transaction followed by large transaction in same zipcode
                    // Create alert with the current transaction details
                    DetailedAlert alert = new DetailedAlert(tx);
                    out.collect(alert);
                }

                // Clean up state regardless of whether fraud was detected
                // This ensures we don't generate multiple alerts for the same pattern
                cleanUp(ctx);
            }
        }
    }

    /**
     * Timer callback method - called when the cleanup timer expires
     * This ensures that state is automatically cleaned up after 1 minute
     * to prevent memory leaks and stale fraud detection patterns
     * 
     * @param timestamp The timestamp when the timer was triggered
     * @param ctx The timer context
     * @param out The collector (not used in this method)
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) throws Exception {
        cleanUp(ctx);
    }

    private void registerTimer(Context ctx) throws Exception {
        Long prevTimer = timerState.value();
        if(prevTimer != null) {
            ctx.timerService().deleteProcessingTimeTimer(prevTimer);
        }

        // Register new timer: current time + 1 minute
        long newTimerState = ctx.timerService().currentProcessingTime() + ONE_MINUTE;
        ctx.timerService().registerProcessingTimeTimer(newTimerState);
        timerState.update(newTimerState);
    }

    /**
     * Clean up all state and timers for the current account
     * This method is called when:
     * 1. A fraud pattern is detected and alert is generated
     * 2. The cleanup timer expires
     * 
     * @param ctx The context providing access to timer service
     */
    private void cleanUp(Context ctx) throws Exception {
        // Delete the processing time timer if it exists
        Long timer = timerState.value();
        if(timer != null) {
            ctx.timerService().deleteProcessingTimeTimer(timer);
            timerState.clear();
        }

        // Clear the small transaction state
        smallTxState.clear();
    }
}
