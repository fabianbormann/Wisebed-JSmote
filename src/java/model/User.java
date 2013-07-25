/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import logic.UserController;

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
    private int offset;
    private int duration;
    private boolean experimentLogin = false;
    private UserController controller = new UserController(this);

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

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
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

    public String doLogin() {
        return controller.startLogin();
    }

    public String doReserve(){
        return controller.startReservation();
    }
    
    public String doLogout() {
        return controller.startLogout();
    }

    public Boolean getExperimentLogin() {
        return experimentLogin;
    }

    public void setExperimentLogin(Boolean experimentLogin) {
        this.experimentLogin = experimentLogin;
    }
}
