package spendreport;

import java.util.Random;
import java.util.Iterator;
/**
 * DetailedTransactionIterator - Generates random transaction data for testing
 * 
 * This iterator creates realistic transaction data by:
 * 1. Randomly selecting from predefined account IDs and zipcodes
 * 2. Generating random transaction amounts (0.0 to 1000.0)
 * 3. Incrementing timestamps to simulate chronological order
 * 4. Providing infinite iteration for continuous data generation
 * 
 * The generated data includes patterns that can trigger fraud detection:
 * - Small transactions (< $1.00) that could be followed by large ones
 * - Large transactions (>= $500.00) that could indicate fraud
 * - Geographic clustering through zipcode selection
 */
public class DetailedTransactionIterator implements Iterator<DetailedTransaction> {
    // Predefined account IDs for testing (5 different accounts)
    private static final long[] ACCOUNT_IDS = {1,2,3,4,5};
    
    // Predefined zipcodes representing different geographic locations
    private static final String[] ZIPCODES = {"01003", "02115", "78712"};

    private final Random rnd;        // Random number generator for data generation
    private long nextTimestamp;     // Timestamp for next transaction (incremented each time)

    /**
     * Constructor - initializes the iterator with current time as starting timestamp
     */
    public DetailedTransactionIterator() {
        this.rnd = new Random();
        // Start timestamp from current time (rounded to nearest second)
        this.nextTimestamp = 0;
    }

    /**
     * Check if iterator has more elements
     * 
     * @return Always returns true since this iterator generates infinite data
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Generate the next random transaction
     * 
     * This method creates a DetailedTransaction with:
     * - Random account ID from predefined list
     * - Random zipcode from predefined list  
     * - Random amount between 0.0 and 1000.0
     * - Incremented timestamp to maintain chronological order
     * 
     * @return A new DetailedTransaction with random data
     */
    @Override 
    public DetailedTransaction next() {
        // Randomly select account ID (0-4 index for 5 accounts)
        long accountId = ACCOUNT_IDS[rnd.nextInt(5)];
        
        // Randomly select zipcode (0-2 index for 3 zipcodes)
        String zipcode = ZIPCODES[rnd.nextInt(3)];
        
        // Generate random amount: (1.0 - random) * 1000.0 gives range [0.0, 1000.0)
        double amount = (1.0 - rnd.nextDouble()) * 1000.0;

        // Create transaction with generated data
        DetailedTransaction tx = new DetailedTransaction(accountId, nextTimestamp, amount, zipcode);
        
        // Increment timestamp by 1 second for next transaction
        nextTimestamp += 1;
        
        return tx;
    }
}
