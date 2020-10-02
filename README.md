# tradesystem
Automated trading system using Interactive Brokers API to place event-driven positions

Link to published project: https://www.quantopian.com/posts/high-sharpe-weekly-strategy

A fully functional automated trading system designed to trade around weekly EIA crude oil production output event.

This project contains two parts.

1st part: The backtesting engine written in third party Quantopian's Python IDE for the the purpose of gauging returns performance and relevant metrics. The engine uses a fetcher to grab a prepopulated csv delimited file containing the direction of a trade to be placed on Wednesdays during crude oil supply output release from the Energy Information Agency (EIA). The EIA production number is then compared against the American Petroleum Institute (API*)'s actual production output. If the de facto EIA's supply production is more than the API*'s number, then the supply count was underestimated and there is more supply than expected. In this case, the algorithm would place a short on the current front month crude oil futures, expecting it lower in price due to an "abundant" supply of the crude oil commodity. In the event that the EIA number is less than the API* number, the algorithm places a long trade expecting the price of crude oil to rise due to a supply "shortage".

2nd part: The live trade system, built using Java in NetBeans interfacing with Interactive Brokers's Trader Workstation (TWS) application programming interface (API), essentially performs the weekly trade in a real event driven environment. A critical component of the live system contains a technology customized to run multiple instances of web scrapers to grab the EIA output number at exactly 10:30 AM on Wednesday. This sends the first successfully scraped value to the DataListener class that immediately places an order to long/short the defined front month Crude Oil contract given there is a connection between the client application to TWS. The ordering syntax contains defined criteras such as quantity, type of order, etc. and also importantly a stop-loss limit order (and profit taking) attached to the trade.

A separate shell script was written to call for the orders to be exited for either a loss or profit, allowing the user to decide at the time of the event.
