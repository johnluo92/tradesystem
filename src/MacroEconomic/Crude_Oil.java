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
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

// RealTimeBars Class is an implementation of the
// IB API EWrapper class
public final class Crude_Oil implements EWrapper {

    // Manual Execution
    public String symbol, direction;
    int quantity, transmit_buy, transmit_sell;
    double API, filledprice, STP, PFT;
    long startTime, endTime;
    
    int counter, counter2 = 0;
    boolean bullish;
    
    // Keep track of the next ID
    private int nextOrderID;
    // The IB API Client Socket object
    private EClientSocket AlgoInteractive = null;

    @SuppressWarnings("empty-statement")
    public Crude_Oil() {
        
        try {
            FileInputStream fstream = new FileInputStream("/Users/Administrator/Desktop/Input_Oil.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            java.util.ArrayList<String> list = new java.util.ArrayList<String>();

            while ((strLine = br.readLine()) != null) {
                list.add(strLine);
            }
            symbol = list.get(0);
            System.out.println("==========>" + symbol + "<==========");
            
            quantity = Integer.parseInt(list.get(1));
            API = Double.parseDouble(list.get(2));
            STP = Double.parseDouble(list.get(3));
            PFT = Double.parseDouble(list.get(4));
            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
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

        run();
    }

    public void OilStrategy(double inventory) {
        Contract contract = new Contract();
        contract.m_localSymbol = symbol;
        contract.m_secType = "FUT";
        contract.m_exchange = "NYMEX";
        contract.m_currency = "USD";
        contract.m_multiplier = "1000";

        Order order = new Order();
        order.m_orderType = "MKT";
        order.m_totalQuantity = quantity;

        /* 
        Price of demand increases as supply is less than expected
        */
        nextOrderID += 1;
        if (inventory < API) {
            order.m_transmit = true;
            order.m_action = "BUY";
            direction = "long";
            startTime = System.currentTimeMillis();
            AlgoInteractive.placeOrder(nextOrderID, contract, order);
            System.out.println(">>>Going Long<<<");

        /* 
        Price of demand decreases as supply is more than expected
        */

    } else if (inventory > API) {
        order.m_transmit = true;
        order.m_action = "SELL";
        direction = "short";
        AlgoInteractive.placeOrder(nextOrderID, contract, order);
        System.out.println(">>>Going Short<<<");

    } else if (inventory == API) {
        System.out.println("Neutral sentiment: no orders placed");
        System.exit(0);
    }
}

public void run() {

    double value = -20;
    try {
        Thread.sleep(500);
        OilStrategy(value);
    } catch (InterruptedException ex) {
        Logger.getLogger(Try_Me.class.getName()).log(Level.SEVERE, null, ex);
    }

//test^^

//         startTime = System.currentTimeMillis();
//        ScraperCallback<String, Double> cb = (err, value) -> {
//            if (err != null) {
//                System.out.println(err);
//                return;
//            }
//            OilStrategy(value);
//            Log.getInstance().log("Main", "Got Value: " + value);
//            Log.getInstance().outputToConsole();
//        };
//        ScraperManager manager = new ScraperManager();
//        manager.listenFor(ScraperManager.DataType.CRUDE_OIL, cb);

}

public void orderStatus(int orderId, String status, double filled,
    double remaining, double avgFillPrice, int permId, int parentId,
    double lastFillPrice, int clientId, String whyHeld) {
    System.out.println("OrderStatus. Id: " + orderId + ", Status: " + status + ", Filled" + filled + ", Remaining: " + remaining
        + ", AvgFillPrice: " + avgFillPrice + ", PermId: " + permId + ", ParentId: " + parentId + ", LastFillPrice: " + lastFillPrice
        + ", ClientId: " + clientId + ", WhyHeld: " + whyHeld);
}

int h, m, s, ms;
public void getTime() {
    Calendar calendar = Calendar.getInstance();
    h = calendar.get(Calendar.HOUR);
    m = calendar.get(Calendar.MINUTE);
    s = calendar.get(Calendar.SECOND);
    ms = calendar.get(Calendar.MILLISECOND);
}

public void execDetails(int reqId, Contract contract, Execution execution) {
    endTime = System.currentTimeMillis();
    getTime();
    Log.getInstance().log("After execution: " + h + ":" + m + ":" + s + ":" + ms);
    Log.getInstance().log("Total Time: " + (endTime-startTime));
    Log.getInstance().outputToConsole();
    System.out.println("That took: " + (endTime-startTime) + "ms");
    filledprice = execution.m_price;
    System.out.println("filled price: " + filledprice);
    contract.m_localSymbol = symbol;
    contract.m_secType = "FUT";
    contract.m_exchange = "NYMEX";
    contract.m_currency = "USD";
    contract.m_multiplier = "1000";

    Order StopLoss = new Order();
    StopLoss.m_orderType = "STP";
    StopLoss.m_totalQuantity = quantity;
    Order ProfitTaking = new Order();
    ProfitTaking.m_orderType = "LMT";
    ProfitTaking.m_totalQuantity = quantity;

    switch (direction) {
        case "long":
        StopLoss.m_auxPrice = filledprice - STP;
        StopLoss.m_action = "SELL";
        ProfitTaking.m_lmtPrice = filledprice + PFT;
        ProfitTaking.m_action = "SELL";
        break;
        case "short":
        StopLoss.m_auxPrice = filledprice + STP;
        StopLoss.m_action = "BUY";
        ProfitTaking.m_lmtPrice = filledprice - PFT;
        ProfitTaking.m_action = "BUY";
        break;
        default:
        break;
    }

    nextOrderID += 1;
    AlgoInteractive.placeOrder(nextOrderID, contract, StopLoss);
    nextOrderID += 1;
    AlgoInteractive.placeOrder(nextOrderID, contract, ProfitTaking);

    try {
        Thread.sleep(500);
        System.exit(0);
    } catch (InterruptedException ex) {
        Logger.getLogger(Testing.class.getName()).log(Level.SEVERE, null, ex);
        System.exit(0);
    }
        //System.exit(0);
}


public void commissionReport(CommissionReport commissionReport) {
}


public void execDetailsEnd(int reqId) {
    System.out.println("ExecDetailsEnd. " + reqId + "\n");
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


public void position(String account, Contract contract, int pos, double avgCost) {
}


public void positionEnd() {
}


public void accountSummary(int reqId, String account, String tag, String value, String currency) {
}


public void accountSummaryEnd(int reqId) {
}


public void accountDownloadEnd(String accountName) {
}


public void openOrder(int orderId, Contract contract, Order order,
    OrderState orderState) {
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
        Crude_Oil EIA = new Crude_Oil();
    } catch (Exception e) {
    }
}
}
