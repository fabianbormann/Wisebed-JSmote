/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;

/**
 *
 * @author Fabian
 */
@ManagedBean
public class User {
    private String username;
    private String password;
    private String hash;     

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String reserve(){
        Logger.getLogger(User.class.getName())
                .log(Level.INFO, "try to reserve..");
        
        return "manage";
    }
}
