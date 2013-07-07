/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import logic.EventController;

/**
 *
 * @author Fabian
 */
@ManagedBean
@SessionScoped
public class User {

    private String username;
    private String password;
    private String urnPrefix;
    private String nodeURNs;
    private String secretReservationKey;
    private Integer offset = 0;
    private Integer duration = 5;
    private boolean experimentLogin = false;
    
    private EventController controller = new EventController(this);

    public String getUrnPrefix() {
        return urnPrefix;
    }

    public void setUrnPrefix(String urnPrefix) {
        this.urnPrefix = urnPrefix;
    }

    public String getNodeURNs() {
        return nodeURNs;
    }

    public void setNodeURNs(String nodeURNs) {
        this.nodeURNs = nodeURNs;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

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

    public String getSecretReservationKey() {
        return secretReservationKey;
    }

    public void setSecretReservationKey(String secretReservationKey) {
        this.secretReservationKey = secretReservationKey;
    }
    
    public String doLogin(){
        return controller.startLogin();
    }
        
    public String doFlash(){
        return controller.startFlashing();
    }

    public Boolean getExperimentLogin() {
        return experimentLogin;
    }
    
    public void setExperimentLogin(Boolean experimentLogin) {
        this.experimentLogin = experimentLogin;
    }
    
}
