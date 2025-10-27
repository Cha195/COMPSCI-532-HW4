package spendreport;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.walkthrough.common.source.TransactionSource;

public class DetailedFraudDetectionJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<DetailedTransaction> detailedTransactions = env.addSource(new DetailedTransactionSource()).name("detailed-transactions");

        DataStream<DetailedAlert> alerts = detailedTransactions.keyBy(DetailedTransaction::getAccountId).process(new DetailedFraudDetector()).name("detailed-fraud-detector");

        alerts.addSink(new DetailedAlertSink()).name("detailed-alert-sink");

        env.execute("Detailed Fraud Detection");
    }
}
