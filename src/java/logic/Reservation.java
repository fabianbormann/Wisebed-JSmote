package logic;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
/**
 *
 * @author Fabian
 */
public class Reservation {

    private String urnPrefix;
    private String username;
    private String password;
    private Integer offset;
    private Integer duration;
    private ArrayList nodeURNs;
    
    //TODO use variable endpoints
    String snaaEndpointURL = "http://wisebed.itm.uni-luebeck.de:8890/snaa";
    String rsEndpointURL = "http://wisebed.itm.uni-luebeck.de:8889/rs";
    String sessionManagementEndpointURL = "http://wisebed.itm.uni-luebeck.de:8888/sessions";

    public Reservation(String urnPrefix, String username, String password,
            String nodeURNs) {
       
        this.urnPrefix = urnPrefix;
        this.username = username;
        this.password = password;
        this.nodeURNs = splitURNs(nodeURNs);
     
    }
    /**
     * Splitt the URNs by ";" as delimiter 
     * @param nodeURNs
     * @return 
     */
    private ArrayList splitURNs(String nodeURNasString) {
        ArrayList splittedURNs = Lists.newArrayList();
        
        Pattern pattern = Pattern.compile(";");
        String[] URNs = pattern.split(nodeURNasString);
        
        splittedURNs.addAll(Arrays.asList(URNs));
        
        return splittedURNs;
        
    }
    
}
