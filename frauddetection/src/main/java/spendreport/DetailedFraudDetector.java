package spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;


public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {
    private static final long serialVersionUID = 1L;

    // Threshold amounts for fraud detection
    private static final double SMALL_THRESHOLD = 10.0;
    private static final double LARGE_THRESHOLD = 500.0;
    private static final long ONE_MINUTE = 60 * 1000;

    // State variables to track per-account information
    private transient ValueState<DetailedTransaction> smallTxState; // Stores the last small transaction that occurred for the same account in the last minute
    private transient ValueState<Long> timerState;                  // Stores the timer timestamp

    /**
     * Initialize state descriptors in the open lifecycle hook
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
     */
    @Override
    public void processElement(DetailedTransaction tx, Context ctx, Collector<DetailedAlert> out) throws Exception {
        final double txAmount = tx.getAmount();

        // Small transaction detected; store it in the state andregister a timer for 1min
        if(txAmount < SMALL_THRESHOLD){
            smallTxState.update(tx);
            registerTimer(ctx);
            return;
        }

        // Large transaction detected; check for fraud pattern
        if(txAmount >= LARGE_THRESHOLD) {
            DetailedTransaction smallTx = smallTxState.value();
            
            // Only proceed if we have a small transaction in the state for the same account
            if(smallTx != null) {
                // Check if both transactions occurred in the same zipcode
                boolean sameZipcode = smallTx.getZipcode().equals(tx.getZipcode());
                
                if(sameZipcode) {
                    // Create alert with the current transaction details
                    DetailedAlert alert = new DetailedAlert(tx);
                    out.collect(alert);
                    
                    // Clean up state only after fraud is detected
                    // The small transaction remains valid for future large transactions in the same zipcode
                    // This prevents multiple alerts for the same fraud pattern
                    cleanUp(ctx);
                }
            }
        }
    }

    /**
     * Timer callback method called when the cleanup timer expires
     */
    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) throws Exception {
        cleanUp(ctx);
    }

    /**
     * Register a timer for 1min
     */
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
