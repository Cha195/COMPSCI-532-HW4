package spendreport;

import java.util.Random;
import java.util.Iterator;


public class DetailedTransactionIterator implements Iterator<DetailedTransaction> {
    // Predefined account IDs and zipcodes
    private static final long[] ACCOUNT_IDS = {1,2,3,4,5};
    private static final String[] ZIPCODES = {"01003", "02115", "78712"};

    // Random number generator for data generation to
    private final Random rnd; 
    // Timestamp for next transaction (incremented each time)
    private long nextTimestamp; 

    public DetailedTransactionIterator() {
        this.rnd = new Random();
        this.nextTimestamp = 0;
    }

    /**
     * Check if iterator has more elements; since this iterator generates infinite data, it always returns true
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Generate the next random transaction
     */
    @Override 
    public DetailedTransaction next() {
        // Randomly select account ID
        long accountId = ACCOUNT_IDS[rnd.nextInt(5)];
        
        // Randomly select zipcode
        String zipcode = ZIPCODES[rnd.nextInt(3)];
        
        // Generate random amount between 0.0 and 1000.0
        double amount = (1.0 - rnd.nextDouble()) * 1000.0;

        // Create transaction with generated data
        DetailedTransaction tx = new DetailedTransaction(accountId, nextTimestamp, amount, zipcode);
        
        // Increment timestamp by 1 second for next transaction
        nextTimestamp += 1;
        
        return tx;
    }
}
