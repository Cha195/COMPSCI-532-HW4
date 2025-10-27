package spendreport;


public final class DetailedAlert {
    private DetailedTransaction tx;

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

    public DetailedTransaction getTx() { return tx; }
    public void setTx(DetailedTransaction tx) { this.tx = tx; }
}
