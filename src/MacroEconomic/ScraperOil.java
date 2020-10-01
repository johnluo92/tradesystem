package MacroEconomic;


import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

//import org.asynchttpclient.*;



import java.util.concurrent.Future;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScraperOil {
	public ScraperCallback<String,Double> callback;
	private SubLog log = new  SubLog("Java Scraper");
	private Semaphore semaphore = new Semaphore(1);
	private int requestCounter = 0;
	private boolean gotData = false;
	private int requestID = 0;
	
	private int getRequestID() {
		requestID ++;
		return requestID;
	}
//	private static String url = "http://ir.eia.gov/ngs/ngs.html";
	private static String url = "http://ir.eia.gov/wpsr/table1.csv";
//	private static String url = "http://localhost:4321";
	private static String byteRange = "bytes=0-170";
	
	public ScraperOil(TimeObject time, ScraperCallback<String,Double> cb) {
                initUnirest();
		callback = cb;
		startAt(time, ()->{
			scrape();
		});
	}
	private void startAt(TimeObject time, Runnable r) {
		System.out.println("Will Scrape at: " + time.hour + "h " + time.minute+"m");
		Date d = dateFromTime(time);
                long delay = 0;
		Long future = d.getTime();
		Long now = new Date().getTime();
		Long offset = future - now + delay;
		if (offset < 0) offset = 0L;
		
		
		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
		executor.schedule(new Runnable() {
		  public void run() {
		    r.run();
		  }
		}, offset, TimeUnit.MILLISECONDS);
		executor.shutdown();
//		Timer t = new Timer();
//		final TimerTask task = new TimerTask() { public void run() { r.run(); }};
//		t.schedule(task, offset);
	}
	/* SCRAPER METHODS */
	public void scrape () {
		System.out.println("Scraping at: ");
		final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);
		int[] delays = { 0,10,30 };
		
		for (int delay : delays) {
			executor.schedule(new Runnable() {
			  @Override
			  public void run() {
			    fireRequest();
//				  System.out.println("Thread");
			  }
			}, delay, TimeUnit.MILLISECONDS);
		}
		
		executor.shutdown();
	}
	private void fireRequest () {
		int id = getRequestID();
		int retryDelay = 10;
		if (gotData) return;
		log.log("Firing request: #" + id);
		Runnable retryRequest = () -> {
			final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
			executor.schedule(new Runnable() {
			  public void run() {
				  requestCounter--;
				  if (gotData && requestCounter == 0) {
					  try {
						Unirest.shutdown();
					} catch (IOException e) {
						e.printStackTrace();
					}
				  }
				  if (requestCounter <= 1) {
					  fireRequest(); 
				  }
			  }
			}, retryDelay, TimeUnit.MILLISECONDS);
			executor.shutdown();
			log.log("Retrying #" + id + " in "+retryDelay+"ms");
		};
		boolean retry = false;
		requestCounter ++;
		
		//// Download file
		DownloadCallback cb = (res) -> {
			if (res.error != null) {
				log.log("#"+id+" , "+res.error);
				retryRequest.run();
				return;
			}
			Double value = parseCsv(res.data);
			if (value == null) {
				log.log("ID #" + id + "  , Parse Error");
				retryRequest.run();
				return;
			}
			requestCounter--;
			done(null,value,id);
		};
		unirestTest(url,byteRange,cb);
		
	}
	
	/* HELPERS */	
	private void done (String err, Double val, int id) {
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			System.out.println("Can't Block Threads with Semaphore");
		}
		if (gotData) return;
		gotData = true;
		System.out.println("DONE at Request #" + id);
		if (callback != null) callback.apply(err,val);
		callback = null;
		semaphore.release();
	}
	private Double parseCsv (String csv) { 
//                if (true) return 0.7;
		if (csv == null) return null;
		String[] lines = csv.split("\n");
		String[] crudeOilLine = lines[1].split("\",\"");
		String difference = crudeOilLine[3];
		return Double.parseDouble(difference);
	}
	private Date dateFromTime (TimeObject time) {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.setLenient(true);
		c.set(Calendar.HOUR_OF_DAY, time.hour);
		c.set(Calendar.MINUTE, time.minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
	private static DownloadResponse downloadPartialFile(String urlString,String byteRange) {
		// Return File, return request
		String err = null;
		String data = "";
        URL url = null;
		try {
			url = new URL(urlString);
		} catch (MalformedURLException e) {
			err = "ERROR: Malformed URL";
			return new DownloadResponse(err,data);
		}
        HttpURLConnection urlConnection;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			err = "ERROR: HTTP Request Error";
			return new DownloadResponse(err,data);
		}
        urlConnection.setRequestProperty("Range", byteRange);
        try {
			urlConnection.connect();
		} catch (IOException e) {
			err = "ERROR: Url Connect Error";
			return new DownloadResponse(err,data);
		}
        int code;
		try {
			code = urlConnection.getResponseCode();
		} catch (IOException e) {
			err = "ERROR: Can't Get Status Code";
			return new DownloadResponse(err,data);
		}
        if (code < 200 || code >= 300) {
        	err = "ERROR: Wrong Status Code " + code;
			return new DownloadResponse(err,data);
        }
        InputStream inputStream;
		try {
			inputStream = urlConnection.getInputStream();
		} catch (IOException e) {
			err = "ERROR: Input Stream Error";
			return new DownloadResponse(err,data);
		}
        int i;
        try {
			while((i=inputStream.read())!=-1) {
				data += (char)i; 
			}
		} catch (IOException e) {
			err = "ERROR: Can't Read Input Stream";
			return new DownloadResponse(err,data);
		}
        if (data.equals("")) {
        	err = "ERROR: Empty Data";
			return new DownloadResponse(err,data);
        }
        urlConnection.disconnect();
        return new DownloadResponse(err,data);
    }
	
	public static void unirestTest(String the_url, String byteRange, DownloadCallback cb) {
		long time = System.currentTimeMillis();
		Future<HttpResponse<String>> d = Unirest.get(the_url)
			.header("Range", byteRange)
			.asStringAsync(new Callback<String>() {
			    
				public void failed(UnirestException e) {
					//System.out.println("The request has failed: " + the_url);
					if (cb == null) return;
					DownloadResponse res = new DownloadResponse("Request Error",null);
					cb.apply(res);
			    }
			
			    public void completed(HttpResponse<String> response) {
			    	if (cb == null) return;
			    	int code = response.getStatus();
			    	String body = response.getBody();
			    	DownloadResponse res = new DownloadResponse(null,body);
			    	if (code < 200 || code >= 300) {
			    		res.error = "Wrong Status Code: " + code;
			    	}
			    	if (body == null || body.equals("")) {
			    		res.error = "Empty Response Body";
			    	}
                                //long now = System.currentTimeMillis();
			        //System.out.println( (now-time) + "ms: \t" + the_url);
			    	cb.apply(res);
			        
			    }
			
			    public void cancelled() {
			        System.out.println("The request has been cancelled");
			    }
			
			});
	}
	
	public static void initUnirest() {
		unirestTest("http://google.com","",null);
	}
	
	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//                 long t1 = System.nanoTime();
//                String d = downloadPartialFile(url,byteRange).data;
//                long tf = (System.nanoTime() - t1)/1000000;
//                System.out.println(tf+"ms");
                
                initUnirest();
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
		
		
		
		SubLog log = new SubLog("Main Scraper");
		ScraperCallback<String,Double> cb = (err,value) -> {
			log.log("Value = " + value);
			if (err != null) log.log("Error = " + err);
			System.out.println("----------------");
			System.out.println("-------LOGS-----");
			log.outputToConsole();
                     try {
                         Unirest.shutdown();
                     } catch (IOException ex) {
                         Logger.getLogger(ScraperOil.class.getName()).log(Level.SEVERE, null, ex);
                     }
		};
		ScraperOil scraper = new ScraperOil(new TimeObject(10,30),cb);
		
               
	}
}
