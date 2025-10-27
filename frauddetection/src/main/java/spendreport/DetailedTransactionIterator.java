package spendreport;

import java.util.Random;
import java.util.Iterator;

public class DetailedTransactionIterator implements Iterator<DetailedTransaction> {
    private static final long[] ACCOUNT_IDS = {1,2,3,4,5};
    private static final String[] ZIPCODES = {"01003", "02115", "78712"};

    private final Random rnd;
    private long nextTimestamp;

    public DetailedTransactionIterator() {
        this.rnd = new Random();
        this.nextTimestamp = (System.currentTimeMillis() / 1000) * 1000;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override public DetailedTransaction next() {
        long accountId = ACCOUNT_IDS[rnd.nextInt(5)];
        String zipcode = ZIPCODES[rnd.nextInt(3)];
        double amount = (1.0 - rnd.nextDouble()) * 1000.0;

        DetailedTransaction tx = new DetailedTransaction(accountId, nextTimestamp, amount, zipcode);
        nextTimestamp += 1_000L;
        return tx;
    }
}
