package spendreport;

import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PublicEvolving
public class DetailedAlertSink implements SinkFunction<DetailedAlert> {
    private static final Logger LOG = LoggerFactory.getLogger(DetailedAlertSink.class);

    public void invoke(DetailedAlert value, SinkFunction.Context context) {
        DetailedTransaction tx = value.getTx();
        LOG.info("DetailedAlert accountId={} timestamp={} zipcode={} amount={}", tx.getAccountId(), tx.getTimestamp(), tx.getZipcode(), tx.getAmount());
    }
}