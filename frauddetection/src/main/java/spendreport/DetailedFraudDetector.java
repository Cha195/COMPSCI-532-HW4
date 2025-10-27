package spendreport;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {
    private static final long serialVersionUID = 1L;

    private static final double SMALL_THRESHOLD = 1.0;
    private static final double LARGE_THRESHOLD = 500.0;

    private transient ValueState<DetailedTransaction> smallTxState;
    private transient ValueState<Long> timerState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<DetailedTransaction> smallTxStateDescriptor = new ValueStateDescriptor<>("lastSmallTx", DetailedTransaction.class);
        smallTxState = getRuntimeContext().getState(smallTxStateDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
                "timer-state",
                Types.LONG);
        timerState = getRuntimeContext().getState(timerDescriptor);
    }

    @Override
    public void processElement(DetailedTransaction tx, Context ctx, Collector<DetailedAlert> out) throws Exception {
        final double txAmount = tx.getAmount();

        if(txAmount < SMALL_THRESHOLD){
            smallTxState.update(tx);
            registerTimer(ctx);
            return;
        }

        if(txAmount >= LARGE_THRESHOLD) {
            DetailedTransaction smallTx = smallTxState.value();
            if(smallTx != null) {
                boolean sameZipcode = smallTx.getZipcode() == tx.getZipcode();
                if(sameZipcode) {
                    out.collect(new DetailedAlert());
                }

                cleanUp(ctx);
            }
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) {
        cleanUp(ctx);
    }

    private void registerTimer(Context ctx) {
        Long prevTimer = timerState.value();
        if(prevTimer != null) {
            ctx.timerService().deleteProcessingTimeTimer(prevTimer);
        }

        long newTimerState = ctx.timerService().currentProcessingTime() + ONE_MINUTE;
        ctx.timerService().registerProcessingTimeTimer(newTimerState);
        timerState.update(newTimerState);
    }

    private void cleanUp(Context ctx) throws Exception {
        Long timer = timerState.value();
        if(timer != null) {
            ctx.timerService().deleteProcessingTimeTimer(timer);
            timerState.clear();
        }

        smallTxState.clear();
    }
}
