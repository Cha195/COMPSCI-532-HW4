package spendreport;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.walkthrough.common.source.TransactionSource;

/**
 * DetailedFraudDetectionJob - Main Flink job for enhanced fraud detection
 * 
 * This job implements a complete fraud detection pipeline using Apache Flink:
 * 1. Creates a stream execution environment
 * 2. Adds a source that generates detailed transaction data
 * 3. Applies fraud detection logic using DetailedFraudDetector
 * 4. Outputs alerts to a sink for logging/monitoring
 * 
 * The job uses the enhanced DetailedFraudDetector which considers zipcode
 * matching in addition to transaction amounts for more sophisticated fraud detection.
 * 
 * Key differences from basic FraudDetectionJob:
 * - Uses DetailedTransaction with zipcode information
 * - Uses DetailedFraudDetector with zipcode-based fraud patterns
 * - Uses DetailedAlertSink for enhanced alert logging
 */
public class DetailedFraudDetectionJob {
    
    /**
     * Main entry point for the detailed fraud detection job
     * 
     * This method sets up and executes the complete fraud detection pipeline:
     * 1. Transaction Source -> 2. Fraud Detection -> 3. Alert Sink
     * 
     * @param args Command line arguments (not used in this implementation)
     * @throws Exception If job execution fails
     */
    public static void main(String[] args) throws Exception {
        // Create Flink stream execution environment
        // This manages the execution of the streaming job
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Step 1: Create transaction data stream
        // The DetailedTransactionSource generates random transaction data continuously
        DataStream<DetailedTransaction> detailedTransactions = 
            env.addSource(new DetailedTransactionSource())
                .name("detailed-transactions");

        // Step 2: Apply fraud detection logic
        // Key by accountId to ensure fraud detection is per-account
        // Process each transaction through DetailedFraudDetector
        DataStream<DetailedAlert> alerts = 
            detailedTransactions
                .keyBy(DetailedTransaction::getAccountId)  // Partition by account for per-account state
                .process(new DetailedFraudDetector())       // Apply fraud detection logic
                .name("detailed-fraud-detector");

        // Step 3: Output alerts to sink
        // The DetailedAlertSink logs fraud alerts to the console
        alerts.addSink(new DetailedAlertSink())
               .name("detailed-alert-sink");

        // Execute the job with a descriptive name
        env.execute("Detailed Fraud Detection");
    }
}
