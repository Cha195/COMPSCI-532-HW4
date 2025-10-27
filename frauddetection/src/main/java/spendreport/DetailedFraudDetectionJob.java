package spendreport;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.walkthrough.common.source.TransactionSource;


public class DetailedFraudDetectionJob {
    public static void main(String[] args) throws Exception {
        // Create Flink stream execution environment that manages the execution of the streaming job
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create transaction data stream that generates random transaction data continuously
        DataStream<DetailedTransaction> detailedTransactions = 
            env.addSource(new DetailedTransactionSource())
                .name("detailed-transactions");

        // Apply fraud detection logic
        // Key by accountId to ensure fraud detection is per-account and partition by account for per-account state
        DataStream<DetailedAlert> detailedAlerts = 
            detailedTransactions
                .keyBy(DetailedTransaction::getAccountId)  // Partition by account for per-account state
                .process(new DetailedFraudDetector())       // Apply fraud detection logic
                .name("detailed-fraud-detector");

        // Output detailed alerts to sink that logs fraud alerts to the console
        detailedAlerts.addSink(new DetailedAlertSink())
               .name("detailed-alert-sink");

        env.execute("Detailed Fraud Detection");
    }
}
