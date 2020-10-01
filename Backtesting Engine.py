import pandas as pd
import datetime

def initialize(context):
    ## csv file: Oil_Inventory.csv
    
    ## https://copy.com/Gob42bHwCsUcaEyg (actual vs forecast)
    ## https://copy.com/lq9r2xGVljBg1PPJ (actual vs previous)
    ## https://copy.com/h1AM3lsCqC3bUgCI (actual vs forecast) original
    fetch_csv("https://copy.com/Gob42bHwCsUcaEyg",
        pre_func=preview,
        date_column='date',
        date_format='%m/%d/%y',
        universe_func=my_universe)
    
    set_slippage(slippage.FixedSlippage(spread=0))
    schedule_function(close_all_positions, date_rules.every_day(), time_rules.market_close())
    
    context.stop_price = 0
    
    ## fixed trailing stop percentage
    context.stop_pct = 0.9955
    ## fixed take profit percentage
    context.take_prft = 1.0325
    
def my_universe(context, fetcher_data):
    my_stocks = set(fetcher_data['sid'])
    context.count = len(my_stocks)
    #print 'total universe size: {c}'.format(c=context.count)
    return my_stocks
def preview(df):
    # log.info(' %s ' % df.head())
    fdates = list(pd.DatetimeIndex(df['date'].values))
    fdates.sort()
    idx = pd.date_range(fdates[0], fdates[-1] + datetime.timedelta(days=1))
    missing_dates = [x for x in idx if x not in fdates]
    for date in missing_dates:
        dr = {}
        dr['date'] = date.strftime('%m/%d/%y')
        dr['symbol'] = 'AAPL'
        dr['score'] = '0'
        row = pd.DataFrame([dr])
        df = pd.concat([df, row], ignore_index=True)
    log.info(df)
    
    return df
def handle_data(context, data):
    exchange_time = pd.Timestamp(get_datetime()).tz_convert('US/Eastern')
    hour = exchange_time.hour
    minute = exchange_time.minute
    cash = context.portfolio.cash
    
    for stock in data:
        
        if has_orders(data):
                print('has open orders - doing nothing!')
                return
        
        if stock.symbol != 'AAPL':
            
            ############################################ OPEN
            if stock not in context.portfolio.positions:
                if data[stock]['dt'].date() == exchange_time.date():
                    if hour == 10 and minute == 29:
                        
                        if 'price' in data[stock]:
                            
                            # Open Long
                            if data[stock]['score'] == 1:
                                weight = data[stock]['score']
                                price = data[stock].price
                                if cash > price:
                                    order_target_percent(stock, weight)
                                    print "Bullish oil: %s at %s" % (stock.symbol, get_datetime().tz_convert('US/Eastern'))
                                    
                            ## Open Short        
                            elif data[stock]['score'] == -1:
                                weight = data[stock]['score']
                                price = data[stock].price
                                if cash > price:
                                    order_target_percent(stock, weight*-1)
                                    print "Bearish oil: %s at %s" % (stock.symbol, get_datetime().tz_convert('US/Eastern'))
                            elif data[stock]['score'] == 0:
                                return
                        else:
                            log.warn("No price for {s}".format(s=stock))
                        
            ############################################ CLOSE            
            elif stock in context.portfolio.positions:
                amount = context.portfolio.positions[stock].amount
                original = context.portfolio.positions[stock].cost_basis
                price = data[stock].price 
                ## updating trailing stop price (minutely)
                context.stop_price = max(context.stop_price, context.stop_pct * price)
                
                ### Close long
                if amount > 0:
                    
                    ## trailing stop loss
                    if price <= context.stop_price:
                        order_target(stock, 0)
                        context.stop_price = 0
                        record(price=data[stock].price, stop=context.stop_price)
                        print "Exited on trailing at %s at %s" % (stock.symbol, price)
                        
                    ## take profit percentage
                    elif price >= (context.take_prft*original):
                        order_target(stock, 0)
                        print "Exited on take profit at %s at %s" % (stock.symbol, price)
                        

def close_all_positions(context, data):
    for security in context.portfolio.positions:
        order_target_percent(security, 0)
        print "Closed %s of %s at %s" % (str(context.portfolio.positions[security].amount), security.symbol, get_datetime().tz_convert('US/Eastern'))
    
                
def has_orders(data):
    open_orders = get_open_orders()
    if open_orders:
        for sec in open_orders:  
            for oo in open_orders[sec]:
                message = 'Open order for {amount} shares in {stock}'  
                message = message.format(amount=oo.amount, stock=sec)  
                log.info(message)
        return True
    return False

def set_trailing_stop(context, data):
    if context.portfolio.positions[stock].amount:
        price = data[stock].price
        context.stop_price = max(context.stop_price, context.stop_pct * price)