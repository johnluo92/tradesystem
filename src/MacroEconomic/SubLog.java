/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MacroEconomic;

public class SubLog {
    private String name = "";

    public SubLog (String name) {
        this.name = name;
        Log.getInstance();
    }

    public void log (String msg) {
        Log.getInstance().log(name, msg);
    }

    public void outputToFile () {
        Log.getInstance().outputToFile();
    }

    public void outputToConsole () {
        Log.getInstance().outputToConsole();
    }

    public void clear () {
        Log.getInstance().clear();
    }

    public void setFileDirectory (String dir) {
        Log.getInstance().setFileDirectory(dir);
    }
}
