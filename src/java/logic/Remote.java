/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Fabian
 */

@ManagedBean
@SessionScoped
public class Remote {
    
    private final String REMOTE_IMAGE_PATH = "";
    private ArrayList nodeArray;
    
    public Remote(ArrayList nodeArray){
        this.nodeArray = nodeArray;
    }
    
    public void flashRemoteImage(){
        //TODO
    }

    public String getFlashArray(){
        return nodeArray.toString();
    }
}
