/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MacroEconomic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author John Luo
 */
public class Scraper {

    public Scraper() {
    }

    public String getStringFromInputStream(InputStream input) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(input));

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
        return sb.toString();
    }

    public double getdata() throws IOException {
        boolean report = true;
        double EIA = 0;
//        long in = System.currentTimeMillis();
        while (report) {
            InputStream input = new URL("http://ir.eia.gov/wpsr/table1.csv").openStream();
            String result = getStringFromInputStream(input);
            String cvsSplitBy = ",";
            String[] value = result.split(cvsSplitBy);
            String check = value[0].replaceAll("\"", "");
            if (check.equalsIgnoreCase("STUB_1")) {
                String EIA_word = value[12].replaceAll("\"", "");
                EIA = Double.parseDouble(EIA_word);
                report = false;
            } else {
                System.out.println("not it");
            }
        }
//        long out = System.currentTimeMillis();
//        long result = out - in;
//        System.out.println(result);
        return EIA;
    } 
    
    public static String getLastModified() throws MalformedURLException, IOException, ParseException {
        long start = System.currentTimeMillis();
        URLConnection connection = new URL("http://ir.eia.gov/wpsr/table1.csv").openConnection();
        String lastModified = connection.getHeaderField("Last-Modified");
        //Date date = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(lastModified);
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        return lastModified;
    }

    public static String getPartialFile() throws MalformedURLException, IOException {
        URL url = new URL("http://ir.eia.gov/wpsr/table1.csv");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Range", "bytes=0-170");
        urlConnection.connect();

//        System.out.println("Respnse Code: " + urlConnection.getResponseCode());
//        System.out.println("Content-Length: " + urlConnection.getContentLengthLong());

        InputStream inputStream = urlConnection.getInputStream();
        long size = 0;
        
        String result = "";
        char c;
        int i;
        
        
        // reads till the end of the stream
         while((i=inputStream.read())!=-1)
         {
            // converts integer to character
            c=(char)i;
            result += c;
            
            // prints character
//            System.out.print(c);
         }
         return result;
//        System.out.println("Downloaded Size: " + size);
    }
    
    public static void main(String args[]) throws IOException, MalformedURLException, ParseException {
        long start = System.currentTimeMillis();
       
        //Scraper scraper = new Scraper();
        //double final_number = scraper.getdata();
        //System.out.println(final_number);
        
        
        String result = getPartialFile();
        
        //String result = getLastModified();
        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end-start));
        //System.out.println(result);
    }
}
