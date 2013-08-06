/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Fabian
 */
public class ExperimentConsole {
    private LinkedHashMap<String, String> console = new LinkedHashMap<String, String>();
    
    public String getConsole(String experimentSourceKey) {        
        
        ArrayList<String> output = new ArrayList<String>();
        
        for (Map.Entry<String,String> message : this.console.entrySet()){
            if (message.getKey().equals(experimentSourceKey)){
                output.add(message.getValue());
            }
        }
        
        String outputString = "";
        
        for (String message : output){
            outputString += message+"<br />";
        }
        
        return outputString;
    }
    
    public void LogToConsole(String experimentSourceKey, String message) {
        this.console.put(experimentSourceKey, message);
    }
}
