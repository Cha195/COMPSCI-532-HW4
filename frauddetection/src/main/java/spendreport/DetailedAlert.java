package spendreport;


public final class DetailedAlert {
    private DetailedTransaction tx;  // The transaction that triggered this alert

    /**
     * Default constructor - required for Flink serialization
     * Note: This leaves tx as null - ensure setTx() is called before use
     */
    public DetailedAlert() {}

    /**
     * Constructor to create a new DetailedAlert with transaction information
     */
    public DetailedAlert(DetailedTransaction tx) {
        this.tx = tx;
    }

    // Getter and setter methods for the transaction
    public DetailedTransaction getTx() { return tx; }
    public void setTx(DetailedTransaction tx) { this.tx = tx; }
}
