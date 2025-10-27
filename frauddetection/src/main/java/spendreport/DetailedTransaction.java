package spendreport;


public class DetailedTransaction {
    private long accountId;
    private long timestamp;
    private double amount;
    private String zipcode;

    /**
     * Default constructor - required for Flink serialization
     */
    public DetailedTransaction() {}

    /**
     * Constructor to create a new DetailedTransaction with all fields
     */
    public DetailedTransaction(long accountId, long timestamp, double amount, String zipcode) {
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.zipcode = zipcode;
    }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public long getTimestamp() { return timestamp; }
    public void  setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public  String getZipcode() { return zipcode; }
    public void setZipcode(String zipCode) { this.zipcode = zipCode; }
}
