/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MacroEconomic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class NodeScriptManager {
	private static SubLog log = new SubLog("GO");
	
	private static String windowsCommand = "node.exe";
	private static String macCommand = "/usr/local/bin/node";
	
	private static String windowsDir = "C:\\Users\\Administrator\\Desktop\\NodeScraper\\";
//	private static String macDir = "/users/barak/projects/freelance/nodealgo/src/";
	private static String macDir = "/users/barak/projects/freelance/Algo_Scraper/";
	
	private static String macGoCommand = "/Users/barak/projects/GO/bin/test";
	private static String windowsGoCommand = "C:\\Users\\Administrator\\GO\\bin\\scraper.exe";
	
	private static String command = macCommand;
	private static String dir = macDir;
	private static String goCommand = windowsGoCommand;
	
	public static void main(String[] args) {
		TimeObject time = new TimeObject(10,55);
		if (args.length >= 2) {
			int argHour = Integer.parseInt(args[0]);
			int argMin = Integer.parseInt(args[1]);
			time.hour = argHour;
			time.minute = argMin;
		}
//		getCrudeOil("09:56");
		
//		String data = runGo(new TimeObject(14,20));
//		log.log("Got data: " + data);
//		log.outputToConsole();
		ScraperCallback<String,String> cb = (err,val) -> {
			log.log("Value: " + val);
			log.outputToConsole();
			log.outputToFile();
		};
		scrape(time,cb);
	}
	
	public static void scrape(TimeObject time, ScraperCallback<String,String> cb) {
		String msg = "Will Scrape at: " + time.hour + "h " + time.minute + "m";
		System.out.println(msg);
		log.log(msg);
		Runnable r = () -> {
			String result = runGo(time);
//			Double value = Double.parseDouble(result);
			cb.apply("",result);
		};
		new Thread(r).start();
	}
	
	public static String runGo(TimeObject time) {
		Long veryStart = System.currentTimeMillis();
		Runtime rt = Runtime.getRuntime();
        Process proc;
        log.log("Running script");
		try {
			proc = rt.exec(goCommand + " " + time.hour + " " + time.minute);
			//Long start = System.currentTimeMillis();
	        BufferedReader stdInput = new BufferedReader(new 
	             InputStreamReader(proc.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new 
	             InputStreamReader(proc.getErrorStream()));

	        // read the output from the command
	        String s = null;
	        String output = "";
	        while ((s = stdInput.readLine()) != null) {
	            output += s;
	        }
	        
	        return output;

		} catch (IOException e) {
			System.out.println(e);
			return "ERROR";
		}
	}
	
	public static void getCrudeOil(String time) {
		String crudeOilScriptName = "scraper.js"; 
		String data = getScriptOutput(dir + crudeOilScriptName, "time="+time);
		log.log("Got Value: " + data);
		log.outputToConsole();
	}
	
	private static String getScriptOutput(String scriptPath, String params) {
		Long veryStart = System.currentTimeMillis();
		Runtime rt = Runtime.getRuntime();
        String[] commands = {command, scriptPath, params};
        Process proc;
        log.log("About to exec");
		try {
			proc = rt.exec(commands);
			//Long start = System.currentTimeMillis();
	        BufferedReader stdInput = new BufferedReader(new 
	             InputStreamReader(proc.getInputStream()));

	        BufferedReader stdError = new BufferedReader(new 
	             InputStreamReader(proc.getErrorStream()));

	        // read the output from the command
	        String s = null;
	        String output = "";
	        while ((s = stdInput.readLine()) != null) {
	            output += s;
	        }
	        
	        return output;
	        
//	        String[] outputs = output.split("#");
//	        Long start = Long.parseLong(outputs[1]);
//	        Long end = System.currentTimeMillis();
//	        String value = outputs[0];
//	        System.out.println("Total Time: " + (end - start));
//	        System.out.println("Very Total Time: " + (end - veryStart));
//	        System.out.println(outputs[2]);
//	        return value;
		} catch (IOException e) {
			System.out.println(e);
			return "ERROR";
		}
	}

}
