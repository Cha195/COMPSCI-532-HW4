package spendreport;

public final class DetailedAlert {
    private DetailedTransaction tx;

    public DetailedAlert() {}

    public DetailedAlert(DetailedTransaction tx) {
        this.tx = tx;
    }

    public DetailedTransaction getTx() { return tx; }
    public void setTx(DetailedTransaction tx) { this.tx = tx; }
}
