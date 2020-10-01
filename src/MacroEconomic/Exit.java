package MacroEconomic;

// Import Java utilities and Interactive Brokers API
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.CommissionReport;
import com.ib.client.UnderComp;
import java.util.logging.Level;
import java.util.logging.Logger;

// RealTimeBars Class is an implementation of the
// IB API EWrapper class
public final class Exit implements EWrapper {

    // Manual Execution
    String symbol = "CLX6";
    // Keep track of the next ID
    private int nextOrderID;
    // The IB API Client Socket object
    private EClientSocket AlgoInteractive = null;
    int close, counter, check = 0;

    @SuppressWarnings("empty-statement")
    public Exit() {
        // Create a new EClientSocket object
        AlgoInteractive = new EClientSocket(this);
        // Connect to the TWS or IB Gateway application
        // Leave null for localhost
        // Port Number (should match TWS/IB Gateway configuration
        AlgoInteractive.eConnect(null, 7496, 100);
        // Pause here for connection to complete
        try {
            // Thread.sleep (1000);
            while (!(AlgoInteractive.isConnected()));
        } catch (Exception e) {
        };

        try {
            handle();

            // At this point our call is done and any market data events
            // will be returned via the realtimeBar method
        } catch (InterruptedException ex) {
            Logger.getLogger(Natural_Gas.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // end RealTimeBars

    public void handle() throws InterruptedException {
        Thread.sleep(100);
        AlgoInteractive.reqPositions();

    }

    @Override
    public void position(String account, Contract contract, int pos, double avgCost) {
        contract.m_localSymbol = symbol;
        contract.m_secType = "FUT";
        contract.m_exchange = "NYMEX";
        contract.m_currency = "USD";
        contract.m_multiplier = "1000";

        Order Close_Scalp = new Order();
        Close_Scalp.m_orderType = "MKT";
        nextOrderID += 1;
        System.out.println("Positions: " + pos);

        if (pos == 0) {
            System.out.println("No positions open for " + contract.m_localSymbol);
            if (check != 1) {
                AlgoInteractive.reqOpenOrders();
                check += 1;
            }
        } else if (pos > 0 && counter != 1) {
            Close_Scalp.m_totalQuantity = pos;
            Close_Scalp.m_action = "SELL";
            AlgoInteractive.placeOrder(nextOrderID, contract, Close_Scalp);
            close = nextOrderID;
            counter++;
        } else if (pos < 0 && counter != 1) {
            Close_Scalp.m_totalQuantity = -pos;
            Close_Scalp.m_action = "BUY";
            AlgoInteractive.placeOrder(nextOrderID, contract, Close_Scalp);
            close = nextOrderID;
            counter++;
        }

    }

    public void openOrder(int orderId, Contract contract, Order order,
            OrderState orderState) {
        System.out.println("OpenOrder. ID: " + orderId);
        System.out.println("OrderState: " + orderState.m_status);
        if ((orderState.m_status.equalsIgnoreCase("presubmitted")
                || orderState.m_status.equalsIgnoreCase("submitted")) && (orderId != close)) {
            AlgoInteractive.cancelOrder(orderId);
        } else {
            System.exit(0);
        }
//        System.exit(0);
    }

    public void orderStatus(int orderId, String status, double filled,
            double remaining, double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld) {
        System.out.println("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled" + filled + ", Remaining: " + remaining
                + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId + ", ParentId: " + parentId + ", LastFillPrice: " + lastFillPrice
                + ", ClientId: " + clientId + ", WhyHeld: " + whyHeld);
    }

    public void execDetails(int reqId, Contract contract, Execution execution) {
    }

    public void commissionReport(CommissionReport commissionReport) {
    }

    public void execDetailsEnd(int reqId) {
    }

    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    public void contractDetails(int reqId, ContractDetails contractDetails) {
    }

    public void contractDetailsEnd(int reqId) {
    }

    public void fundamentalData(int reqId, String data) {
    }

    public void currentTime(long time) {
    }

    public void displayGroupList(int requestId, String contraftInfo) {
    }

    public void displayGroupUpdated(int requestId, String contractInfo) {
    }

    public void verifyCompleted(boolean completed, String contractInfo) {
    }

    public void verifyMessageAPI(String message) {
    }

    public void historicalData(int reqId, String date, double open,
            double high, double low, double close, int volume, int count,
            double WAP, boolean hasGaps) {
    }

    public void managedAccounts(String accountsList) {
    }

    public void positionEnd() {
    }

    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
    }

    public void accountSummaryEnd(int reqId) {
    }

    public void accountDownloadEnd(String accountName) {
    }

    public void openOrderEnd() {
    }

    public void orderStatus(int orderId, String status, int filled,
            int remaining, double avgFillPrice, int permId, int parentId,
            double lastFillPrice, int clientId, String whyHeld) {
    }

    public void receiveFA(int faDataType, String xml) {
    }

    public void scannerData(int reqId, int rank,
            ContractDetails contractDetails, String distance, String benchmark,
            String projection, String legsStr) {
    }

    public void scannerDataEnd(int reqId) {
    }

    public void scannerParameters(String xml) {
    }

    public void tickEFP(int symbolId, int tickType, double basisPoints,
            String formattedBasisPoints, double impliedFuture, int holdDays,
            String futureExpiry, double dividendImpact, double dividendsToExpiry) {
    }

    public void tickGeneric(int symbolId, int tickType, double value) {
    }

    public void tickOptionComputation(int tickerId, int field,
            double impliedVol, double delta, double optPrice,
            double pvDividend, double gamma, double vega,
            double theta, double undPrice) {
    }

    public void deltaNeutralValidation(int reqId, UnderComp underComp) {
    }

    public void updateAccountTime(String timeStamp) {
    }

    public void updateAccountValue(String key, String value, String currency,
            String accountName) {
    }

    public void updateMktDepth(int symbolId, int position, int operation,
            int side, double price, int size) {
    }

    public void updateMktDepthL2(int symbolId, int position,
            String marketMaker, int operation, int side, double price, int size) {
    }

    public void updateNewsBulletin(int msgId, int msgType, String message,
            String origExchange) {
    }

    public void updatePortfolio(Contract contract, int position,
            double marketPrice, double marketValue, double averageCost,
            double unrealizedPNL, double realizedPNL, String accountName) {
    }

    public void marketDataType(int reqId, int marketDataType) {
    }

    public void tickSnapshotEnd(int tickerId) {
    }

    public void connectionClosed() {
    }

    public void realtimeBar(int reqId, long time, double open, double high,
            double low, double close, long volume, double wap, int count) {
        // Display the Real-Time bar
        // reqId is the integer specified as the first parameter to reqRealTimeBars()
        try {
            System.out.println("realtimeBar:" + time + "," + open + ","
                    + high + "," + low + "," + close + ","
                    + volume);
        } catch (Exception e) {
        }
    }

    public void error(Exception e) {
    }

    public void error(String str) {
        // Print out the error message
        System.out.println(str);
    }

    public void error(int id, int errorCode, String errorMsg) {
        // Overloaded error event (from IB) with their own error 
        // codes and messages
        System.out.println("[Message]: " + id + "," + errorCode + "," + errorMsg);
    }

    public void nextValidId(int orderId) {
        // Return the next valid OrderID
        nextOrderID = orderId;
    }

    public void tickPrice(int orderId, int field, double price,
            int canAutoExecute) {

    }

    public void tickSize(int orderId, int field, int size) {
    }

    public void tickString(int orderId, int tickType, String value) {
    }

    public static void main(String args[]) {
        try {
            // Create an instance
            // At this time a connection will be made
            // and the request for market data will happen
            Exit owl = new Exit();
        } catch (Exception e) {
        }
    }
}
