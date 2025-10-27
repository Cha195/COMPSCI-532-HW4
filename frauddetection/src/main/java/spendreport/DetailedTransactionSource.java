package spendreport;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

public class DetailedTransactionSource implements SourceFunction<DetailedTransaction> {
    private static final long serialVersionUID = 1L;

    private volatile boolean running = true;

    public DetailedTransactionSource() {}

    @Override
    public void run(SourceContext<DetailedTransaction> ctx) throws Exception {
        DetailedTransactionIterator it = new DetailedTransactionIterator();

        while(running) {
            DetailedTransaction tx = it.next();
            synchronized (ctx.getCheckpointLock()) {
                ctx.collect(tx);
            }
            Thread.sleep(1000L);
        }
    }

    public void cancel() {
        running = false;
    }
}
