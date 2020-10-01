package MacroEconomic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import MacroEconomic.DownloadResponse;

public class ScraperManager {
        public static enum DataType {
            CRUDE_OIL
        }
        private static String getStringFromDataType(DataType d) {
            switch (d) {
                case CRUDE_OIL: return "crude-oil";
            }
            return null;
        }
        
        //
    
	private SubLog log = new SubLog("Scraper Manager");
	private static String serverIp = "52.45.243.81";
	private static String paramTesting = "false";
	
	private static String macGoCommand = "/Users/algointeractive/projects/algo/go/bin/client";
	private static String windowsGoCommand = "C:\\algo-scraper\\go\\bin\\client.exe";
	
	private static String goCommand = windowsGoCommand;
	
	public static void main(String[] args) {		
		// Example
		ScraperManager manager = new ScraperManager();
		ScraperCallback<String,Double> cb = (err,value) -> {
			if (err != null) System.out.println("Error = " + err);
			System.out.println("Value = " + value);
		};
		manager.listenFor(DataType.CRUDE_OIL,cb);
		// End of Example.
	}
        
        //
	
	public void listenFor(DataType dataType, ScraperCallback<String,Double> cb) {
            String dataId = getStringFromDataType(dataType);
            log.log("Listening for data...");
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.redirectErrorStream(true);
                builder.command(goCommand,"-server",serverIp);

                Process process = builder.start();
                InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;
                String dataValue = null;


                while ( (line = reader.readLine()) != null ) {
                    log.log("Message from Socket: " + line);
                    String v = getMessageLineValue(line,dataId);
                    if (v != null) {
                        log.log("Got " + dataId);
                        dataValue = v;
                        break;
                    }
                }
	        
	        if (dataValue == null) {
                    cb.apply("No Matching Values Recieved",null);
                    return;
                }
                
	        try {
                    // Parse and pass on value.
                    Double target = Double.parseDouble(dataValue);
                    cb.apply(null, target);
	        } catch (Exception e) {
                    // Couldn't Parse
                    cb.apply("Error: Value Not a Number", null);
	        }

            } catch (IOException e) {
                cb.apply(e.toString(), null);
            }
	}
        
        private String getMessageLineValue(String line, String dataId) {
            if (line.contains(dataId)) {
                String[] lines = line.split("=");
                if (lines.length == 2) {
                    return lines[1];
                }
            }
            return null;
        }
	
        /* 
        // UNNECESSARY
	private Map<String,String> parseMessage(String msg) {
		if (msg.equals("")) return null;
		Map<String,String> map = new HashMap<String,String>();
		String[] lines = msg.split("\t");
		for (String s : lines) {
			String[] subLine = s.split("=");
			if (subLine.length != 2) continue;
			map.put(subLine[0], subLine[1]);
		}
                System.out.println(map);
		return map;
	}
        */
        
        

}
