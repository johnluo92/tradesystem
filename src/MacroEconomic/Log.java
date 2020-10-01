/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MacroEconomic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Log {
	private static Log instance = null;
	
	private ArrayList<Object[]> list = new ArrayList<Object[]>();
	private String dir = "logs/";
	private String fileExtension = ".log";
	
	private Log() { 
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        if (instance != null) {
                            instance.outputToConsole();
                            instance.log("End.");
                        }
                        
                    } catch (Exception exp) {

                    }
                }
            }); 
        }
	public static Log getInstance() {
		if (instance == null) {
			instance = new Log();
		}
		return instance;
	}
	
	public void log (String sourceName, String msg) {
		Long time = System.currentTimeMillis();
		instance.list.add( new Object[]{ sourceName, time, msg } );
                System.out.println(msg);
	}
	
	public void log (String msg) {
		log("",msg);
	}
	
	public void logPrint(String msg) {
		
	}
	
	private String processList () {
		String output = "";
		for (Object[] log : instance.list) {
			String sourceName = (String)log[0];
			Long time = (long)log[1];
			String msg = (String)log[2];
			Date date = new Date(time);
			DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
			String dateString = formatter.format(date);
			
			output += sourceName + " \t " + dateString + ": \t" + msg + "\n";
		}
		return output;
	}
	
	public void outputToFile () {
//		DateFormat formatter = new SimpleDateFormat("E @ HH'h'mm'm'ss.SSS's' (MMM dd,yyyy)");
		DateFormat formatter = new SimpleDateFormat("E, MMM dd,yyyy");		
		String filename = formatter.format(new Date());
		try {
			String content = processList();
			File file = new File(instance.dir + filename + instance.fileExtension);
			File dirPath = new File(instance.dir);
			boolean appendMode = false;
			// if file doesnt exists, then create it
			if (!dirPath.exists()) {
				dirPath.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			} else {
				appendMode = true;
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), appendMode);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void outputToConsole () {
		System.out.println( processList() );
	}
	
	public void clear () {
		instance.list.clear();
	}
	
	public void setFileDirectory (String directory) {
		instance.dir = directory;
	}
	
}
