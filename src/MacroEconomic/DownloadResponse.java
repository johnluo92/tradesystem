/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MacroEconomic;

@FunctionalInterface
interface ScraperCallback<One, Two> {
    public void apply(One one, Two two);
}

interface DownloadCallback {
    public void apply(DownloadResponse one);
}

public class DownloadResponse {
	String data;
	String error;
	public DownloadResponse() {}
	public DownloadResponse(String e, String d) {
		data = d;
		error = e;
	}
}
