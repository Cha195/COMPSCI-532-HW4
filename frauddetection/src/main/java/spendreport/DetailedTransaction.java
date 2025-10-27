package spendreport;

/**
 * DetailedTransaction - Data class representing a financial transaction
 * 
 * This class encapsulates all the information needed for fraud detection:
 * - accountId: Unique identifier for the account making the transaction
 * - timestamp: When the transaction occurred (in milliseconds since epoch)
 * - amount: The monetary value of the transaction (in dollars)
 * - zipcode: The geographic location where the transaction occurred
 * 
 * This enhanced version includes zipcode information compared to the basic
 * Transaction class, enabling more sophisticated fraud detection patterns
 * that consider geographic proximity of transactions.
 */
public class DetailedTransaction {
    private long accountId;    // Account identifier
    private long timestamp;   // Transaction timestamp in milliseconds
    private double amount;    // Transaction amount in dollars
    private String zipcode;   // Geographic location (zipcode)

    /**
     * Default constructor - required for Flink serialization
     */
    public DetailedTransaction() {}

    /**
     * Constructor to create a new DetailedTransaction with all fields
     * 
     * @param accountId The account making the transaction
     * @param timestamp When the transaction occurred
     * @param amount The monetary value of the transaction
     * @param zipcode The geographic location of the transaction
     */
    public DetailedTransaction(long accountId, long timestamp, double amount, String zipcode) {
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.zipcode = zipcode;
    }

    // Getter and setter methods for accountId
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    // Getter and setter methods for timestamp
    public long getTimestamp() { return timestamp; }
    public void  setTimestamp(long timestamp) { this.timestamp = timestamp; }

    // Getter and setter methods for amount
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    // Getter and setter methods for zipcode
    public  String getZipcode() { return zipcode; }
    public void setZipcode(String zipCode) { this.zipcode = zipCode; }
}
